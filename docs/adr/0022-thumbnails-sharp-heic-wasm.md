# 0022. Thumbnails with sharp, HEIC through libheif in WASM

- **Status:** Accepted
- **Date:** 2026-11-24

## Context

`image-processor` makes a WebP thumbnail of each photo: upright, without metadata, at most 100 KB, in under 10 seconds (RF-SIS-02, RF-SIS-03, RNF-REN-02, RNF-REN-03). Uploads can be JPEG, PNG, WebP or HEIC. sharp's prebuilt binaries do not decode HEIC (HEVC is patent-encumbered).

## Options

| Option | Pro | Con |
|---|---|---|
| sharp, with `heic-decode` (libheif in WASM) for HEIC | Fast; EXIF orientation and metadata removal built in; WASM bundles into `index.js` | sharp is native: the zip must ship its Linux arm64 build |
| Pure JS or WASM only (Jimp, jSquash) | Plain zip | 10–30× slower, more memory; orientation and size limit written by hand |
| sharp in a Lambda layer or container image | Code zip unchanged | One more artifact to build, version and deploy |

## Decision

**sharp, with `heic-decode` for HEIC.** It meets the time limit with little code of our own, and the packaging cost is one `npm install --os=linux --cpu=arm64` in the `zip` script.

## Consequences

- ✅ A 12 MP HEIC takes about 1 s at 1024 MB; JPEG, PNG and WebP far less.
- ⚠️ The `zip` script, not `npm ci`, decides the sharp platform: the zip must be built with `npm run zip`.
- ⚠️ HEIC decoding is single-threaded WASM, the slowest path.
- ⚠️ Only `image-processor` publishes a photo, so a `READY` photo always has a thumbnail. `upload-cleanup` frees anything still `PENDING`, including a valid photo whose S3 event was lost (S3 retries twice).
