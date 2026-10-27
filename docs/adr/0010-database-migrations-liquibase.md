# 0010. Database migrations with Liquibase in CI/CD

- **Status:** Accepted
- **Date:** 2026-10-12 (updated 2026-10-20)

## Context

The schema ([0004](0004-database-aurora-serverless-v2.md)) must evolve in a versioned way across environments. Running migrations at startup would slow Lambda cold starts (RNF-REN-04) and open connections during the SnapStart init ([0001](0001-backend-spring-boot-en-lambda.md)).

## Options

| Option | Pro | Con |
|---|---|---|
| At application startup | No extra pipeline step | Slower start; connections in the snapshot; instances fight for the lock |
| CI/CD step inside the API deploy | Fast start; a failed migration blocks the deploy | Schema and API tied together; every schema change redeploys the API |
| Standalone project with its own workflow | Fast start; schema and API deploy independently | No automatic ordering between migration and API deploy |

## Decision

**Liquibase as a standalone project in `db/`**, run by its own GitHub Actions workflow on changes to `db/**`.

- Liquibase CLI (Community edition); changelogs in YAML.
- Credentials only from environment variables (`LIQUIBASE_COMMAND_*`): GitHub secrets in CI, local defaults for Docker.
- Liquibase always runs from `db/`; the changelog path is part of each changeset's identity.

## Consequences

- ✅ Start time unaffected; schema changes deploy without rebuilding the API.
- ⚠️ A schema change the API needs ships in two PRs: migration first, then code.
- ⚠️ Migrations must stay backward compatible with the running version (expand and contract).
- ⚠️ The pipeline must reach and authenticate to the database.
- ⚠️ A paused database (0 ACU) must resume first, which slows the migration.