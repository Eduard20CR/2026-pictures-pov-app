# 0011. Terraform state in Amazon S3

- **Status:** Accepted
- **Date:** 2026-10-13

## Context

Terraform ([0007](0007-infrastructure-as-code-terraform.md)) needs remote state shared by the pipeline and the team, with versioning, encryption and locking.

## Options

| Option | Pro | Con |
|---|---|---|
| HCP Terraform | Fully managed; run history; no bootstrap | Outside AWS; separate credentials; free plan limits |
| Amazon S3 | Inside AWS with IAM; same OIDC as the pipeline; cents per month | Bootstrap with local state; no web UI |

## Decision

**Amazon S3**: one versioned, encrypted, private bucket, one key per environment (`dev/terraform.tfstate`, `prod/terraform.tfstate`) and native locking (`use_lockfile`, no DynamoDB). `infra/bootstrap/` creates the bucket once; its own state is local and never committed.

## Consequences

- ✅ One provider and one authentication for everything; old states recoverable.
- ⚠️ Losing the bootstrap state means re-importing the bucket with `terraform import`.
- ⚠️ Deleting the bucket loses every state: use `prevent_destroy` and strict IAM.
- ⚠️ State may hold secrets: only deploy roles and admins can read it.
