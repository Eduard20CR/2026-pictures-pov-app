# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository status

Pictures POV App: web application where event guests upload photos to a shared gallery by scanning a QR code, without creating an account; organizers moderate them and download them as a ZIP.

The project is in the documentation phase. `apps/` and `infra/` exist but are empty: there is no code, build, lint or tests yet. Do not invent commands; once code is added, document the real ones here.

## Planned architecture

The full picture (diagram, stack table, folder tree) is in `README.md`. Points that require reading several documents together:

- **Two independent toolchains, no workspaces.** `apps/api` is Spring Boot 3 + Java 21 with Gradle; `apps/web` is React + Vite + TypeScript with npm. There is no `packages/shared` and no pnpm.
- **The OpenAPI contract is the boundary between them.** The backend generates it from the code with springdoc-openapi (RNF-OPE-06) and the frontend generates its types with openapi-typescript in `apps/web/src/api/`. An API change means regenerating those types.
- **Backend on AWS Lambda with SnapStart** behind API Gateway HTTP API (via `aws-serverless-java-container-springboot3`). PostgreSQL connections must be opened after the snapshot is restored, never during the captured initialization.
- **Photos never go through the backend.** The browser uploads directly to S3 with presigned URLs; an S3-triggered Lambda generates WebP thumbnails. Photos and ZIPs are only exposed through temporary signed links (RNF-SEG-05).
- **Dominant cost constraint:** each environment with no traffic must cost ≤ 25 USD/month (RNF-COS-01). This rules out services with a fixed idle cost (e.g. Fargate + ALB).
- Infrastructure with Terraform, dev/prod environments in separate AWS accounts; CI/CD with GitHub Actions + OIDC.

## Documentation

All documentation is written in English, in a formal tone.

- `docs/01_requisitos_funcionales.md` (`RF-<AREA>-<NN>`) and `docs/02_requisitos_no_funcionales.md` (`RNF-<CATEGORY>-<NN>`). Identifiers are stable and keep their original Spanish codes (RF, RNF, RN, P, and areas such as `INV`, `SIS`, `SEG`): they are never reused or renamed, and removed ones are marked *Retired*. MoSCoW priority (M/S/C/W).
- Each requirements document has a metadata table (version, status, date) and a change history: when modifying it, bump the version and add a row to the history.
- `docs/adr/` uses MADR with `NNNN-title.md` numbering; the index, conventions and template are in `docs/adr/README.md`. When creating an ADR, update that index.
- **The rationale for a decision lives only in its ADR**; the README and other documents link to it instead of repeating it. An accepted ADR is not rewritten: a new one is created to supersede it.
- `docs/c4_model.excalidraw` is the C4 diagram and is maintained only by the user: never read or modify it by any means, including Bash or scripts. If the stack changes, update the Mermaid diagram in the README and tell the user the C4 diagram needs updating. `docs/plan_maestro.pdf` cannot be edited from the repo.

