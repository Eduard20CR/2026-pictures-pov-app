# CLAUDE.md

Guidance for Claude Code in this repository.

## Status

Pictures POV App: guests upload event photos via QR code, no account, and see or delete only their own; organizers moderate and download them as a ZIP.

Documentation phase: no build, lint or tests yet. Do not invent commands; document the real ones once code exists.

## Architecture rules

Full picture in `README.md`. Rules that span several documents:

- **Two toolchains, no workspaces:** `apps/api` is Spring Boot 4.1.1 + Java 21 with Maven (`mvnw`); `apps/web` is React + Vite + TypeScript with npm. No `packages/shared`, no pnpm.
- **OpenAPI is the boundary:** springdoc generates it from the backend (RNF-OPE-06); openapi-typescript generates the frontend types in `apps/web/src/api/`. An API change means regenerating them.
- **Lambda zip with SnapStart:** the API runs on Lambda as a zip (`run.sh` + jar) with AWS Lambda Web Adapter and SnapStart; API Gateway calls the `live` alias (ADR 0013). Open PostgreSQL connections after the snapshot restore, never during the captured init.
- **Migrations in CI/CD, not at startup:** Liquibase changelogs live in `db/`; they must stay backward compatible with the running version.
- **Photos never go through the backend:** direct S3 uploads with presigned URLs; an S3-triggered Lambda makes WebP thumbnails; photos and ZIPs only via temporary signed links (RNF-SEG-05).
- **Cost:** each idle environment ≤ 25 USD/month (RNF-COS-01). No services with fixed idle cost (e.g. Fargate + ALB).
- **Infrastructure:** Terraform; dev and prod in one AWS account, isolated by `<project>-<environment>-` names, the `Environment` tag, separate state keys and one IAM role each (ADR 0012). CI/CD with GitHub Actions + OIDC.
- **Terraform state in S3** (ADR 0011), bucket created by `infra/bootstrap/`. Every configuration, the bootstrap included, uses the `s3` backend with `use_lockfile` and its own key; only the bootstrap's first run uses local state, then migrated. No state file is committed; no DynamoDB, no HCP Terraform.

## Documentation

- English, formal tone, short and clear: little text, prefer tables and bullets.
- Requirements: `docs/01_requisitos_funcionales.md` (`RF-<AREA>-<NN>`) and `docs/02_requisitos_no_funcionales.md` (`RNF-<CATEGORY>-<NN>`). IDs keep their Spanish codes, are never reused or renamed; removed ones are marked *Retired*. MoSCoW priority.
- When editing a requirements document, bump its version and add a row to its change history.
- ADRs in `docs/adr/`: follow the rules and template in `docs/adr/README.md` and update its index.
- A decision's rationale lives only in its ADR; other docs link to it.
- `docs/c4_model.excalidraw` belongs to the user: never read or modify it, by any means. If the stack changes, update the README's Mermaid diagram and tell the user to update the C4. `docs/plan_maestro.pdf` cannot be edited.
