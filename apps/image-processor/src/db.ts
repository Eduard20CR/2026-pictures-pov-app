// Copied in apps/upload-cleanup/src/db.ts and apps/zip-export/src/db.ts (no shared packages): keep all in sync.
import { GetParametersByPathCommand, SSMClient } from "@aws-sdk/client-ssm";
import { Pool, type PoolConfig } from "pg";

let pool: Pool | undefined;

// One pool per execution environment, with the same settings as the API:
// DB_URL for local runs, the SSM parameters in AWS.
export async function getPool(): Promise<Pool> {
  if (!pool) {
    const localUrl = process.env.DB_URL;
    const config = localUrl ? localConfig(localUrl) : await ssmConfig(process.env.SSM_DB_PATH!);
    pool = new Pool({ ...config, max: 1 });
  }
  return pool;
}

// Lets a one-shot local run exit.
export async function closePool(): Promise<void> {
  await pool?.end();
  pool = undefined;
}

// Same defaults as the API's application.properties; Docker Postgres has no TLS.
function localConfig(jdbcUrl: string): PoolConfig {
  return {
    ...parseJdbcUrl(jdbcUrl),
    user: process.env.DB_USERNAME ?? "app",
    password: process.env.DB_PASSWORD ?? "app",
    ssl: false,
  };
}

async function ssmConfig(path: string): Promise<PoolConfig> {
  const { Parameters = [] } = await new SSMClient({}).send(
    new GetParametersByPathCommand({ Path: path, WithDecryption: true }),
  );
  const param = (name: string) => Parameters.find((p) => p.Name === `${path}/${name}`)?.Value;

  return {
    ...parseJdbcUrl(param("spring.datasource.url")!),
    user: param("spring.datasource.username"),
    password: param("spring.datasource.password"),
    ssl: { rejectUnauthorized: true },
  };
}

// The API uses a JDBC URL: jdbc:postgresql://host:port/db?sslmode=require
function parseJdbcUrl(jdbcUrl: string): PoolConfig {
  const url = new URL(jdbcUrl.replace(/^jdbc:/, ""));
  return {
    host: url.hostname,
    port: Number(url.port || 5432),
    database: url.pathname.slice(1),
  };
}
