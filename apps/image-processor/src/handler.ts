import type { S3Event } from "aws-lambda";
import { detectImageType } from "./fileType";
import { deleteImageRow, markReady } from "./images";
import { deleteObject, readFirstBytes } from "./s3";

export async function handler(event: S3Event): Promise<void> {
  for (const record of event.Records) {
    const bucket = record.s3.bucket.name;
    // S3 sends the key URL-encoded, with spaces as "+".
    const key = decodeURIComponent(record.s3.object.key.replace(/\+/g, " "));

    const type = detectImageType(await readFirstBytes(bucket, key));
    if (type) {
      await markReady(key);
    } else {
      console.warn("Not a valid image, rejecting", key);
      await deleteImageRow(key);
      await deleteObject(bucket, key);
    }
  }
}