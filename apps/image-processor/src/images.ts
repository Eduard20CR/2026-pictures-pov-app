import { getPool } from "./db";

// Idempotent: a second call for the same key matches no PENDING row and updates nothing.
export async function markReady(s3Key: string, thumbKey: string): Promise<void> {
  const pool = await getPool();
  const result = await pool.query(
    "UPDATE images SET status = 'READY', thumb_key = $2 WHERE s3_key = $1 AND status = 'PENDING'",
    [s3Key, thumbKey],
  );
  if (result.rowCount === 0) {
    console.warn("No PENDING image for key", s3Key);
  }
}

// Rejects an upload: deletes its PENDING row and gives the guest's quota slot back, in one
// statement so neither happens without the other.
export async function releasePending(s3Key: string): Promise<void> {
  const pool = await getPool();
  await pool.query(
    `WITH deleted AS (
       DELETE FROM images
       WHERE s3_key = $1 AND status = 'PENDING'
       RETURNING event_id, guest_id
     )
     UPDATE guest_limits g
     SET upload_count = g.upload_count - 1, updated_at = now()
     FROM deleted d
     WHERE g.event_id = d.event_id AND g.guest_id = d.guest_id`,
    [s3Key],
  );
}

// Local runs only (src/replayPending.ts).
export async function findPendingKeys(): Promise<string[]> {
  const pool = await getPool();
  const result = await pool.query("SELECT s3_key FROM images WHERE status = 'PENDING'");
  return result.rows.map((row) => row.s3_key);
}
