import { findExpired, release } from "./images";
import { deleteObject } from "./s3";

const bucket = process.env.PHOTOS_BUCKET!;

// Scheduled: one batch per run. Uploads beyond the batch wait for the next run.
// Only image-processor publishes a photo, with its thumbnail. Whatever is still PENDING here
// never arrived or never got a thumbnail: a failed upload.
export async function handler(): Promise<void> {
  for (const image of await findExpired()) {
    // File first: if the Lambda dies before the release, the next run finds the row again.
    // S3 deletes succeed even when the upload never arrived.
    await deleteObject(bucket, image.s3Key);
    await release(image.id);
  }
}
