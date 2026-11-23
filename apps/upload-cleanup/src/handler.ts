import { detectImageType } from "./fileType";
import { findExpired, markReady, release, type PendingImage } from "./images";
import { deleteObject, readFirstBytes } from "./s3";

const bucket = process.env.PHOTOS_BUCKET!;

// Scheduled: one batch per run. Uploads beyond the batch wait for the next run.
export async function handler(): Promise<void> {
  for (const image of await findExpired()) {
    await settle(image);
  }
}

async function settle(image: PendingImage): Promise<void> {
  const bytes = await readFirstBytes(bucket, image.s3Key);

  if (bytes === null) {
    await release(image.id); // The upload never reached S3.
    return;
  }
  if (detectImageType(bytes)) {
    await markReady(image.id); // The file arrived but its S3 event was lost.
    return;
  }

  console.warn("Not a valid image, rejecting", image.s3Key);
  await release(image.id); // Row first, as in image-processor.
  await deleteObject(bucket, image.s3Key);
}
