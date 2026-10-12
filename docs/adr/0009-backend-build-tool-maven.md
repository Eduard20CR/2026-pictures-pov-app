# 0009. Maven as the backend build tool

- **Status:** Accepted
- **Date:** 2026-10-12

## Context and problem statement

[Decision 0001](0001-backend-spring-boot-en-lambda.md) established that `apps/api` would be built with Gradle. Before any backend code was written, the build tool was reconsidered, and the initial Spring Boot project was generated with Maven and the Maven Wrapper.

This decision supersedes only the build tool defined in decision 0001; the rest of that decision remains in force.

## Decision drivers

- The developer's familiarity and comfort with the tool.
- Adoption of the tool in the Java ecosystem and in the job market.
- Native support from Spring Boot and from the libraries used by the backend.

## Considered options

1. Gradle.
2. Maven.

## Decision

**Option 2 is chosen: Maven.**

- `apps/api` is built with Maven through the Maven Wrapper (`mvnw`), so no local Maven installation is required.
- The project is described in `apps/api/pom.xml`.

The main reasons are that the developer is more comfortable working with Maven and that Maven is the most widely adopted build tool for server-side Java projects, which makes it the industry standard in that context.

## Pros and cons of the options

### Option 1: Gradle

- Pro: flexible build scripts and faster incremental builds thanks to its build cache.
- Con: the developer is less comfortable with it, and its scripts are harder to read and maintain than a declarative configuration.

### Option 2: Maven

- Pro: the developer is more comfortable with it; it is the most widely adopted build tool for server-side Java; its declarative, convention-based configuration is predictable and well supported by Spring Boot, IDEs and CI tools.
- Con: builds are generally slower than with Gradle, and customizing the build is less flexible.

## Consequences

### Positive

- The backend is built with the tool the developer knows best, which reduces friction during development.
- The project follows the most common convention in server-side Java projects.

### Negative and risks

- **Build time.** Builds in CI may be slower than with Gradle; caching the local Maven repository in the pipeline mitigates this.
- **Two toolchains in CI.** The continuous integration pipeline must handle Java 21 with Maven for `apps/api` and Node.js with npm for `apps/web`.

## Related decisions

- [0001. Spring Boot backend on AWS Lambda](0001-backend-spring-boot-en-lambda.md)
