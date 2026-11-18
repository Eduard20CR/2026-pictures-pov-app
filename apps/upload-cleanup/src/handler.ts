import { DeleteObjectCommand, GetObjectCommand, NoSuchKey, S3Client } from "@aws-sdk/client-s3";
import { detectImageType } from "./fileType";
import { findExpired, markReady, release, type PendingImage } from "./images";

const s3 = new S3Client({});
const bucket = process.env.PHOTOS_BUCKET!;

// Only the first 12 bytes: enough for the magic bytes. null when the upload never arrived.
async function readFirstBytes(key: string): Promise<Uint8Array | null> {
    try {
        const { Body } = await s3.send(
            new GetObjectCommand({ Bucket: bucket, Key: key, Range: "bytes=0-11" }),
        );
        return await Body!.transformToByteArray();
    } catch (error) {
        if (error instanceof NoSuchKey) return null;
        throw error; // Any other error: stop and free nothing.
    }
}

// Drains every batch: each image leaves PENDING, so batches shrink.
export async function handler(): Promise<void> {
    for (let batch = await findExpired(); batch.length > 0; batch = await findExpired()) {
        await publishOrRelease(batch);
    }
}

async function publishOrRelease(batch: PendingImage[]): Promise<void> {
    for (const image of batch) {
        const bytes = await readFirstBytes(image.s3Key);
        if (bytes && detectImageType(bytes)) {
            await markReady(image.id); // The S3 event was lost.
            continue;
        }
        await release(image.id);
        if (bytes) {
            console.warn("Not a valid image, rejecting", image.s3Key);
            await s3.send(new DeleteObjectCommand({ Bucket: bucket, Key: image.s3Key }));
        }
    }
}
