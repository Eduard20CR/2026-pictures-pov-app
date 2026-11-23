import type { S3Event } from "aws-lambda";
import { handler } from "./handler";
import { findPendingKeys } from "./images";
import { objectExists } from "./s3";

// Local runs only: replays the S3 event the dev bucket sent to the dev Lambda instead.
export async function processPending(bucket: string): Promise<void> {
  for (const key of await findPendingKeys()) {
    if (!(await objectExists(bucket, key))) {
      console.log("Not uploaded yet, skipping", key);
      continue;
    }
    await handler(s3Event(bucket, key));
  }
}

// S3 sends the key URL-encoded; the handler decodes it.
function s3Event(bucket: string, key: string): S3Event {
  return {
    Records: [{ s3: { bucket: { name: bucket }, object: { key: encodeURIComponent(key) } } }],
  } as unknown as S3Event;
}
