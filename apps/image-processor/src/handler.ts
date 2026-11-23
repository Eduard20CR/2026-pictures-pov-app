import type { S3Event } from "aws-lambda";
import { detectImageType } from "./fileType";
import { markReady, releasePending } from "./images";
import { deleteObject, readFirstBytes } from "./s3";

// Triggered by S3 for each new original: publishes real images, rejects anything else.
export async function handler(event: S3Event): Promise<void> {
  for (const record of event.Records) {
    await validateUpload(record.s3.bucket.name, decodeS3Key(record.s3.object.key));
  }
}

async function validateUpload(bucket: string, key: string): Promise<void> {
  const bytes = await readFirstBytes(bucket, key);
  if (detectImageType(bytes)) {
    await markReady(key);
    return;
  }

  console.warn("Not a valid image, rejecting", key);
  // Row first: if the Lambda dies before the delete, S3 retries with the file still there.
  await releasePending(key);
  // Safe to delete: upload links use If-None-Match: *, so this file never was a validated photo.
  await deleteObject(bucket, key);
}

// S3 sends the key URL-encoded, with spaces as "+".
function decodeS3Key(key: string): string {
  return decodeURIComponent(key.replace(/\+/g, " "));
}
