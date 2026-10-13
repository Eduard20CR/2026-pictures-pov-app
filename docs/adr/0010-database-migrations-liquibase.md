# 0010. Database migrations with Liquibase in CI/CD

- **Status:** Accepted
- **Date:** 2026-10-12

## Context

The schema ([0004](0004-database-aurora-serverless-v2.md)) must evolve in a versioned way across environments. Running migrations at startup would slow Lambda cold starts (RNF-REN-04) and open connections during the SnapStart init ([0001](0001-backend-spring-boot-en-lambda.md)).

## Options

| Option | Pro | Con |
|---|---|---|
| At application startup | No extra pipeline step | Slower start; connections in the snapshot; instances fight for the lock |
| Separate CI/CD step | Fast start; runs once per deploy; failure stops the deploy | Pipeline needs network access and credentials to the database |

## Decision

**Liquibase, run as a CI/CD step** before deploying the backend. Changelogs live in `apps/api`; `spring.liquibase.enabled=false`.

## Consequences

- ✅ Start time unaffected; one visible migration per deploy; a failed migration blocks the deploy.
- ⚠️ The pipeline must reach and authenticate to the database.
- ⚠️ Migrations must stay backward compatible with the running version (expand and contract).
- ⚠️ A paused database (0 ACU) must resume first, which slows the deploy.
