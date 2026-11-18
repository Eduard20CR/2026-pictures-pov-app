# 0019. Terraform state split into app and data per environment

- **Status:** Accepted
- **Date:** 2026-11-18

## Context

[0011](0011-terraform-state-amazon-s3.md) gave each environment one state key. With one state, destroying or recreating the application stack can also destroy the database.

## Options

| Option | Pro | Con |
|---|---|---|
| One state per environment | One `init`/`apply` per environment | `terraform destroy` reaches the database |
| Split `app` / `data` per environment | Destroying the app stack never touches the database | Two configurations per environment |

## Decision

**Split `app` / `data` per environment**, so that destroying the app stack never destroys the database. This replaces the environment rows of 0011's key table:

| Configuration | Folder | Key |
|---|---|---|
| Bootstrap | `infra/bootstrap/` | `bootstrap/terraform.tfstate` |
| `<env>` app | `infra/environments/<env>/app/` | `<env>/app/terraform.tfstate` |
| `<env>` data | `infra/environments/<env>/data/` | `<env>/data/terraform.tfstate` |

## Consequences

- ✅ The database survives any change or destroy of the app stack.
- ⚠️ `app` does not read `data` outputs; it finds the database through SSM ([0015](0015-dev-database-neon-credentials-ssm.md)).
- ⚠️ dev's `data` stack is empty because dev uses Neon (0015); prod's `data` stack will hold Aurora.
