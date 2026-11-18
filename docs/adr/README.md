# Architecture Decision Records (ADR)

Key architecture decisions, one per file, in a short [MADR](https://adr.github.io/madr/) format.

## Index

| ADR | Decision | Status |
|---|---|---|
| [0001](0001-backend-spring-boot-en-lambda.md) | Spring Boot backend on AWS Lambda | Partially superseded by 0013 |
| [0002](0002-npm-without-workspaces.md) | npm without workspaces | Accepted |
| [0003](0003-frontend-react.md) | React frontend | Accepted |
| [0004](0004-database-aurora-serverless-v2.md) | Aurora Serverless v2 database | Accepted |
| [0005](0005-compute-aws-lambda.md) | Backend compute on AWS Lambda | Accepted |
| [0006](0006-authentication-amazon-cognito.md) | Authentication with Amazon Cognito | Partially superseded by 0018 |
| [0007](0007-infrastructure-as-code-terraform.md) | Infrastructure as code with Terraform | Accepted |
| [0008](0008-background-zip-generation.md) | Background ZIP generation | Accepted |
| [0009](0009-backend-build-tool-maven.md) | Maven as the backend build tool | Accepted |
| [0010](0010-database-migrations-liquibase.md) | Database migrations with Liquibase in CI/CD | Accepted |
| [0011](0011-terraform-state-amazon-s3.md) | Terraform state in Amazon S3 | Partially superseded by 0019 |
| [0012](0012-single-aws-account.md) | Single AWS account for development and production | Partially superseded by 0019, 0020 |
| [0013](0013-lambda-zip-snapstart-web-adapter.md) | Lambda zip with SnapStart and AWS Lambda Web Adapter | Accepted |
| [0014](0014-public-aurora-lambda-outside-vpc.md) | Public Aurora endpoint with Lambda outside the VPC | Accepted |
| [0015](0015-dev-database-neon-credentials-ssm.md) | Neon for the dev database, credentials in SSM Parameter Store | Accepted |
| [0016](0016-rest-api-conventions.md) | REST API conventions | Accepted |
| [0017](0017-api-same-origin-cloudfront.md) | API served under /api on the CloudFront origin | Accepted |
| [0018](0018-self-sign-up-email-only.md) | Self sign-up with email and password only | Accepted |
| [0019](0019-terraform-state-split-app-data.md) | Terraform state split into app and data per environment | Accepted |
| [0020](0020-ci-role-per-workflow.md) | One CI role per workflow per environment | Accepted |
| [0021](0021-rejected-upload-returns-quota.md) | Rejected or abandoned uploads return their quota | Accepted |

## Rules

- File name: `NNNN-title-with-hyphens.md`. Numbers are never reused.
- Status: Proposed, Accepted, Rejected, Deprecated or Superseded by `NNNN`.
- An accepted ADR is not rewritten; a new ADR supersedes it.
- The reasoning lives only in the ADR; other docs link to it.
- Keep it short: a few sentences per section.

## Template

```markdown
# NNNN. Decision title

- **Status:** Accepted
- **Date:** YYYY-MM-DD

## Context

The problem, in two or three sentences.

## Options

| Option | Pro | Con |
|---|---|---|
| A | … | … |
| B | … | … |

## Decision

**Option chosen**, and why in one or two sentences.

## Consequences

- ✅ …
- ⚠️ …
```
