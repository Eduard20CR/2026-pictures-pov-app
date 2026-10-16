# 0004. Aurora Serverless v2 database

- **Status:** Accepted
- **Date:** 2026-10-09

## Context

Events, uploads and moderation data are relational. The database must keep idle cost ≤ 25 USD/month (RNF-COS-01), and we want to learn SQL in depth.

## Options

| Option | Pro | Con |
|---|---|---|
| PostgreSQL on RDS | Simple, well known | Billed continuously, even idle |
| Aurora Serverless v2 (PostgreSQL) | Pauses at 0 ACU: only storage billed when idle; autoscaling | First request after a pause waits for resume |
| DynamoDB | Pay per request; no connections | Not relational; misses the learning goal |

## Decision

**Aurora Serverless v2 with PostgreSQL**, minimum capacity 0 ACU so it pauses when idle. No compute cost when idle, and it is real PostgreSQL.

## Consequences

- ✅ No idle compute cost; experience with SQL and PostgreSQL.
- ⚠️ Resuming takes seconds and can break RNF-REN-04: tune the pause timeout per environment.
- ⚠️ Keep the connection pool small ([0013](0013-lambda-zip-snapstart-web-adapter.md)).
