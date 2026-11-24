import type { S3Event } from "aws-lambda";
import { detectImageType } from "./fileType";
import { markReady, releasePending } from "./images";
import { deleteObject, putObject, readObject } from "./s3";
import { makeThumbnail } from "./thumbnail";

// Triggered by S3 for each new original: publishes real images with their thumbnail,
// rejects anything else.
export async function handler(event: S3Event): Promise<void> {
  for (const record of event.Records) {
    await processUpload(record.s3.bucket.name, decodeS3Key(record.s3.object.key));
  }
}

async function processUpload(bucket: string, key: string): Promise<void> {
  const bytes = await readObject(bucket, key);
  const type = detectImageType(bytes);
  const thumbnail = type && (await makeThumbnail(bytes, type).catch(() => null));
  if (!thumbnail) {
    await reject(bucket, key);
    return;
  }

  // Thumbnail first: a READY photo always has one. The bucket notifies only originals/.
  const thumbKey = key.replace(/^originals\//, "thumbs/") + ".webp";
  await putObject(bucket, thumbKey, thumbnail, "image/webp");
  await markReady(key, thumbKey);
}

async function reject(bucket: string, key: string): Promise<void> {
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
