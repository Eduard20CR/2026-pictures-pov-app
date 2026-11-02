import { GetParametersByPathCommand, SSMClient } from "@aws-sdk/client-ssm";
import { Pool } from "pg";

let pool: Pool | undefined;

// Reads the same SSM parameters as the API, once per execution environment.
export async function getPool(): Promise<Pool> {
  if (pool) return pool;

  const path = process.env.SSM_DB_PATH!;
  const ssm = new SSMClient({});
  const { Parameters = [] } = await ssm.send(
    new GetParametersByPathCommand({ Path: path, WithDecryption: true }),
  );
  const value = (name: string) =>
    Parameters.find((p) => p.Name === `${path}/${name}`)?.Value;

  // The API stores a JDBC URL: jdbc:postgresql://host/db?sslmode=require
  const url = new URL(value("spring.datasource.url")!.replace(/^jdbc:/, ""));

  pool = new Pool({
    host: url.hostname,
    port: Number(url.port || 5432),
    database: url.pathname.slice(1),
    user: value("spring.datasource.username"),
    password: value("spring.datasource.password"),
    max: 1,
    ssl: { rejectUnauthorized: true },
  });
  return pool;
}