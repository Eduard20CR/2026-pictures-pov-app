# Image processor

Lambda triggered by S3 for each new original. Reads its first bytes: a real image (JPEG, PNG, WebP, HEIC) becomes `READY`; anything else loses its row, its quota slot ([ADR 0021](../../docs/adr/0021-rejected-upload-returns-quota.md)) and its file.

| Command | Does |
|---|---|
| `npm run typecheck && npm test && npm run build` | Same checks as CI |
| `npm run local` | Runs the handler against the local database (below) |

## Local run

Local uploads land in the dev bucket, which notifies the dev Lambda and the dev database, so local rows stay `PENDING`. After uploading from the local web:

```bash
PHOTOS_BUCKET=pictures-pov-dev-photos-058264166152 npm run local
```

- Calls the same handler once per local `PENDING` row whose file is in the bucket, then exits. Rows without a file yet are skipped.
- Needs the local database ([root README](../../README.md#getting-started)) and AWS credentials for the dev bucket in your profile.
- Same variables and defaults as the API: `DB_URL` (`jdbc:postgresql://localhost:5432/picturepov`), `DB_USERNAME` and `DB_PASSWORD` (`app`). The bucket is in `apps/api/src/main/resources/application-local.properties`.
- One pass only: run it again after each new upload.
