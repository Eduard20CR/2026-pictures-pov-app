import {
  DeleteObjectCommand,
  GetObjectCommand,
  HeadObjectCommand,
  NotFound,
  S3Client,
} from "@aws-sdk/client-s3";

const s3 = new S3Client({});

// Only the first 12 bytes: enough for the magic bytes, no need to download the photo.
export async function readFirstBytes(bucket: string, key: string): Promise<Uint8Array> {
  const { Body } = await s3.send(
    new GetObjectCommand({ Bucket: bucket, Key: key, Range: "bytes=0-11" }),
  );
  return Body!.transformToByteArray();
}

export async function deleteObject(bucket: string, key: string): Promise<void> {
  await s3.send(new DeleteObjectCommand({ Bucket: bucket, Key: key }));
}

// Local runs only (src/replayPending.ts).
export async function objectExists(bucket: string, key: string): Promise<boolean> {
  try {
    await s3.send(new HeadObjectCommand({ Bucket: bucket, Key: key }));
    return true;
  } catch (error) {
    if (error instanceof NotFound) return false;
    throw error;
  }
}
