# 0001. Spring Boot backend on AWS Lambda

- **Status:** Accepted
- **Date:** 2026-10-08

## Context and problem statement

The application's backend (`apps/api`) was planned with NestJS running on AWS Lambda behind Amazon API Gateway (HTTP API). The backend framework is being reconsidered, since one of the project's goals is for the experience gained to have the greatest possible value in the job market.

The new solution must respect the architecture's existing constraints:

- The infrastructure cost of each environment with no traffic must not exceed 25 USD per month (RNF-COS-01).
- API operations must respond in under 500 ms (p95) and under 1.5 s (p99) (RNF-REN-04).
- The API contract must be documented in OpenAPI format and generated from the code (RNF-OPE-06).

## Decision drivers

- Job market demand for the technology in Costa Rica and internationally.
- Idle cost compatible with RNF-COS-01.
- Cold start latency compatible with RNF-REN-04.
- Complexity of the build and deployment process.
- Generation of the OpenAPI contract from the code.

## Considered options

1. Keep NestJS on AWS Lambda.
2. Spring Boot 3 on AWS Lambda with SnapStart.
3. Spring Boot 3 on AWS Fargate behind an Application Load Balancer.
4. Spring Boot 3 compiled as a GraalVM native image on AWS Lambda.

## Decision

**Option 2 is chosen: Spring Boot 3 with Java 21 on AWS Lambda**, behind Amazon API Gateway (HTTP API).

- Lambda and Spring are integrated with `aws-serverless-java-container-springboot3`.
- Lambda SnapStart is enabled to reduce cold start time.
- `apps/api` is built with Gradle.
- The OpenAPI contract is generated from the code with springdoc-openapi.

The main reason is that Spring Boot has considerably higher job market demand than NestJS, both in Costa Rica and internationally. Running on Lambda keeps the pay-per-use model and, therefore, compliance with RNF-COS-01.

## Pros and cons of the options

### Option 1: NestJS on AWS Lambda

- Pro: requires no changes to the architecture and shares a language with the frontend.
- Con: lower job market demand than Spring Boot, which is the deciding factor for this decision.

### Option 2: Spring Boot 3 on AWS Lambda with SnapStart

- Pro: high job market demand; zero idle cost; SnapStart mitigates the JVM cold start; springdoc-openapi covers RNF-OPE-06.
- Con: requires managing the SnapStart lifecycle correctly, more memory per function and a second toolchain in CI.

### Option 3: Spring Boot 3 on AWS Fargate with an Application Load Balancer

- Pro: conventional execution model, without cold starts or Lambda-specific constraints.
- Con: the environment costs approximately 25 USD per month just to stay running, so it violates RNF-COS-01.

### Option 4: Spring Boot 3 as a GraalVM native image on AWS Lambda

- Pro: very fast startup and lower memory usage.
- Con: the build process is complex and slow, and requires additional configuration for reflection and dynamic proxies.

## Consequences

### Positive

- The backend is developed with a technology in high job market demand.
- The architecture remains serverless with zero idle cost, in compliance with RNF-COS-01.
- The OpenAPI contract is generated from the code, in compliance with RNF-OPE-06.

### Negative and risks

- **Connections after SnapStart restore.** Connections to PostgreSQL must not be opened during the initialization captured in the snapshot, but after it is restored. Otherwise, restored instances would reuse invalid or shared connections.
- **Function memory.** The API's Lambda function requires between 1 and 2 GB of memory, which increases the cost per invocation compared with a Node.js runtime.
- **Two toolchains in CI.** The continuous integration pipeline must handle Java 21 with Gradle for `apps/api` and Node.js with npm for `apps/web`.

## Related decisions

- [0002. npm without workspaces](0002-npm-sin-workspaces.md)
