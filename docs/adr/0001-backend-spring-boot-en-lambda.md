# 0001. Spring Boot backend on AWS Lambda

- **Status:** Partially superseded by [0013](0013-lambda-container-image-web-adapter.md) (packaging, SnapStart, Spring Boot version)
- **Date:** 2026-10-08
- **Note:** The build tool (Gradle) is superseded by [0009](0009-backend-build-tool-maven.md).

## Context

The backend was planned with NestJS. We want a framework with more value in the job market, while keeping idle cost ≤ 25 USD/month (RNF-COS-01), latency targets (RNF-REN-04) and an OpenAPI contract generated from code (RNF-OPE-06).

## Options

| Option | Pro | Con |
|---|---|---|
| NestJS on Lambda | No changes; same language as the frontend | Lower job market demand |
| Spring Boot 3 on Lambda + SnapStart | High demand; zero idle cost; springdoc covers RNF-OPE-06 | SnapStart lifecycle; more memory; second toolchain |
| Spring Boot 3 on Fargate + ALB | No cold starts | ~25 USD/month just running: breaks RNF-COS-01 |
| Spring Boot 3 as GraalVM native image | Fast startup, low memory | Complex, slow build; reflection config |

## Decision

**Spring Boot 3 + Java 21 on AWS Lambda with SnapStart**, behind API Gateway HTTP API, using `aws-serverless-java-container-springboot3` and springdoc-openapi. It has the highest job market demand and keeps pay-per-use cost.

## Consequences

- ✅ High-demand technology, zero idle cost, OpenAPI from code.
- ⚠️ PostgreSQL connections must open after the snapshot restore, never during the captured init.
- ⚠️ The function needs 1–2 GB of memory, so each invocation costs more than Node.js.
- ⚠️ CI handles two toolchains: Java for `apps/api`, Node.js for `apps/web`.
