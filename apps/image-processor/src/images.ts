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

// Deletes a PENDING row and returns its quota slot. Returns false when a validated
// (READY or HIDDEN) photo holds the key, so the caller must keep the file.
export async function deleteImageRow(s3Key: string): Promise<boolean> {
  const pool = await getPool();
  const result = await pool.query(
    `WITH deleted AS (
       DELETE FROM images WHERE s3_key = $1 AND status = 'PENDING'
       RETURNING event_id, guest_id
     ), released AS (
       UPDATE guest_limits g
       SET upload_count = g.upload_count - 1, updated_at = now()
       FROM deleted d
       WHERE g.event_id = d.event_id AND g.guest_id = d.guest_id
     )
     -- "released" runs although unreferenced (data-modifying CTE). This SELECT sees the
     -- rows from before the DELETE, hence the status filter.
     SELECT NOT EXISTS (
       SELECT 1 FROM images WHERE s3_key = $1 AND status <> 'PENDING'
     ) AS file_unclaimed`,
    [s3Key],
  );
  return result.rows[0].file_unclaimed;
}
