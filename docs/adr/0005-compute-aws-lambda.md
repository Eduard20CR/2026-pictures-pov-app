# 0005. Backend compute on AWS Lambda

- **Status:** Accepted
- **Date:** 2026-10-09

## Context and problem statement

The application is only used during specific events: traffic is concentrated on the event day and the following days, and is close to zero the rest of the time. The compute service for the backend (`apps/api`) must keep the cost of each environment with no traffic within 25 USD per month (RNF-COS-01).

[Decision 0001](0001-backend-spring-boot-en-lambda.md) chose Spring Boot as the framework and Lambda as its runtime. This record documents the choice of compute service and the alternative to adopt if Lambda proves too complex.

## Decision drivers

- Near-zero cost when there is no event.
- Complexity of the project and of the deployment process.
- Operational effort.

## Considered options

1. AWS Lambda.
2. Amazon ECS on AWS Fargate behind an Application Load Balancer.
3. Amazon EC2 instance started the day before each event and stopped after it.

## Decision

**Option 1 is chosen: AWS Lambda**, behind Amazon API Gateway (HTTP API).

The main reason is that Lambda is billed per invocation, so the backend's cost is close to zero outside events, with no need to start or stop anything manually.

**Review condition.** If Lambda significantly increases the complexity of the project or of its deployment, option 3 will be adopted, recorded in a new ADR that supersedes this one.

## Pros and cons of the options

### Option 1: AWS Lambda

- Pro: near-zero cost with no traffic; scales automatically; no servers to operate or to start and stop.
- Con: adds Lambda-specific concerns (SnapStart, cold starts, connection management, packaging).

### Option 2: Amazon ECS on AWS Fargate

- Pro: conventional container execution model, without Lambda-specific constraints.
- Con: the task and the load balancer are billed continuously, which violates RNF-COS-01.

### Option 3: Amazon EC2 started on demand

- Pro: simple, conventional execution model; low cost if it only runs during events.
- Con: requires starting the instance before each event and stopping it afterwards; forgetting either step causes downtime or unnecessary cost.

## Consequences

### Positive

- The backend has near-zero cost outside events, in compliance with RNF-COS-01.
- No manual operation is needed before or after each event.

### Negative and risks

- The added complexity of Lambda must be evaluated during the first development iterations to decide whether the review condition applies.

## Related decisions

- [0001. Spring Boot backend on AWS Lambda](0001-backend-spring-boot-en-lambda.md)
- [0004. Aurora Serverless v2 database](0004-database-aurora-serverless-v2.md)
