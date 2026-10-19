# 0014. Public Aurora endpoint with Lambda outside the VPC

- **Status:** Proposed
- **Date:** 2026-10-19

## Context

The API Lambda must reach Aurora ([0004](0004-database-aurora-serverless-v2.md)) and also call S3, Cognito and other AWS APIs. A Lambda inside a VPC needs a NAT Gateway (about 32 USD/month per AZ plus data) or VPC endpoints for that, which breaks the idle cost limit (RNF-COS-01). This is a small project.

## Options

| Option | Pro | Con |
|---|---|---|
| Lambda in VPC + NAT Gateway, private Aurora | Database not reachable from the internet | Fixed NAT cost per AZ |
| Lambda in VPC + VPC endpoints, private Aurora | No NAT | Interface endpoints have a fixed hourly cost; Cognito JWKS still needs internet |
| Lambda outside VPC, public Aurora | No NAT or endpoints; free egress; CI reaches the DB directly | Database endpoint reachable from the internet |

## Decision

**Lambda outside the VPC; Aurora Serverless v2 in our own VPC, in public subnets (2 AZs), `publicly_accessible = true`.** Lowest cost. A Lambda outside a VPC has no fixed outbound IP, so the security group allows TCP 5432 from `0.0.0.0/0`. We accept this risk and protect the database with these controls:

| Area | Control |
|---|---|
| Transit | TLS required (`rds.force_ssl = 1`); clients use `sslmode=verify-full` with the AWS RDS CA |
| At rest | KMS encryption |
| Authentication | IAM database authentication preferred; otherwise a strong password in Secrets Manager with rotation, never in code, Git, Terraform variables or logs |
| Users | Least-privilege app user, separate from the Liquibase and master users |
| Application | Parameterized queries; no secrets or PII in logs; least-privilege execution role |
| Operations | Automated backups with one tested restore; CloudWatch alarms (connections, ACU, errors); budget alarm; credential revocation procedure |

## Consequences

- ✅ No NAT or endpoint cost; GitHub Actions runs `liquibase:update` directly ([0010](0010-database-migrations-liquibase.md)).
- ⚠️ The database is exposed to scans and brute force: the controls above are mandatory, not optional.
- ⚠️ The public endpoint has a public IPv4 charge (about 3.6 USD/month).
- ⚠️ Auto-pause needs minimum 0 ACU and PostgreSQL 16.3, 15.7, 14.12 or 13.15+. Any open connection, even idle, or RDS Proxy keeps it awake: HikariCP `minimumIdle=0` and no RDS Proxy.
- ⚠️ Resume takes about 15 s (30 s+ after a day paused): JDBC connect and socket timeouts above that, about 3 retries. With the API Gateway 30 s limit, the first request after a long pause may fail; accepted in dev, client retry in prod.
- ⚠️ `pg_cron` jobs do not wake the database: the expired-upload cleanup runs as a scheduled Lambda.
- ⚠️ Lambda reserved concurrency caps database connections.

## References

- [Aurora Serverless v2 automatic pause and resume](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/aurora-serverless-v2-auto-pause.html)
- [IAM database authentication](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/UsingWithRDS.IAMDBAuth.html)
- Spike #20
