import type { S3Event } from "aws-lambda";
import { handler } from "./handler";
import { findPendingKeys } from "./images";
import { objectExists } from "./s3";

// Local runs only: the dev bucket notifies the dev Lambda, never the local database,
// so this sends the handler the S3 event it would have received.
export async function replayPending(bucket: string): Promise<void> {
  for (const key of await findPendingKeys()) {
    if (await objectExists(bucket, key)) {
      await handler(s3Event(bucket, key));
    } else {
      console.log("Not uploaded yet, skipping", key);
    }
  }
}

// Only the fields the handler reads. S3 sends the key URL-encoded.
function s3Event(bucket: string, key: string): S3Event {
  const record = { s3: { bucket: { name: bucket }, object: { key: encodeURIComponent(key) } } };
  return { Records: [record] } as unknown as S3Event;
}
