# Upload cleanup

Scheduled Lambda that frees uploads still `PENDING` after twice P-10 (`UPLOAD_TTL_MINUTES`). Only `image-processor` publishes a photo, with its thumbnail, so these uploads failed: the file never arrived, or was never processed.

- Deletes the file (if any), then the row, and returns the quota slot ([ADR 0021](../../docs/adr/0021-rejected-upload-returns-quota.md)).

| Command | Does |
|---|---|
| `npm run typecheck && npm test && npm run build` | Same checks as CI |
| `npm run local` | Runs the handler against the local database (below) |

## Local run

```bash
PHOTOS_BUCKET=pictures-pov-dev-photos-058264166152 npm run local
```

- Calls the same handler once, then exits. Only uploads older than twice P-10 are touched, so one still in progress is never released.
- Needs the local database ([root README](../../README.md#getting-started)) and AWS credentials for the dev bucket in your profile.
- Same variables and defaults as the API: `DB_URL` (`jdbc:postgresql://localhost:5432/picturepov`), `DB_USERNAME` and `DB_PASSWORD` (`app`). `UPLOAD_TTL_MINUTES` defaults to 15, as in Terraform. The bucket is in `apps/api/src/main/resources/application-local.properties`.
- To test it: request an upload, do not finish it, wait 30 minutes and run the command.
