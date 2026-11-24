import type { S3Event } from "aws-lambda";
import sharp from "sharp";
import { beforeEach, describe, expect, it, vi } from "vitest";

// Actor 1: S3 (our own helpers).
const readObject = vi.fn();
const putObject = vi.fn();
const deleteObject = vi.fn();
vi.mock("../src/s3", () => ({ readObject, putObject, deleteObject }));

// Actor 2: the database functions.
const markReady = vi.fn();
const releasePending = vi.fn();
vi.mock("../src/images", () => ({ markReady, releasePending }));

const { handler } = await import("../src/handler");

const JPEG = await sharp({ create: { width: 800, height: 600, channels: 3, background: "red" } })
  .jpeg()
  .toBuffer();
const PDF = new TextEncoder().encode("%PDF-1.7....");
const BROKEN_JPEG = new Uint8Array([0xff, 0xd8, 0xff, 0xe0, 0, 0, 0, 0, 0, 0, 0, 0]);

function s3Event(key: string): S3Event {
  return {
    Records: [{ s3: { bucket: { name: "photos" }, object: { key } } }],
  } as unknown as S3Event;
}

describe("handler", () => {
  beforeEach(() => vi.resetAllMocks());

  it("stores a WebP thumbnail and publishes the photo with it", async () => {
    readObject.mockResolvedValue(JPEG);

    await handler(s3Event("originals/e1/img-1"));

    expect(putObject).toHaveBeenCalledWith("photos", "thumbs/e1/img-1.webp", expect.any(Buffer), "image/webp");
    const [, , thumbnail] = putObject.mock.calls[0];
    expect((await sharp(thumbnail).metadata()).format).toBe("webp");
    expect(markReady).toHaveBeenCalledWith("originals/e1/img-1", "thumbs/e1/img-1.webp");
    expect(deleteObject).not.toHaveBeenCalled();
  });

  it("stores the thumbnail BEFORE publishing, so a READY photo always has one", async () => {
    readObject.mockResolvedValue(JPEG);

    await handler(s3Event("originals/e1/img-1"));

    expect(putObject.mock.invocationCallOrder[0])
      .toBeLessThan(markReady.mock.invocationCallOrder[0]);
  });

  it("releases the row BEFORE deleting the file when it is not an image", async () => {
    readObject.mockResolvedValue(PDF);

    await handler(s3Event("originals/e1/img-1"));

    expect(markReady).not.toHaveBeenCalled();
    expect(putObject).not.toHaveBeenCalled();
    expect(releasePending).toHaveBeenCalledWith("originals/e1/img-1");
    expect(deleteObject).toHaveBeenCalledWith("photos", "originals/e1/img-1");
    // If the Lambda dies in between, S3 retries and the object is still there.
    expect(releasePending.mock.invocationCallOrder[0])
      .toBeLessThan(deleteObject.mock.invocationCallOrder[0]);
  });

  it("rejects a file with image magic bytes that cannot be decoded", async () => {
    readObject.mockResolvedValue(BROKEN_JPEG);

    await handler(s3Event("originals/e1/img-1"));

    expect(markReady).not.toHaveBeenCalled();
    expect(putObject).not.toHaveBeenCalled();
    expect(releasePending).toHaveBeenCalledWith("originals/e1/img-1");
    expect(deleteObject).toHaveBeenCalledWith("photos", "originals/e1/img-1");
  });

  it("fails and rejects nothing when storing the thumbnail fails, so S3 retries", async () => {
    readObject.mockResolvedValue(JPEG);
    putObject.mockRejectedValue(new Error("SlowDown"));

    await expect(handler(s3Event("originals/e1/img-1"))).rejects.toThrow("SlowDown");

    expect(markReady).not.toHaveBeenCalled();
    expect(releasePending).not.toHaveBeenCalled();
    expect(deleteObject).not.toHaveBeenCalled();
  });

  it("decodes the key that S3 sends URL-encoded", async () => {
    readObject.mockResolvedValue(JPEG);

    await handler(s3Event("originals/my+photo%281%29"));

    expect(readObject).toHaveBeenCalledWith("photos", "originals/my photo(1)");
    expect(markReady).toHaveBeenCalledWith("originals/my photo(1)", "thumbs/my photo(1).webp");
  });
});
