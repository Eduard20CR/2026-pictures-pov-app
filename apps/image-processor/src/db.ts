import { Pool } from "pg";

// Created once per execution environment and reused across invocations.
export const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  max: 1,
  ssl: { rejectUnauthorized: true },
});
