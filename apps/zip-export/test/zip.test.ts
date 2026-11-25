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
const uploadFails = vi.fn<() => Error | undefined>();
vi.mock("@aws-sdk/lib-storage", () => ({
  Upload: class {
    // As in the SDK, abort() makes done() fail.
    private readonly aborted = Promise.withResolvers<never>();
    constructor(private readonly options: { params: { Bucket: string; Key: string; Body: Readable } }) {
      this.aborted.promise.catch(() => {});
    }
    async done() {
      // As in the SDK: only Node's own Readable is accepted as a stream body.
      if (!(this.options.params.Body instanceof Readable)) throw new Error("Body Data is unsupported format");
      const error = uploadFails();
      if (error) throw error; // Stops reading the body, as a failed multipart upload does.
      uploaded.bucket = this.options.params.Bucket;
      uploaded.key = this.options.params.Key;
      uploaded.body = await Promise.race([buffer(this.options.params.Body), this.aborted.promise]);
    }
    async abort() {
      abort();
      this.aborted.reject(new Error("Upload aborted."));
    }
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

  it("downloads one photo at a time, never the whole event at once", async () => {
    let open = 0;
    let maxOpen = 0;
    send.mockImplementation(async (command: GetObjectCommand) => {
      maxOpen = Math.max(maxOpen, ++open);
      const body = Readable.from([Buffer.from(files[command.input.Key!])]);
      return { Body: body.on("end", () => open--) };
    });

    await writeZip("photos", "zips/x.zip", originals);

    expect(send).toHaveBeenCalledTimes(2);
    expect(maxOpen).toBe(1);
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

  it("fails when a download is cut midway", async () => {
    send.mockImplementation(async () => ({
      Body: Readable.from((async function* () {
        yield Buffer.from("half a photo");
        throw new Error("ECONNRESET"); // Script: the connection drops
      })()),
    }));

    await expect(writeZip("photos", "zips/x.zip", originals)).rejects.toThrow("ECONNRESET");
    expect(abort).toHaveBeenCalled();
  });

  it("fails, instead of waiting for the timeout, when the upload stops", async () => {
    uploadFails.mockReturnValue(new Error("AccessDenied")); // Script: S3 refuses the parts
    send.mockImplementation(async () => ({ Body: Readable.from([Buffer.alloc(1024 * 1024)]) })); // More than the stream buffers

    await expect(writeZip("photos", "zips/x.zip", originals)).rejects.toThrow("AccessDenied");
  });
});
