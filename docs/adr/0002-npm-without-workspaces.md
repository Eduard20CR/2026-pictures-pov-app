# 0002. npm without workspaces

- **Status:** Accepted
- **Date:** 2026-10-08

## Context

The repo was planned as a pnpm monorepo with a `packages/shared` package for the OpenAPI types. After [0001](0001-backend-spring-boot-en-lambda.md), the backend is Java, so `apps/web` is the only JavaScript project and the only consumer of those types.

## Options

| Option | Pro | Con |
|---|---|---|
| pnpm + workspaces + `packages/shared` | Easy to share code with future JS projects | Extra tool and config with no value for a single project |
| npm without workspaces | Bundled with Node.js; less config; types next to their only consumer | Must be revisited if another JS project appears |

## Decision

**npm without workspaces.** `packages/shared` is removed; openapi-typescript generates the API types in `apps/web/src/api/`.

## Consequences

- ✅ Simpler repo and CI configuration.
- ⚠️ Types must be regenerated whenever the OpenAPI contract changes; CI should detect drift.
