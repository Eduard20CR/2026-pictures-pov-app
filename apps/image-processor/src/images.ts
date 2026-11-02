import { getPool } from "./db";

// Idempotent: a second call for the same key matches no PENDING row and updates nothing.
export async function markReady(s3Key: string): Promise<void> {
  const pool = await getPool();
  const result = await pool.query(
    "UPDATE images SET status = 'READY' WHERE s3_key = $1 AND status = 'PENDING'",
    [s3Key],
  );
  if (result.rowCount === 0) {
    console.warn("No PENDING image for key", s3Key);
  }
}