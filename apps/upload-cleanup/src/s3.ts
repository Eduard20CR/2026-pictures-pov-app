import { DeleteObjectCommand, GetObjectCommand, NoSuchKey, S3Client } from "@aws-sdk/client-s3";

const s3 = new S3Client({});

// Only the first 12 bytes: enough for the magic bytes. null when the upload never arrived.
export async function readFirstBytes(bucket: string, key: string): Promise<Uint8Array | null> {
  try {
    const { Body } = await s3.send(
      new GetObjectCommand({ Bucket: bucket, Key: key, Range: "bytes=0-11" }),
    );
    return await Body!.transformToByteArray();
  } catch (error) {
    if (error instanceof NoSuchKey) return null;
    throw error; // Any other error: stop and free nothing.
  }
}

export async function deleteObject(bucket: string, key: string): Promise<void> {
  await s3.send(new DeleteObjectCommand({ Bucket: bucket, Key: key }));
}
