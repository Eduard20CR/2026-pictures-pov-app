# 0012. Single AWS account for development and production

- **Status:** Accepted
- **Date:** 2026-10-13

## Context

dev and prod must be isolated (RNF-OPE-02). Separate AWS accounts or one?

## Options

| Option | Pro | Con |
|---|---|---|
| Separate accounts (Organizations) | Strongest isolation; quotas and costs split | Bootstrap, OIDC and roles duplicated |
| Single account | One account to run and secure | Isolation depends on IAM, names and tags; shared quotas |

## Decision

**A single AWS account**, isolated by four rules:

| Rule | How |
|---|---|
| Naming | `<project>-<environment>-<component>`, e.g. `pictures-pov-dev-api` (S3 adds region and account ID) |
| Tags | `Project`, `Environment`, `ManagedBy` via `default_tags`; one budget per `Environment` |
| State | One key per environment ([0011](0011-terraform-state-amazon-s3.md)) |
| IAM | One OIDC role per environment; `prod` requires GitHub approval; roles only reach their own environment |

Shared account-level resources (state bucket, OIDC provider) live in `infra/bootstrap/`.

## Consequences

- ✅ Simpler setup; cost per environment still visible through tags.
- ⚠️ A too-broad IAM policy lets dev touch prod: review role scopes.
- ⚠️ Quotas are shared: reserve Lambda concurrency for prod if needed.
- ⚠️ Moving prod to its own account later means migrating (new ADR).
