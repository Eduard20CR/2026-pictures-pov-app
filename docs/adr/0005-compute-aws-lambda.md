# 0005. Backend compute on AWS Lambda

- **Status:** Accepted
- **Date:** 2026-10-09

## Context

Traffic only exists around events; the rest of the time it is near zero. The backend's compute must keep idle cost ≤ 25 USD/month (RNF-COS-01).

## Options

| Option | Pro | Con |
|---|---|---|
| AWS Lambda | Near-zero idle cost; autoscaling; nothing to start or stop | Lambda concerns: SnapStart, cold starts, connections, packaging |
| ECS on Fargate + ALB | Conventional containers | Billed continuously: breaks RNF-COS-01 |
| EC2 started per event | Simple; cheap if only run during events | Manual start/stop; forgetting it means downtime or cost |

## Decision

**AWS Lambda** behind API Gateway HTTP API: pay per invocation, no manual operation.

If Lambda makes the project or its deployment much more complex, we move to EC2 per event in a new ADR.

## Consequences

- ✅ Near-zero cost outside events; no manual steps.
- ⚠️ Evaluate Lambda's complexity in the first iterations.
