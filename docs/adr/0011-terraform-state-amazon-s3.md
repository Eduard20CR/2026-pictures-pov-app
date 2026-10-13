# 0011. Terraform state in Amazon S3

- **Status:** Accepted
- **Date:** 2026-10-13

## Context and problem statement

[ADR 0007](0007-infrastructure-as-code-terraform.md) chose Terraform and stored its state in HCP Terraform, with one workspace per environment. That ADR identified as risks the dependency on a platform outside AWS, the need to configure access from each workspace to its AWS account, and the limits of the HCP Terraform free plan.

This ADR revisits only where the state is stored. The choice of Terraform, its modules and the configuration per environment remain as decided in ADR 0007.

## Decision drivers

- Keep the infrastructure and its state within AWS, without an additional external platform to secure.
- Reuse the authentication already planned for the pipeline (GitHub Actions with OIDC to AWS), without a second trust relationship.
- No plan limits on the number of managed resources.
- Negligible cost, compatible with RNF-COS-01.
- Versioning, encryption and locking of the state.

## Considered options

1. HCP Terraform (current decision in ADR 0007).
2. Amazon S3 backend with native locking.

## Decision

**Option 2 is chosen: the Terraform state is stored in Amazon S3**, in one bucket per AWS account, with versioning, server-side encryption and public access blocked. Locking uses the S3 native lock file (`use_lockfile`, available since Terraform 1.10), so no DynamoDB table is required.

The main reason is that it keeps the state in the same AWS accounts as the infrastructure. It also removes the external dependency and the plan limits, and Terraform is executed from GitHub Actions with the same OIDC credentials used for the rest of the deployment.

The bucket is created by a separate configuration, `infra/bootstrap/`, applied once per account before the environments. Its own state is kept locally, because the bucket cannot store the state of the configuration that creates it.

This decision supersedes the state section of ADR 0007.

## Pros and cons of the options

### Option 1: HCP Terraform

- Pro: managed state with versioning, encryption, locking and run history included; no bootstrap required.
- Con: platform outside AWS whose access grants control over the infrastructure; requires configuring dynamic credentials from each workspace to AWS; free plan limits.

### Option 2: Amazon S3 backend

- Pro: state within the AWS accounts, protected with IAM; same OIDC authentication as the pipeline; no resource limits; versioning allows any previous state to be recovered; cost of a few cents per month.
- Con: requires a bootstrap configuration with local state; no web interface or run history, which are replaced by the GitHub Actions logs.

## Consequences

### Positive

- A single provider (AWS) and a single authentication mechanism for the infrastructure.
- Any previous version of the state can be recovered from the bucket versioning.

### Negative and risks

- **Bootstrap state.** The local state of `infra/bootstrap/` must be kept outside the repository and in a safe place; if lost, the bucket must be re-imported with `terraform import`.
- **Bucket protection.** Deleting the bucket would lose the state of every environment in the account; it must be protected with `prevent_destroy` and restrictive IAM permissions.
- **Bucket permissions.** Access to the bucket grants access to the state, which may contain sensitive values; it must be limited to the deployment role and administrators.

## Related decisions

- [0007. Infrastructure as code with Terraform](0007-infrastructure-as-code-terraform.md)
