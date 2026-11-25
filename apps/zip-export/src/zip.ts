import { GetObjectCommand, S3Client } from "@aws-sdk/client-s3";
import { Upload } from "@aws-sdk/lib-storage";
import { ZipArchive } from "archiver";
import { once } from "node:events";
import type { Readable } from "node:stream";

export type Original = { id: string; s3Key: string; contentType: string };

const s3 = new S3Client({});

// Copy of apps/api/.../image/ImageTypes.java (no shared packages): keep both in sync.
// Same names as a single-photo download: originals are stored without an extension.
const extensions: Record<string, string> = {
  "image/jpeg": ".jpg",
  "image/png": ".png",
  "image/webp": ".webp",
  "image/heic": ".heic",
};

// Streams each original from S3 into one ZIP uploaded to S3 in parts: nothing is held in memory
// beyond the upload's part buffers. Photos are already compressed, so entries are stored as is.
// Returns the ZIP size in bytes.
export async function writeZip(bucket: string, key: string, originals: Original[]): Promise<number> {
  const archive = new ZipArchive({ store: true });
  const upload = new Upload({
    client: s3,
    params: { Bucket: bucket, Key: key, Body: archive, ContentType: "application/zip" },
  });
  const uploaded = upload.done();
  // The archive or the upload may stop midway; either fails the export at once instead of waiting for the timeout.
  const failed = Promise.race([
    once(archive, "error").then(([error]) => { throw error; }),
    uploaded.then(() => new Promise<never>(() => {})),
  ]);
  failed.catch(() => {}); // Raised through the races below.

  try {
    for (const original of originals) {
      // One original at a time: the next GetObject starts only once this entry is written.
      const { Body } = await s3.send(new GetObjectCommand({ Bucket: bucket, Key: original.s3Key }));
      const written = once(archive, "entry");
      archive.append(Body as Readable, { name: original.id + (extensions[original.contentType] ?? "") });
      await Promise.race([written, failed]);
    }
    await Promise.race([archive.finalize(), failed]);
    await Promise.race([uploaded, failed]);
  } catch (error) {
    archive.abort();
    await upload.abort().catch(() => {});
    await uploaded.catch(() => {}); // Rejects once aborted.
    throw error;
  }
  return archive.pointer();
}
