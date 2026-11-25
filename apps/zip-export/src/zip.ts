import { GetObjectCommand, S3Client } from "@aws-sdk/client-s3";
import { Upload } from "@aws-sdk/lib-storage";
import { ZipArchive } from "archiver";
import { once } from "node:events";
import { PassThrough, type Readable } from "node:stream";

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

// Builds one ZIP of the originals and stores it in S3. Bytes flow S3 → ZIP → S3 while it runs,
// so memory holds only a few MB, whatever the event's size. Returns the ZIP size in bytes.
export async function writeZip(bucket: string, key: string, originals: Original[]): Promise<number> {
  // Photos are already compressed: entries are stored as they are.
  const zip = new ZipArchive({ store: true });
  // Uploads, in parts, whatever the ZIP produces. Started first: it reads the ZIP while it is written.
  // Upload accepts only Node's own streams; archiver builds on the readable-stream package, so the
  // ZIP passes through an empty Node stream on its way.
  const upload = new Upload({
    client: s3,
    params: { Bucket: bucket, Key: key, Body: zip.pipe(new PassThrough()), ContentType: "application/zip" },
  });
  const uploaded = upload.done();
  const unlessFailed = stopOnFailure(zip, uploaded);

  try {
    // One photo at a time: the next download starts once this one is inside the ZIP.
    for (const original of originals) {
      const photo = await download(bucket, original.s3Key);
      photo.on("error", (error) => zip.destroy(error)); // A download cut midway breaks the ZIP.
      zip.append(photo, { name: fileName(original) });
      await unlessFailed(once(zip, "entry"));
    }
    await unlessFailed(zip.finalize()); // Writes the ZIP's table of contents.
    await unlessFailed(uploaded);
  } catch (error) {
    // Cancel the upload, so S3 keeps no parts of a broken ZIP.
    zip.abort();
    await upload.abort().catch(() => {});
    await uploaded.catch(() => {});
    throw error;
  }
  return zip.pointer();
}

// Waiting on the ZIP alone would hang until the Lambda timeout if the upload stopped reading it.
// Returns a wait that also ends, with the error, as soon as the ZIP or the upload fails.
function stopOnFailure(zip: ZipArchive, uploaded: Promise<unknown>) {
  const failed = new Promise<never>((_, reject) => {
    zip.on("error", reject);
    uploaded.catch(reject);
  });
  failed.catch(() => {}); // Raised by the waits that use it.
  return <T>(step: Promise<T>) => Promise.race([step, failed]);
}

async function download(bucket: string, key: string): Promise<Readable> {
  const { Body } = await s3.send(new GetObjectCommand({ Bucket: bucket, Key: key }));
  return Body as Readable;
}

function fileName(original: Original): string {
  return original.id + (extensions[original.contentType] ?? "");
}
