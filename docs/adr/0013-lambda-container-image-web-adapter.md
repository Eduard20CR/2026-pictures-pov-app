# 0013. Lambda container image with AWS Lambda Web Adapter

- **Status:** Accepted
- **Date:** 2026-10-15

## Context

[0001](0001-backend-spring-boot-en-lambda.md) chose a zip package with SnapStart and `aws-serverless-java-container`. We want a simpler package that runs Spring Boot unchanged, and to move to Spring Boot 4.1.1.

## Options

| Option | Pro | Con |
|---|---|---|
| Zip + SnapStart | Fast cold starts | Complex: snapshot lifecycle, adapter library |
| Container image + Lambda Web Adapter | Simple: plain Spring Boot app in a Docker image | No SnapStart: cold start of a few seconds |
| ECS | No cold starts | Fixed idle cost: breaks RNF-COS-01 |

## Decision

**Container image with AWS Lambda Web Adapter, Spring Boot 4.1.1 + Java 21, no SnapStart** (Lambda does not support it for images). Simplicity outweighs the slower cold start.

## Consequences

- ✅ Plain Spring Boot app; runs the same locally and on Lambda.
- ✅ No snapshot lifecycle: connections open normally at startup.
- ⚠️ Cold start of a few seconds can break RNF-REN-04.
- ⚠️ CI builds and pushes the image to Amazon ECR.
