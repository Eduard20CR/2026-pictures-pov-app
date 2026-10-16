# 0013. Lambda zip with SnapStart and AWS Lambda Web Adapter

- **Status:** Accepted
- **Date:** 2026-10-16

## Context

[0001](0001-backend-spring-boot-en-lambda.md) chose a zip package with SnapStart and `aws-serverless-java-container`. We want a simpler package that runs Spring Boot unchanged, on Spring Boot 4.1.1. Measured in dev at 3008 MB, a container image without SnapStart had a cold start of about 9 s, close to the 10 s Lambda init limit and above RNF-REN-04.

## Options

| Option | Pro | Con |
|---|---|---|
| Container image + Lambda Web Adapter | Simple build and deploy | No SnapStart: cold start of about 9 s |
| Zip + SnapStart + Lambda Web Adapter | Cold start of about 1 s; plain Spring Boot app | Zip over 50 MB goes through S3; versions and an alias to manage |
| ECS | No cold starts | Fixed idle cost: breaks RNF-COS-01 |

## Decision

**Zip package (`run.sh` + the Spring Boot jar) on the `java21` runtime, with SnapStart and the AWS Lambda Web Adapter layer**, following the official [Spring Boot zip example](https://github.com/aws/aws-lambda-web-adapter/tree/main/examples/springboot-zip). A test showed a 1.05 s restore against 4.1 s without SnapStart, with the same jar.

## Consequences

- ✅ Plain Spring Boot app; runs the same locally and on Lambda.
- ✅ Cold start of about 1 s; SnapStart has no extra cost for Java.
- ⚠️ The snapshot is cloned: no database connections or unique state at startup.
- ⚠️ SnapStart only applies to published versions: API Gateway calls the `live` alias, and each deploy publishes a version and moves the alias.
- ⚠️ CI uploads the zip to an S3 artifacts bucket.

## References

- [AWS Lambda Web Adapter](https://github.com/aws/aws-lambda-web-adapter)
- [Spring Boot zip example](https://github.com/aws/aws-lambda-web-adapter/tree/main/examples/springboot-zip)
- [Lambda SnapStart](https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html)
