# 0008. Background ZIP generation

- **Status:** Accepted
- **Date:** 2026-10-09

## Context

Organizers download all photos of an event as a ZIP (RF-ORG-14), stored in S3 and shared by a temporary signed link (RNF-SEG-05). 2,000 photos must take < 15 min (RNF-REN-06). It cannot run inside an API request: events weigh gigabytes and API Gateway cuts at 30 s.

## Options

| Option | Pro | Con |
|---|---|---|
| Lambda worker via SQS | No idle cost; SQS retries and dead-letter queue | 15-minute Lambda limit |
| ECS task on Fargate | No time limit; billed only while running | Container image and extra infrastructure |
| ZIP in the browser | No server work | Page must stay open; file not stored in S3 |

## Decision

**Lambda worker triggered by SQS.**

1. The API saves the request as *in progress* and queues a message.
2. The worker streams photos from S3 into a ZIP written back to S3 with multipart upload (nothing held in memory).
3. The worker sets *available* or *failed*; the event page shows the status and the link.

## Consequences

- ✅ The organizer can close the page; no cost when idle.
- ⚠️ Huge events may exceed 15 minutes: measure with 2,000 photos and move to Fargate (new ADR) if needed.
- ⚠️ The worker must be idempotent and respect file reuse and the generation limit (P-08).
