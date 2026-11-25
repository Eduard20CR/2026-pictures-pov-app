# ZIP export

SQS-triggered Lambda that builds one ZIP of an event's photos ([ADR 0008](../../docs/adr/0008-background-zip-generation.md), RF-ORG-14).

- Message: `{ "exportId": "<zip_exports.id>" }`, one per invocation.
- Streams every `READY` original, hidden ones included, into `zips/<exportId>.zip` in the photos bucket (multipart upload, entries stored uncompressed, named `<imageId>.<ext>` like a single-photo download).
- `zip_exports.status`: `PENDING` → `RUNNING` → `READY` with `image_count` and `size_bytes`, or `FAILED` with `error_message`.
- Idempotent: an export already `READY` or `FAILED` is skipped; a retry overwrites the same key.
- A failure throws, so SQS retries. The last receive (`MAX_RECEIVE_COUNT`, the queue's `maxReceiveCount`) marks the export `FAILED`, then the message goes to the dead-letter queue.
- The API's request (queueing, P-07 link, P-08 limit) is #294 and #295.

| Command | Does |
|---|---|
| `npm run typecheck && npm test && npm run build` | Same checks as CI |
| `npm run local -- <exportId>` | Runs the handler for one export against the local database (below) |

## Local run

```bash
PHOTOS_BUCKET=pictures-pov-dev-photos-058264166152 npm run local -- <exportId>
```

- Needs the local database ([root README](../../README.md#getting-started)) and AWS credentials for the dev bucket in your profile.
- Same database variables and defaults as the API: `DB_URL` (`jdbc:postgresql://localhost:5432/picturepov`), `DB_USERNAME` and `DB_PASSWORD` (`app`).
- A local failure marks the export `FAILED` at once (`MAX_RECEIVE_COUNT=1`).

## Queue an export by hand (dev)

```sql
-- In the dev database: an export for an event with photos, by its organizer.
INSERT INTO zip_exports (id, event_id, requested_by)
VALUES (gen_random_uuid(), '<eventId>', '<organizerUserId>')
RETURNING id;
```

```bash
aws sqs send-message \
  --queue-url "$(terraform -chdir=infra/environments/dev/app output -raw zip_export_queue_url)" \
  --message-body '{"exportId":"<id>"}'
aws s3 ls s3://pictures-pov-dev-photos-058264166152/zips/
```
