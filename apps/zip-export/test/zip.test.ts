import { GetObjectCommand } from "@aws-sdk/client-s3";
import { unzipSync } from "fflate";
import { Readable } from "node:stream";
import { buffer } from "node:stream/consumers";
import { beforeEach, describe, expect, it, vi } from "vitest";

// Actor 1: S3 reads. Each original is a stream, as GetObject returns it.
const files: Record<string, string> = {
  "originals/a": "first photo bytes",
  "originals/b": "second photo bytes",
};
const send = vi.fn();
vi.mock("@aws-sdk/client-s3", async (importOriginal) => ({
  ...(await importOriginal<typeof import("@aws-sdk/client-s3")>()),
  S3Client: class { send = send; },
}));

// Actor 2: the multipart upload. Keeps what the worker streamed into it.
const uploaded: { bucket?: string; key?: string; body?: Buffer } = {};
const abort = vi.fn();
vi.mock("@aws-sdk/lib-storage", () => ({
  Upload: class {
    constructor(private readonly options: { params: { Bucket: string; Key: string; Body: Readable } }) {}
    async done() {
      uploaded.bucket = this.options.params.Bucket;
      uploaded.key = this.options.params.Key;
      uploaded.body = await buffer(this.options.params.Body);
    }
    abort = abort;
  },
}));

const { writeZip } = await import("../src/zip");

const originals = [
  { id: "a", s3Key: "originals/a", contentType: "image/jpeg" },
  { id: "b", s3Key: "originals/b", contentType: "image/heic" },
];

describe("writeZip", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    abort.mockResolvedValue(undefined);
    send.mockImplementation(async (command: GetObjectCommand) => ({
      Body: Readable.from([Buffer.from(files[command.input.Key!])]),
    }));
  });

  it("stores every original, named like a single-photo download", async () => {
    await writeZip("photos", "zips/x.zip", originals);

    const entries = unzipSync(new Uint8Array(uploaded.body!));
    expect(Object.keys(entries)).toEqual(["a.jpg", "b.heic"]);
    expect(Buffer.from(entries["a.jpg"]).toString()).toBe("first photo bytes");
    expect(Buffer.from(entries["b.heic"]).toString()).toBe("second photo bytes");
    expect(uploaded).toMatchObject({ bucket: "photos", key: "zips/x.zip" });
  });

  it("returns the size of the stored ZIP", async () => {
    const size = await writeZip("photos", "zips/x.zip", originals);

    expect(size).toBe(uploaded.body!.length);
  });

  it("makes a valid empty ZIP for an event without photos", async () => {
    await writeZip("photos", "zips/x.zip", []);

    expect(unzipSync(new Uint8Array(uploaded.body!))).toEqual({});
  });

  it("fails when an original cannot be read", async () => {
    send.mockRejectedValue(new Error("NoSuchKey")); // Script: S3 fails

    await expect(writeZip("photos", "zips/x.zip", originals)).rejects.toThrow("NoSuchKey");
    expect(abort).toHaveBeenCalled(); // No multipart parts left behind
  });
});
