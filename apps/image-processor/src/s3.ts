import {
  DeleteObjectCommand,
  GetObjectCommand,
  HeadObjectCommand,
  NotFound,
  PutObjectCommand,
  S3Client,
} from "@aws-sdk/client-s3";

const s3 = new S3Client({});

// The whole original (at most P-02, 20 MB): the thumbnail needs every byte.
export async function readObject(bucket: string, key: string): Promise<Uint8Array> {
  const { Body } = await s3.send(new GetObjectCommand({ Bucket: bucket, Key: key }));
  return Body!.transformToByteArray();
}

export async function putObject(
  bucket: string, key: string, body: Uint8Array, contentType: string,
): Promise<void> {
  await s3.send(new PutObjectCommand({ Bucket: bucket, Key: key, Body: body, ContentType: contentType }));
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
