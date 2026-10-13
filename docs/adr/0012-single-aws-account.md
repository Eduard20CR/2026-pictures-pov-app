# 0012. Single AWS account for development and production

- **Status:** Accepted
- **Date:** 2026-10-13

## Context and problem statement

The system must have two isolated environments, development and production (RNF-OPE-02). [ADR 0007](0007-infrastructure-as-code-terraform.md) assumed that each environment would live in its own AWS account. Before the first infrastructure is provisioned, it must be decided whether both environments live in separate AWS accounts or in a single account, and, in the latter case, how they are kept isolated.

This ADR revisits only the account layout. The choice of Terraform, its modules and the configuration per environment remain as decided in ADR 0007, and the state storage as decided in [ADR 0011](0011-terraform-state-amazon-s3.md).

## Decision drivers

- Isolation between development and production, as required by RNF-OPE-02.
- Operational simplicity for a small team: number of accounts, credentials, bootstraps and trust relationships to maintain.
- Cost of each environment with no traffic, within the limit of RNF-COS-01.
- Limiting the blast radius of an error or a compromised credential in development.

## Considered options

1. Separate AWS accounts for development and production, grouped in AWS Organizations.
2. A single AWS account, with environments isolated by naming, tags, Terraform state and IAM roles.

## Decision

**Option 2 is chosen: development and production run in a single AWS account.**

The main reason is simplicity: a single account means one bootstrap of the state bucket, one OIDC identity provider for GitHub Actions, one set of account-level resources and one place to review costs and activity. For a project of this size, the stronger isolation of separate accounts does not justify duplicating that work, provided that the environments are separated inside the account as follows.

**Resource naming.** Every resource name includes the project and the environment, with the pattern `<project>-<environment>-<component>` (for example, `pictures-pov-dev-api`). Globally unique names, such as S3 buckets, also include the region and the account identifier. No resource is shared between environments, except the account-level resources listed below.

**Tags.** Every resource carries the tags `Project`, `Environment` (`dev` or `prod`) and `ManagedBy`, applied through the `default_tags` of the AWS provider in each environment configuration. `Environment` is activated as a cost allocation tag so that the cost of each environment can be measured against RNF-COS-01, and an AWS Budgets budget is defined per environment filtered by that tag.

**Terraform state.** Each environment keeps its state under its own key in the state bucket (`dev/terraform.tfstate` and `prod/terraform.tfstate`), so a `plan` or `apply` in one environment never reads or locks the state of the other.

**IAM roles.** GitHub Actions assumes a different IAM role per environment through OIDC:

- The trust policy of the production role only accepts tokens issued for the `prod` GitHub environment of this repository, which requires approval before deploying. The development role only accepts tokens from this repository.
- The permissions of each role are limited, where the service allows it, to resources whose name starts with the prefix of its environment or that carry its `Environment` tag. The development role cannot modify production resources, nor the state key of production.
- The runtime roles of the application (Lambda functions) are also defined per environment and only grant access to the resources of their environment.

**Account-level resources.** The Terraform state bucket, the GitHub OIDC identity provider and the account-wide settings (such as the S3 public access block and the cost allocation tags) are shared. They are managed by `infra/bootstrap/` and are not modified by the environment configurations.

This decision supersedes the account layout assumed in ADR 0007.

## Pros and cons of the options

### Option 1: Separate AWS accounts

- Pro: the strongest isolation AWS offers; an error or a compromised credential in development cannot affect production; service quotas and costs are separated per account without additional configuration.
- Con: requires AWS Organizations and an administration account or IAM Identity Center; the bootstrap, the OIDC identity provider and the account-level resources must be created and maintained in each account; more credentials and roles to manage.

### Option 2: Single AWS account

- Pro: a single account to administer, secure and monitor; one bootstrap and one OIDC identity provider; no duplicated account-level resources; switching between environments does not require changing account.
- Con: the isolation depends on IAM policies, naming and tags being correct; service quotas are shared between environments; the cost per environment must be obtained from tags.

## Consequences

### Positive

- The infrastructure set-up is simpler: one bootstrap, one OIDC identity provider and one account to secure.
- Costs and activity of both environments are reviewed in a single place, and the cost of each environment remains measurable through the `Environment` tag.

### Negative and risks

- **Blast radius.** An overly broad IAM policy, or a credential with access to the whole account, may allow a change in development to affect production. The permissions of each role must be reviewed so that they are scoped to their environment, and the use of administrator credentials must be limited to the bootstrap.
- **Shared quotas.** Service quotas, such as the concurrent executions of AWS Lambda, are shared by both environments. A load test or an error in development may consume capacity needed by production; if necessary, reserved concurrency must be configured for the production functions.
- **Tag discipline.** A resource without the `Environment` tag is not attributed to any environment in the cost reports. The tags must be applied by default from the provider configuration, not resource by resource.
- **Reversibility.** If the project grows and requires stronger isolation, moving production to its own account implies recreating its resources and migrating its data; this would be recorded in a new ADR.

## Related decisions

- [0007. Infrastructure as code with Terraform](0007-infrastructure-as-code-terraform.md)
- [0011. Terraform state in Amazon S3](0011-terraform-state-amazon-s3.md)
