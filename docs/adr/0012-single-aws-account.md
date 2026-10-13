# 0012. Single AWS account for development and production

- **Status:** Accepted
- **Date:** 2026-10-13

## Context and problem statement

The system needs two isolated environments, development and production (RNF-OPE-02). Should they live in separate AWS accounts or in a single one? And if single, how are they kept apart?

## Decision drivers

- Isolation between environments (RNF-OPE-02).
- Simplicity for a small team: fewer accounts, credentials and bootstraps.
- Cost per environment measurable against RNF-COS-01.

## Considered options

1. Separate accounts, grouped in AWS Organizations.
2. A single account, with environments isolated by naming, tags, state and IAM roles.

## Decision

**A single AWS account.** One bootstrap, one OIDC provider for GitHub Actions and one place to review costs. The stronger isolation of separate accounts does not justify duplicating that work at this project's size.

The environments are kept apart with four rules:

| Rule | How |
|---|---|
| Naming | `<project>-<environment>-<component>`, e.g. `pictures-pov-dev-api`. Globally unique names (S3) also include region and account ID. |
| Tags | `Project`, `Environment` (`dev`/`prod`) and `ManagedBy` via the provider's `default_tags`. `Environment` is a cost allocation tag, with one AWS Budgets budget per environment. |
| State | One key per environment in the state bucket ([ADR 0011](0011-terraform-state-amazon-s3.md)). |
| IAM | One OIDC role per environment. The `prod` role only accepts the `prod` GitHub environment, which requires approval. Each role, including the Lambda runtime roles, only reaches resources of its own environment. |

Only account-level resources are shared (state bucket, OIDC provider, account settings). They are managed by `infra/bootstrap/`.

## Pros and cons of the options

| Option | Pro | Con |
|---|---|---|
| Separate accounts | Strongest isolation; quotas and costs separated automatically | AWS Organizations; bootstrap, OIDC and roles duplicated per account |
| Single account | One account to administer and secure; no duplicated resources | Isolation depends on correct IAM, naming and tags; shared quotas; cost per environment comes from tags |

## Consequences

- ✅ Simpler set-up, with cost per environment still measurable through the `Environment` tag.
- ⚠️ A too-broad IAM policy could let development affect production: review role scopes and use admin credentials only for the bootstrap.
- ⚠️ Quotas (e.g. Lambda concurrency) are shared: reserve concurrency for production if needed.
- ⚠️ Moving production to its own account later means recreating resources and migrating data (a new ADR).

## Related decisions

- [0007. Infrastructure as code with Terraform](0007-infrastructure-as-code-terraform.md)
- [0011. Terraform state in Amazon S3](0011-terraform-state-amazon-s3.md)
