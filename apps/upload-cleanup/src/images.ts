import { getPool } from "./db";

export type PendingImage = { id: string; s3Key: string };

// P-10, set in Terraform: how long an upload authorization lasts.
const uploadTtlMinutes = Number(process.env.UPLOAD_TTL_MINUTES!);

// Waits twice P-10: S3 accepts a PUT started just before expiry, so it may land later.
export async function findExpired(): Promise<PendingImage[]> {
    const pool = await getPool();
    const result = await pool.query(
        `SELECT id, s3_key AS "s3Key" FROM images
     WHERE status = 'PENDING' AND created_at < now() - make_interval(mins => 2 * $1::int)
     LIMIT 100`,
        [uploadTtlMinutes],
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