# 0011. Terraform state in Amazon S3

- **Status:** Accepted
- **Date:** 2026-10-13

## Context and problem statement

Terraform ([ADR 0007](0007-infrastructure-as-code-terraform.md)) needs a remote place to store its state, shared by the pipeline and the team, with versioning, encryption and locking. Where should it live?

## Decision drivers

- Keep the state inside AWS, with no extra platform to secure.
- Reuse the pipeline's authentication (GitHub Actions with OIDC to AWS).
- No limits on managed resources and negligible cost (RNF-COS-01).

## Considered options

1. HCP Terraform (HashiCorp's managed platform).
2. Amazon S3 backend with native locking.

## Decision

**Amazon S3.** A single bucket with versioning, encryption and public access blocked. Each environment uses its own key (`dev/terraform.tfstate`, `prod/terraform.tfstate`). Locking uses the S3 native lock file (`use_lockfile`, Terraform 1.10+), so no DynamoDB table is needed.

The bucket is created by `infra/bootstrap/`, applied once. Its own state is local and never committed, because the bucket cannot store the state of the configuration that creates it.

## Pros and cons of the options

| Option | Pro | Con |
|---|---|---|
| HCP Terraform | Everything managed; run history; no bootstrap | Platform outside AWS; separate credentials to AWS; free plan limits |
| S3 | Inside AWS, protected with IAM; same OIDC as the pipeline; no limits; cents per month | Needs a bootstrap with local state; no web UI (GitHub Actions logs instead) |

## Consequences

- ✅ One provider and one authentication mechanism for all the infrastructure.
- ✅ Any previous state can be recovered thanks to versioning.
- ⚠️ If the bootstrap's local state is lost, the bucket must be re-imported with `terraform import`.
- ⚠️ Deleting the bucket loses every state: protect it with `prevent_destroy` and restrictive IAM.
- ⚠️ The state may contain secrets: only the deployment roles and administrators can read it.

## Related decisions

- [0007. Infrastructure as code with Terraform](0007-infrastructure-as-code-terraform.md)
- [0012. Single AWS account for development and production](0012-single-aws-account.md)
