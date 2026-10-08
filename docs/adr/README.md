# Architecture Decision Records (ADR)

This directory contains the project's relevant architecture decisions in [MADR](https://adr.github.io/madr/) format. Each record documents the context, the options considered, the decision made and its consequences.

## Index

| Number | Title | Status | Date |
|---|---|---|---|
| [0001](0001-backend-spring-boot-en-lambda.md) | Spring Boot backend on AWS Lambda | Accepted | 2026-10-08 |
| [0002](0002-npm-sin-workspaces.md) | npm without workspaces | Accepted | 2026-10-08 |

## Conventions

- **File name.** `NNNN-lowercase-title-with-hyphens.md`, with sequential four-digit numbering. A number is never reused.
- **Statuses.** Proposed, Accepted, Rejected, Deprecated or Superseded by `NNNN`.
- **Immutability.** An accepted ADR is not substantially modified. If the decision changes, a new ADR is created and the previous one moves to *Superseded by `NNNN`*.
- **Single source.** The rationale for each decision lives only in its ADR; the rest of the documentation links to it.

## Template

```markdown
# NNNN. Decision title

- **Status:** Proposed | Accepted | Rejected | Deprecated | Superseded by NNNN
- **Date:** YYYY-MM-DD

## Context and problem statement

Situation that motivates the decision and applicable constraints.

## Decision drivers

- Driver 1.
- Driver 2.

## Considered options

1. Option A.
2. Option B.

## Decision

Chosen option and main reason.

## Pros and cons of the options

### Option A

- Pro: …
- Con: …

## Consequences

### Positive

- …

### Negative and risks

- …
```
