# 0007. Infrastructure as code with Terraform

- **Status:** Accepted
- **Date:** 2026-10-09

## Context and problem statement

All infrastructure must be defined as code and deployed without manual changes (RNF-OPE-01), from GitHub Actions, for a development and a production environment. Which tool should be used?

## Decision drivers

- Job market demand for the tool.
- Reuse of the same definitions in both environments.

## Considered options

1. Terraform.
2. AWS CloudFormation.
3. AWS CDK.

## Decision

**Terraform**, with reusable modules and one configuration per environment in `infra/`. It is the most widely used tool in the job market, and its modules let both environments share the same definitions.

Where the state is stored is decided in [ADR 0011](0011-terraform-state-amazon-s3.md), and how the environments are laid out in AWS in [ADR 0012](0012-single-aws-account.md).

## Pros and cons of the options

| Option | Pro | Con |
|---|---|---|
| Terraform | Highest market demand; reusable modules; `plan` shows changes before applying them | The state must be stored outside the tool |
| CloudFormation | Native to AWS; AWS manages the state | Verbose templates; little value outside AWS |
| CDK | Infrastructure in a general-purpose language | Generates CloudFormation underneath, which makes errors harder to diagnose |

## Consequences

- ✅ Infrastructure that is versioned, reviewable and reproducible (RNF-OPE-01).
- ⚠️ The state needs its own storage and protection (see ADR 0011).

## Related decisions

- [0005. Backend compute on AWS Lambda](0005-compute-aws-lambda.md)
- [0011. Terraform state in Amazon S3](0011-terraform-state-amazon-s3.md)
- [0012. Single AWS account for development and production](0012-single-aws-account.md)
