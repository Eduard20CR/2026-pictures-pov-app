# 0007. Infrastructure as code with Terraform

- **Status:** Accepted
- **Date:** 2026-10-09

## Context and problem statement

All infrastructure must be defined as code and deployed without manual changes (RNF-OPE-01). The project has development and production environments in separate AWS accounts and is deployed from GitHub Actions. An infrastructure as code tool must be chosen, together with the place where the infrastructure state is stored.

## Decision drivers

- Job market demand for the tool.
- Reuse of definitions across environments.
- Reliable, shared storage of the infrastructure state on a managed platform.

## Considered options

1. Terraform.
2. AWS CloudFormation.
3. AWS CDK.

## Decision

**Option 1 is chosen: Terraform**, with reusable modules and one configuration per environment in `infra/`.

The main reason is that Terraform is the most widely used infrastructure as code tool in the job market, and its modules allow the same definitions to be reused in the development and production environments.

**State.** The state is stored in **HCP Terraform** (formerly Terraform Cloud), HashiCorp's managed platform for Terraform, with one workspace per environment. It keeps the state on a reliable platform with versioning, encryption and locking included, and is the option most closely aligned with industry practice for teams using Terraform.

## Pros and cons of the options

### Option 1: Terraform

- Pro: highest job market demand; reusable modules; declarative language; the plan shows changes before they are applied.
- Con: the state must be stored and protected outside the tool itself.

### Option 2: AWS CloudFormation

- Pro: native to AWS; AWS manages the state.
- Con: verbose templates and lower job market value outside AWS.

### Option 3: AWS CDK

- Pro: infrastructure defined in a general-purpose programming language.
- Con: generates CloudFormation underneath, adding a layer of abstraction that makes errors harder to diagnose.

## Consequences

### Positive

- The infrastructure is versioned, reviewable and reproducible across environments, in compliance with RNF-OPE-01.
- The team gains experience with a tool in high job market demand.

### Negative and risks

- **External dependency.** The state depends on a platform outside AWS; access to HCP Terraform must be protected, since it grants control over the infrastructure.
- **Access to AWS.** Each workspace must be configured to access its AWS account, preferably with dynamic credentials (OIDC) instead of long-lived keys.
- **Plan limits.** The limits of the HCP Terraform free plan must be checked against the number of resources managed.

## Related decisions

- [0005. Backend compute on AWS Lambda](0005-compute-aws-lambda.md)
