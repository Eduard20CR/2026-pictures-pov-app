import { getPool } from "./db";
import type { Original } from "./zip";

// zip_exports.status: PENDING (queued) → RUNNING → READY (available) or FAILED.

// Starts the export, or returns null once it is available or failed.
// RUNNING is claimed again: a retry follows a run that died midway.
export async function claim(id: string): Promise<{ eventId: string } | null> {
  const pool = await getPool();
  const result = await pool.query(
    `UPDATE zip_exports
     SET status = 'RUNNING'
     WHERE id = $1 AND status IN ('PENDING', 'RUNNING')
     RETURNING event_id AS "eventId"`,
    [id],
  );
  return result.rows[0] ?? null;
}

// Every processed original, hidden ones included (RF-ORG-14).
export async function findOriginals(eventId: string): Promise<Original[]> {
  const pool = await getPool();
  const result = await pool.query(
    `SELECT id, s3_key AS "s3Key", content_type AS "contentType"
     FROM images
     WHERE event_id = $1 AND status = 'READY'
     ORDER BY created_at, id`,
    [eventId],
  );
  return result.rows;
}

export async function markAvailable(id: string, s3Key: string, imageCount: number, sizeBytes: number): Promise<void> {
  const pool = await getPool();
  await pool.query(
    `UPDATE zip_exports
     SET status = 'READY', s3_key = $2, image_count = $3, size_bytes = $4,
         error_message = NULL, completed_at = now()
     WHERE id = $1 AND status = 'RUNNING'`,
    [id, s3Key, imageCount, sizeBytes],
  );
}

// PENDING too: the last receive may fail before the export started.
export async function markFailed(id: string, reason: string): Promise<void> {
  const pool = await getPool();
  await pool.query(
    `UPDATE zip_exports
     SET status = 'FAILED', error_message = left($2, 1000), completed_at = now()
     WHERE id = $1 AND status IN ('PENDING', 'RUNNING')`,
    [id, reason],
  );
}
