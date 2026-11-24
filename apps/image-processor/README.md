# Image processor

Lambda triggered by S3 for each new original in `originals/`. Checks its first bytes, then makes its thumbnail ([ADR 0022](../../docs/adr/0022-thumbnails-sharp-heic-wasm.md)):

- A real image (JPEG, PNG, WebP, HEIC) gets `thumbs/<event>/<image>.webp`: at most 400 px and 100 KB, upright, without metadata. Then its row becomes `READY` with `thumb_key`.
- Anything else, or an image that cannot be decoded, loses its row, its quota slot ([ADR 0021](../../docs/adr/0021-rejected-upload-returns-quota.md)) and its file.

| Command | Does |
|---|---|
| `npm run typecheck && npm test && npm run build` | Same checks as CI |
| `npm run zip` | Builds `dist/function.zip` with sharp for Linux arm64 (after `npm run build`) |
| `npm run local` | Runs the handler against the local database (below) |

## Local run

Local uploads land in the dev bucket, which notifies the dev Lambda and the dev database, so local rows stay `PENDING`. After uploading from the local web:

```bash
PHOTOS_BUCKET=pictures-pov-dev-photos-058264166152 npm run local
```

- Calls the same handler once per local `PENDING` row whose file is in the bucket, then exits. Rows without a file yet are skipped. The thumbnail is written to the dev bucket.
- Needs the local database ([root README](../../README.md#getting-started)) and AWS credentials for the dev bucket in your profile.
- Same variables and defaults as the API: `DB_URL` (`jdbc:postgresql://localhost:5432/picturepov`), `DB_USERNAME` and `DB_PASSWORD` (`app`). The bucket is in `apps/api/src/main/resources/application-local.properties`.
- One pass only: run it again after each new upload.
