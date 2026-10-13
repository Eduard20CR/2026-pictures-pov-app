# 0007. Infrastructure as code with Terraform

- **Status:** Accepted
- **Date:** 2026-10-09

## Context

All infrastructure must be code, deployed from GitHub Actions without manual changes (RNF-OPE-01), for dev and prod.

## Options

| Option | Pro | Con |
|---|---|---|
| Terraform | Highest demand; reusable modules; `plan` before applying | State stored outside the tool |
| CloudFormation | Native; AWS manages state | Verbose; little value outside AWS |
| CDK | General-purpose language | CloudFormation underneath; harder errors |

## Decision

**Terraform**, with reusable modules and one configuration per environment in `infra/`. State storage is in [0011](0011-terraform-state-amazon-s3.md); environment layout in [0012](0012-single-aws-account.md).

## Consequences

- ✅ Versioned, reviewable, reproducible infrastructure (RNF-OPE-01).
- ⚠️ The state needs its own storage and protection.
