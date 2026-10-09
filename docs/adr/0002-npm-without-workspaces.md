# 0002. npm without workspaces

- **Status:** Accepted
- **Date:** 2026-10-08

## Context and problem statement

The repository was planned as a monorepo managed with pnpm and workspaces, with a `packages/shared` package containing the types generated from the OpenAPI contract so they could be shared between the frontend and the backend.

As a result of [decision 0001](0001-backend-spring-boot-en-lambda.md), the backend is now developed with Spring Boot and built with Gradle. Consequently, `apps/web` becomes the only JavaScript project in the repository and the backend no longer consumes TypeScript types.

## Decision drivers

- Number of JavaScript projects in the repository.
- Simplicity of the local and CI configuration.
- Keeping a single place where the API client types are generated.

## Considered options

1. Keep pnpm with workspaces and the `packages/shared` package.
2. Use npm without workspaces and generate the types inside `apps/web`.

## Decision

**Option 2 is chosen: npm without workspaces.**

- `apps/web` is managed with npm.
- The `packages/shared` package is removed.
- The API client types are generated with openapi-typescript from the OpenAPI contract and placed in `apps/web/src/api/`.

## Pros and cons of the options

### Option 1: pnpm with workspaces and `packages/shared`

- Pro: makes it easier to share code if more JavaScript projects are added in the future.
- Con: adds a tool and a workspace configuration that bring no value with a single JavaScript project; the shared package has no consumer other than the frontend.

### Option 2: npm without workspaces

- Pro: uses the package manager bundled with Node.js, with no additional installation; reduces the repository and CI configuration; the types live next to the only code that consumes them.
- Con: if another JavaScript project that needs the same types is added in the future, the structure will need to be reconsidered.

## Consequences

### Positive

- The repository and CI pipeline configuration for the frontend is simpler.
- The API types are kept next to the code that uses them.

### Negative and risks

- The types generated in `apps/web/src/api/` must be regenerated every time the OpenAPI contract produced by the backend changes; the CI process must detect mismatches between the two.
- Adding a new JavaScript project would require revisiting this decision.

## Related decisions

- [0001. Spring Boot backend on AWS Lambda](0001-backend-spring-boot-en-lambda.md)
