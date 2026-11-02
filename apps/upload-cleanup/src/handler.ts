import { HeadObjectCommand, NotFound, S3Client } from "@aws-sdk/client-s3";
import { findExpired, markReady, release } from "./images";

const s3 = new S3Client({});
const bucket = process.env.PHOTOS_BUCKET!;

async function existsInS3(key: string): Promise<boolean> {
    try {
        await s3.send(new HeadObjectCommand({ Bucket: bucket, Key: key }));
        return true;
    } catch (error) {
        if (error instanceof NotFound) return false;
        throw error; // Any other error: stop and free nothing.
    }
}

export async function handler(): Promise<void> {
    for (const image of await findExpired()) {
        if (await existsInS3(image.s3Key)) {
            await markReady(image.id); // The S3 event was lost.
        } else {
            await release(image.id);
        }
    }
}