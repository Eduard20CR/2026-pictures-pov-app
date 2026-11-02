import type { S3Event } from "aws-lambda";
import { markReady } from "./images";

export async function handler(event: S3Event): Promise<void> {
  for (const record of event.Records) {
    // S3 sends the key URL-encoded, with spaces as "+".
    const key = decodeURIComponent(record.s3.object.key.replace(/\+/g, " "));
    await markReady(key);
  }
}
