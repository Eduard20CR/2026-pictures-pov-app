# 0008. Background ZIP generation

- **Status:** Accepted
- **Date:** 2026-10-09

## Context and problem statement

The organizer must be able to download all of an event's photos in a ZIP file (RF-ORG-14). The file is generated in a background process, stored in Amazon S3 and offered through a signed, temporary link on the event page (RNF-SEG-05). Generating the ZIP for an event of 2,000 photos must take less than 15 minutes (RNF-REN-06).

Generating the file within the API request is not viable: an event can contain several gigabytes of photos, and API Gateway limits each request to 30 seconds.

## Decision drivers

- Generation time compatible with RNF-REN-06.
- Near-zero cost when no ZIP is being generated (RNF-COS-01).
- Consistency with the serverless architecture ([decision 0005](0005-compute-aws-lambda.md)).

## Considered options

1. Serverless worker: AWS Lambda triggered through an Amazon SQS queue.
2. Script run as an on-demand Amazon ECS task on AWS Fargate.
3. ZIP generation in the browser.

## Decision

**Option 1 is chosen: a serverless worker on AWS Lambda triggered through Amazon SQS.**

1. The API registers the request with status *in progress* and sends a message to the queue.
2. The worker reads the photos from S3 and writes the ZIP back to S3 as a stream, using a multipart upload, so the full file is never held in memory or on disk.
3. When it finishes, the worker updates the status to *available* or *failed*; the event page shows the status and, when available, the signed link.

The main reason is that it has no cost when no ZIP is being generated and reuses the same serverless model as the rest of the backend.

## Pros and cons of the options

### Option 1: Lambda with SQS

- Pro: no idle cost; no servers to operate; SQS provides retries and a dead-letter queue for failed jobs.
- Con: Lambda has a maximum execution time of 15 minutes, which limits the size of the events it can handle.

### Option 2: Script as an ECS task on Fargate

- Pro: no execution time limit; billed only while the task runs, with no load balancer.
- Con: requires a container image and additional infrastructure to launch and monitor the task.

### Option 3: Generation in the browser

- Pro: no server-side processing.
- Con: the organizer must keep the page open and download every photo, and the file is not stored in S3, as required by RF-ORG-14.

## Consequences

### Positive

- The organizer does not need to keep the page open while the ZIP is generated.
- ZIP generation has no cost when it is not in use.

### Negative and risks

- **Execution time limit.** Very large events could exceed Lambda's 15-minute limit. The generation time must be measured with an event of 2,000 photos; if the limit is not met, the worker will move to option 2, recorded in a new ADR.
- **Duplicate requests.** The worker must be idempotent, and requests must respect the reuse of the existing file and the generation limit (P-08) defined in RF-ORG-14.

## Related decisions

- [0005. Backend compute on AWS Lambda](0005-compute-aws-lambda.md)
- [0004. Aurora Serverless v2 database](0004-database-aurora-serverless-v2.md)
