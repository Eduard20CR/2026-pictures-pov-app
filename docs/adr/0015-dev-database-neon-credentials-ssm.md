# 0015. Neon for the dev database, credentials in SSM Parameter Store

- **Status:** Accepted
- **Date:** 2026-10-19

## Context

The dev environment needs a PostgreSQL database within the idle cost limit (RNF-COS-01). Aurora ([0004](0004-database-aurora-serverless-v2.md), [0014](0014-public-aurora-lambda-outside-vpc.md)) is kept for prod. The same jar must run locally, in dev and in prod, with no credentials in code, Git or Terraform.

## Options

| Option | Pro | Con |
|---|---|---|
| Aurora Serverless v2 in dev | Same engine as prod | Public IPv4 and storage cost; slow resume |
| Neon free tier in dev | No cost; scales to zero; TLS; built-in pooler | Third-party provider; not the prod engine |
| Credentials in Lambda env vars from Terraform | Simple | Visible in the console and in the Terraform state |
| Credentials in SSM SecureString, read by the app | Not in code, Git, state or console | Small startup code; read at init |
| Spring Cloud AWS Parameter Store | No custom code | Community library; compatibility with Spring Boot 4.1 not listed |

## Decision

**Neon in dev; URL, user and password in SSM Parameter Store as SecureString, read at startup with the official AWS SDK.** Free for dev, and no secret leaves SSM except into the running process.

| Environment | Database | Credentials |
|---|---|---|
| Local | Docker PostgreSQL | `application.properties` defaults |
| dev | Neon (pooler host, `sslmode=require`) | `/pictures-pov/dev/db/` in SSM |
| prod | Aurora ([0014](0014-public-aurora-lambda-outside-vpc.md)) | `/pictures-pov/prod/db/` in SSM |

- Parameters are named after the Spring property: `/pictures-pov/<env>/db/spring.datasource.{url,username,password}`, created by hand with the AWS managed key `aws/ssm`.
- Terraform only sets `SSM_DB_PATH` and grants the Lambda role read access to that path.
- `SsmDbSettings` runs before Spring starts: if `SSM_DB_PATH` exists, it reads the path and sets the values as system properties; otherwise the local defaults apply.

## Consequences

- ✅ No secrets in Git, Terraform variables, state or the Lambda console.
- ✅ Same artifact in every environment; only `SSM_DB_PATH` changes.
- ⚠️ Values are read during the SnapStart init and stored in the snapshot: a credential change needs a new published version.
- ⚠️ Neon suspends when idle: the first request after a pause is slower.
- ⚠️ Dev and prod run different providers; engine-specific behavior is only tested in prod.
- ⚠️ Supersedes the Secrets Manager option in [0014](0014-public-aurora-lambda-outside-vpc.md) for storing credentials.

## References

- [SSM Parameter Store: GetParametersByPath](https://docs.aws.amazon.com/systems-manager/latest/APIReference/API_GetParametersByPath.html)
- [Restrict Parameter Store access with IAM](https://docs.aws.amazon.com/systems-manager/latest/userguide/sysman-paramstore-access.html)
- [Spring Boot externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- Issue #21
