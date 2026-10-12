# 0010. Database migrations with Liquibase in CI/CD

- **Status:** Accepted
- **Date:** 2026-10-12

## Context and problem statement

The database schema (Aurora Serverless v2 with PostgreSQL, see [decision 0004](0004-database-aurora-serverless-v2.md)) must evolve in a versioned and reproducible way across the development and production environments. A database migration tool is required, and it must be decided when and where the migrations are executed.

Spring Boot can run migrations automatically during application startup. However, the backend runs on AWS Lambda with SnapStart (see [decision 0001](0001-backend-spring-boot-en-lambda.md)), where startup time directly affects cold start latency and RNF-REN-04, and where connections to PostgreSQL must not be opened during the initialization captured in the snapshot.

## Decision drivers

- Lambda cold start time and the latency targets of RNF-REN-04.
- Compatibility with the SnapStart lifecycle.
- Versioned, reproducible schema changes across environments.
- Control over when schema changes are applied.

## Considered options

1. Run the migrations during application startup.
2. Run the migrations as a separate step in the CI/CD pipeline.

## Decision

**Liquibase is chosen as the database migration tool, and option 2 is chosen: the migrations run as a separate step in the CI/CD pipeline**, not during application startup.

- The changelogs are versioned in `apps/api` together with the backend code.
- Automatic execution at startup is disabled in the backend (`spring.liquibase.enabled=false`).
- The CI/CD pipeline applies the pending migrations to each environment before deploying the new version of the backend.

The main reason is that the backend runs on AWS Lambda, and running the migrations during application startup would make the function's start slower.

## Pros and cons of the options

### Option 1: Migrations during application startup

- Pro: no additional pipeline step; the schema is always up to date when the application starts.
- Con: increases the function's start time; with SnapStart, it would open database connections during the captured initialization; several instances starting concurrently would compete for the migration lock.

### Option 2: Migrations as a separate CI/CD step

- Pro: does not affect the function's start time; migrations run once per deployment, at a controlled point; a failed migration stops the deployment before the new code is released.
- Con: requires an additional pipeline step with network access to the database and its own credentials.

## Consequences

### Positive

- The Lambda function's start time does not include the migrations.
- Schema changes are applied once per deployment and are visible in the pipeline history.
- A failed migration prevents the deployment of code that depends on it.

### Negative and risks

- **Database access from CI/CD.** The pipeline step must be able to reach the database and authenticate against it. How it reaches the database depends on the network configuration of each environment.
- **Backward-compatible migrations.** Since the migrations are applied before the new code is deployed, the version currently running must keep working with the new schema. Breaking changes must be split across several deployments (expand and contract).
- **Database resume.** If the database is paused at 0 ACU, the migration step must wait for it to resume, which lengthens the deployment.

## Related decisions

- [0001. Spring Boot backend on AWS Lambda](0001-backend-spring-boot-en-lambda.md)
- [0004. Aurora Serverless v2 database](0004-database-aurora-serverless-v2.md)
