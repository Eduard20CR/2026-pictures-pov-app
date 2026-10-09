# 0004. Aurora Serverless v2 database

- **Status:** Accepted
- **Date:** 2026-10-09

## Context and problem statement

The application stores events, guests' uploads and moderation data, which are relational by nature. The database was planned as PostgreSQL on Amazon RDS. The database engine and service must be confirmed, respecting the constraint that the infrastructure cost of each environment with no traffic must not exceed 25 USD per month (RNF-COS-01).

## Decision drivers

- Idle cost compatible with RNF-COS-01.
- Relational data model.
- Learning value: in-depth SQL and advanced relational database concepts.

## Considered options

1. PostgreSQL on Amazon RDS (provisioned instance).
2. Amazon Aurora Serverless v2 with PostgreSQL compatibility.
3. Amazon DynamoDB.

## Decision

**Option 2 is chosen: Amazon Aurora Serverless v2 with PostgreSQL compatibility**, configured with a minimum capacity of 0 ACU so that it pauses automatically when it is not in use.

The main reasons are that it scales to zero compute when idle, which keeps the environment within RNF-COS-01, and that it is PostgreSQL, which allows the team to learn SQL thoroughly and work with advanced relational database concepts.

## Pros and cons of the options

### Option 1: PostgreSQL on Amazon RDS

- Pro: simple and well-known PostgreSQL service.
- Con: the instance is billed continuously, even with no traffic.

### Option 2: Aurora Serverless v2 (PostgreSQL)

- Pro: pauses at 0 ACU when idle, so only storage is billed; scales automatically with load; full PostgreSQL compatibility.
- Con: the first request after a pause must wait for the database to resume.

### Option 3: Amazon DynamoDB

- Pro: pay-per-request with no idle cost and no connection management.
- Con: it is not relational, so it does not meet the learning goal and fits the data model worse.

## Consequences

### Positive

- The environment has no compute cost for the database while idle, in compliance with RNF-COS-01.
- The team gains experience with SQL and advanced PostgreSQL features.

### Negative and risks

- **Resume latency.** Resuming from 0 ACU takes several seconds, so the first request after a pause can exceed the latency targets of RNF-REN-04. The pause timeout must be tuned per environment.
- **Connections from Lambda.** Connections must still be opened after the SnapStart restore (see [decision 0001](0001-backend-spring-boot-en-lambda.md)), and the connection pool must be kept small to avoid exhausting the database's connections.

## Related decisions

- [0001. Spring Boot backend on AWS Lambda](0001-backend-spring-boot-en-lambda.md)
