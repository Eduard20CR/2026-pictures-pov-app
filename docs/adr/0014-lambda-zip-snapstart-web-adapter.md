# 0014. Lambda zip with SnapStart and AWS Lambda Web Adapter

- **Status:** Accepted
- **Date:** 2026-10-16

## Context

[0013](0013-lambda-container-image-web-adapter.md) chose a container image without SnapStart. Measured in dev at 3008 MB, the cold start was about 9 s, close to the 10 s Lambda init limit and above RNF-REN-04. Container images do not support SnapStart; zip packages do.

## Options

| Option | Pro | Con |
|---|---|---|
| Container image + Lambda Web Adapter | Simple build and deploy | Cold start of about 9 s |
| Zip + SnapStart + Lambda Web Adapter | Cold start of about 1 s; plain Spring Boot app | Zip over 50 MB goes through S3; versions and an alias to manage |
| Container image + provisioned concurrency | No cold starts for warm instances | Hourly cost even when idle |

## Decision

**Zip package (`run.sh` + the Spring Boot jar) on the `java21` runtime, with SnapStart and the AWS Lambda Web Adapter layer**, following the official [Spring Boot zip example](https://github.com/aws/aws-lambda-web-adapter/tree/main/examples/springboot-zip). A test showed a 1.05 s restore against 4.1 s without SnapStart, with the same jar.

## Consequences

- ✅ Plain Spring Boot app; runs the same locally and on Lambda.
- ✅ Cold start of about 1 s; SnapStart has no extra cost for Java.
- ⚠️ The snapshot is cloned: no database connections or unique state at startup.
- ⚠️ SnapStart only applies to published versions: API Gateway calls the `live` alias, and each deploy publishes a version and moves the alias.
- ⚠️ CI uploads the zip to an S3 artifacts bucket instead of pushing to Amazon ECR.

## References

- [AWS Lambda Web Adapter](https://github.com/aws/aws-lambda-web-adapter)
- [Spring Boot zip example](https://github.com/aws/aws-lambda-web-adapter/tree/main/examples/springboot-zip)
- [Lambda SnapStart](https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html)
