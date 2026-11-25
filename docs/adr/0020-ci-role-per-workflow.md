# 0020. One CI role per workflow per environment

- **Status:** Accepted
- **Date:** 2026-11-18

## Context

[0012](0012-single-aws-account.md) gave each environment one GitHub OIDC role. That role would need the permissions of every deploy (API, web, Lambdas, infrastructure), so any workflow could do what the broadest one does.

## Options

| Option | Pro | Con |
|---|---|---|
| One role per environment | One role and one variable to manage | Every workflow gets the broadest permissions |
| One role per workflow per environment | Least privilege: each deploy reaches only its own resources | More roles and repository variables |

## Decision

**One OIDC role per deploy workflow per environment** (least privilege). This replaces the IAM row of 0012; its other rules stay:

| Workflow | dev role | Repository variable |
|---|---|---|
| `api-deploy-dev` | `<project>-dev-gha-api` | `AWS_DEV_GHA_ROLE_ARN_API` |
| `web-deploy-dev` | `<project>-dev-gha-web` | `AWS_DEV_GHA_ROLE_ARN_WEB` |
| `image-processor-deploy-dev` | `<project>-dev-gha-image-processor` | `AWS_DEV_GHA_ROLE_ARN_IMAGE_PROCESSOR` |
| `upload-cleanup-deploy-dev` | `<project>-dev-gha-upload-cleanup` | `AWS_DEV_GHA_ROLE_ARN_UPLOAD_CLEANUP` |
| `zip-export-deploy-dev` | `<project>-dev-gha-zip-export` | `AWS_DEV_GHA_ROLE_ARN_ZIP_EXPORT` |
| `infra-deploy-dev` | `<project>-dev-gha-infra` | `AWS_DEV_GHA_ROLE_ARN_INFRA` |

Roles live in `infra/bootstrap/github_oidc_*.tf`. Each reaches only its own environment; `prod` roles, when added, require GitHub approval. `db-migrate-dev` uses repository secrets, not a role.

## Consequences

- ✅ A compromised app workflow cannot change infrastructure or other apps.
- ⚠️ A new deploy workflow needs its own role and variable.
