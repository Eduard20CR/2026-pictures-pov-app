# 0021. Rejected or abandoned uploads return their quota

- **Status:** Accepted
- **Date:** 2026-11-18

## Context

Each upload takes one slot of the per-device limit (RN-01) when the presigned URL is issued. Some uploads never become photos: `image-processor` rejects files that are not images, and `upload-cleanup` frees uploads not completed within twice P-10.

## Options

| Option | Pro | Con |
|---|---|---|
| Keep the slot | Discourages repeated fake uploads | Does not stop an attacker (a new cookie gives a new quota); penalizes honest guests whose upload failed |
| Return the slot | Honest guests keep their full limit | A device can retry without limit |

## Decision

**Return the slot**, in the same statement that deletes the photo row. Keeping it would not stop abuse and would only hurt honest guests; per-device rate limiting or a WAF, still to be added, is the real defense.

## Consequences

- ✅ A failed or abandoned upload never costs the guest a photo.
- ⚠️ Until per-device rate limiting exists, only API Gateway stage throttling limits repeated uploads.
- ⚠️ Hiding or deleting a ready photo still keeps the slot (RN-03).
