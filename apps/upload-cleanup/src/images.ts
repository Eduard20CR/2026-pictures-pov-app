import { getPool } from "./db";

export type PendingImage = { id: string; s3Key: string };

// P-10: an authorized upload must finish within 15 minutes.
export async function findExpired(): Promise<PendingImage[]> {
    const pool = await getPool();
    const result = await pool.query(
        `SELECT id, s3_key AS "s3Key" FROM images
     WHERE status = 'PENDING' AND created_at < now() - interval '15 minutes'
     LIMIT 100`,
    );
    return result.rows;
}

export async function markReady(id: string): Promise<void> {
    const pool = await getPool();
    await pool.query(
        "UPDATE images SET status = 'READY' WHERE id = $1 AND status = 'PENDING'",
        [id],
    );
}

// One statement: delete the row and give the quota back, or do nothing.
export async function release(id: string): Promise<void> {
    const pool = await getPool();
    await pool.query(
        `WITH deleted AS (
       DELETE FROM images WHERE id = $1 AND status = 'PENDING'
       RETURNING event_id, guest_id
     )
     UPDATE guest_limits g
     SET upload_count = g.upload_count - 1, updated_at = now()
     FROM deleted d
     WHERE g.event_id = d.event_id AND g.guest_id = d.guest_id`,
        [id],
    );
}