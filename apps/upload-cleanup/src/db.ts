// Copy of apps/image-processor/src/db.ts (no shared packages): keep both in sync.
import { GetParametersByPathCommand, SSMClient } from "@aws-sdk/client-ssm";
import { Pool, type PoolConfig } from "pg";

let pool: Pool | undefined;

// Once per execution environment. DB_URL (local runs): same variables and defaults
// as the API, no SSL. Otherwise the same SSM parameters as the API.
export async function getPool(): Promise<Pool> {
  if (pool) return pool;

  const localUrl = process.env.DB_URL;
  pool = new Pool(
    localUrl
      ? {
          ...fromJdbcUrl(localUrl),
          user: process.env.DB_USERNAME ?? "app",
          password: process.env.DB_PASSWORD ?? "app",
          ssl: false,
        }
      : await fromSsm(process.env.SSM_DB_PATH!),
  );
  return pool;
}

// Lets a one-shot local run exit.
export async function closePool(): Promise<void> {
  await pool?.end();
  pool = undefined;
}

async function fromSsm(path: string): Promise<PoolConfig> {
  const ssm = new SSMClient({});
  const { Parameters = [] } = await ssm.send(
    new GetParametersByPathCommand({ Path: path, WithDecryption: true }),
  );
  const value = (name: string) =>
    Parameters.find((p) => p.Name === `${path}/${name}`)?.Value;

  return {
    ...fromJdbcUrl(value("spring.datasource.url")!),
    user: value("spring.datasource.username"),
    password: value("spring.datasource.password"),
    ssl: { rejectUnauthorized: true },
  };
}

// The API uses a JDBC URL: jdbc:postgresql://host/db?sslmode=require
function fromJdbcUrl(jdbcUrl: string): PoolConfig {
  const url = new URL(jdbcUrl.replace(/^jdbc:/, ""));
  return {
    host: url.hostname,
    port: Number(url.port || 5432),
    database: url.pathname.slice(1),
    max: 1,
  };
}
