# 0011. Terraform state in Amazon S3

- **Status:** Accepted
- **Date:** 2026-10-13

## Context

Terraform ([0007](0007-infrastructure-as-code-terraform.md)) needs remote state shared by the pipeline and the team, with versioning, encryption and locking.

## Options

| Option | Pro | Con |
|---|---|---|
| HCP Terraform | Fully managed; run history; no bootstrap | Outside AWS; separate credentials; free plan limits |
| Amazon S3 | Inside AWS with IAM; same OIDC as the pipeline; cents per month | Bootstrap's first run is manual; no web UI |

## Decision

**Amazon S3**: all state, the bootstrap's included, lives in one versioned, encrypted, private bucket with native locking (`use_lockfile`, no DynamoDB) and one key per configuration:

| Configuration | Key |
|---|---|
| Bootstrap | `bootstrap/terraform.tfstate` |
| dev | `dev/terraform.tfstate` |
| prod | `prod/terraform.tfstate` |

`infra/bootstrap/` creates the bucket. Its first run uses local state; it then enables its `s3` backend and runs `terraform init -migrate-state`. No state file is ever committed. Steps: [`infra/bootstrap/README.md`](../../infra/bootstrap/README.md).

## Consequences

- ✅ One provider and one authentication for everything; every state versioned, shared and recoverable.
- ⚠️ The bootstrap depends on the bucket it manages: if the bucket is recreated, repeat the manual first run.
- ⚠️ Deleting the bucket loses every state: use `prevent_destroy` and strict IAM.
- ⚠️ State may hold secrets: only deploy roles and admins can read it.
