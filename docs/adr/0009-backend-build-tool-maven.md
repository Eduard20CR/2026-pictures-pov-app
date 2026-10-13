# 0009. Maven as the backend build tool

- **Status:** Accepted
- **Date:** 2026-10-12

## Context

[0001](0001-backend-spring-boot-en-lambda.md) chose Gradle for `apps/api`. Before writing code, the project was generated with Maven instead. This ADR supersedes only the build tool of 0001.

## Options

| Option | Pro | Con |
|---|---|---|
| Gradle | Flexible; faster incremental builds | Less familiar; scripts harder to read |
| Maven | Familiar; most used in server-side Java; declarative and predictable | Slower builds; less flexible |

## Decision

**Maven**, through the Maven Wrapper (`mvnw`), with `apps/api/pom.xml`. The developer knows it best and it is the industry standard.

## Consequences

- ✅ Less friction; follows the common Java convention.
- ⚠️ Slower CI builds: cache the local Maven repository.
