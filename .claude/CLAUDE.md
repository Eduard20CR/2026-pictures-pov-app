# CLAUDE.md

Pictures POV App: guests upload event photos via QR code, no account, and never see them afterwards; organizers and moderators review them, organizers download them as a ZIP.

## Commands

Same as CI (`.github/workflows/*-build-test.yaml`, `infra-check.yaml`).

| App | Check |
|---|---|
| `apps/api` (Spring Boot 4.1.1, Java 21) | `./mvnw -B verify` |
| `apps/web` (React, Vite, TS) | `npm run lint && npm test && npm run build`; after an API change, `scripts/openapi-export.sh` (starts the API, exports the contract, runs `npm run api:types`) |
| `apps/image-processor`, `apps/upload-cleanup` (Node Lambdas) | `npm run typecheck && npm test && npm run build` |
| `infra/` (Terraform) | `terraform fmt -check -recursive`; per stack `terraform init -backend=false && terraform validate` |

## Architecture rules

Full picture in `README.md`. Rules that span several documents:

- **Separate toolchains, no workspaces:** Maven (`mvnw`) for the API, npm per Node app. No `packages/shared`, no pnpm.
- **OpenAPI is the boundary:** springdoc generates it (RNF-OPE-06); openapi-typescript generates `apps/web/src/api/`. CI fails if they drift. Update `openapi.yaml` and `schema.d.ts` only by running `scripts/openapi-export.sh`, never by hand.
- **Lambda zip with SnapStart:** `run.sh` + jar with AWS Lambda Web Adapter; API Gateway calls the `live` alias (ADR 0013). Open PostgreSQL connections after the snapshot restore, never during the captured init.
- **Migrations in CI/CD, not at startup:** Liquibase changelogs in `db/`, backward compatible with the running version.
- **Photos never go through the backend:** presigned S3 uploads; S3-triggered Lambdas make thumbnails; photos and ZIPs only via temporary signed links (RNF-SEG-05).
- **Cost:** each idle environment ≤ 25 USD/month (RNF-COS-01). No services with fixed idle cost (e.g. Fargate + ALB).
- **Infrastructure:** Terraform; dev and prod in one AWS account, isolated by `<project>-<environment>-` names, the `Environment` tag, separate state keys (`app` / `data`, ADR 0019) and one IAM role per workflow per environment (ADRs 0012, 0020). CI/CD with GitHub Actions + OIDC.
- **Terraform state in S3** (ADR 0011), bucket from `infra/bootstrap/`; `s3` backend with `use_lockfile` and its own key everywhere. No committed state, no DynamoDB, no HCP Terraform.

## Documentation

- English, formal tone, short: prefer tables and bullets.
- `docs/01_functional_requirements.md`: one line per requirement (`RF-<AREA>-<NN>`), business rules (`RN-<NN>`), parameters (`P-<NN>`). `docs/02_non_functional_requirements.md`: `RNF-<CATEGORY>-<NN>`. IDs keep their Spanish codes, are never reused or renamed; removed ones are marked *Retired*.
- Acceptance criteria live only in GitHub issues, never in the requirements documents.
- Editing a requirements document bumps its version and adds a change-history row.
- ADRs: rules and template in `docs/adr/README.md`; update its index. A decision's rationale lives only in its ADR.
- `docs/c4_model.excalidraw` belongs to the user: never read or modify it. If the stack changes, update the README's Mermaid diagram and tell the user to update the C4.

## Agent skills

- **Issue tracker:** GitHub Issues via `gh`. See `docs/agents/issue-tracker.md`.
- **Triage labels:** default five roles. See `docs/agents/triage-labels.md`.
- **Domain docs:** single-context, root `GLOSSARY.md` + `docs/adr/`. See `docs/agents/domain.md`.
