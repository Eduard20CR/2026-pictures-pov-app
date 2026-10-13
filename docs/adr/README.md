# Architecture Decision Records (ADR)

Key architecture decisions, one per file, in a short [MADR](https://adr.github.io/madr/) format.

## Index

| ADR | Decision | Status |
|---|---|---|
| [0001](0001-backend-spring-boot-en-lambda.md) | Spring Boot backend on AWS Lambda | Accepted |
| [0002](0002-npm-without-workspaces.md) | npm without workspaces | Accepted |
| [0003](0003-frontend-react.md) | React frontend | Accepted |
| [0004](0004-database-aurora-serverless-v2.md) | Aurora Serverless v2 database | Accepted |
| [0005](0005-compute-aws-lambda.md) | Backend compute on AWS Lambda | Accepted |
| [0006](0006-authentication-amazon-cognito.md) | Authentication with Amazon Cognito | Accepted |
| [0007](0007-infrastructure-as-code-terraform.md) | Infrastructure as code with Terraform | Accepted |
| [0008](0008-background-zip-generation.md) | Background ZIP generation | Accepted |
| [0009](0009-backend-build-tool-maven.md) | Maven as the backend build tool | Accepted |
| [0010](0010-database-migrations-liquibase.md) | Database migrations with Liquibase in CI/CD | Accepted |
| [0011](0011-terraform-state-amazon-s3.md) | Terraform state in Amazon S3 | Accepted |
| [0012](0012-single-aws-account.md) | Single AWS account for development and production | Accepted |

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
