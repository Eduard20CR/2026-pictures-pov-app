import type { S3Event } from "aws-lambda";
import { beforeEach, describe, expect, it, vi } from "vitest";

// Actor 1: S3 (our own helpers).
const readFirstBytes = vi.fn();
const deleteObject = vi.fn();
vi.mock("../src/s3", () => ({ readFirstBytes, deleteObject }));

// Actor 2: the database functions.
const markReady = vi.fn();
const deleteImageRow = vi.fn();
vi.mock("../src/images", () => ({ markReady, deleteImageRow }));

const { handler } = await import("../src/handler");

const JPEG = new Uint8Array([0xff, 0xd8, 0xff, 0xe0, 0, 0, 0, 0, 0, 0, 0, 0]);
const PDF = new TextEncoder().encode("%PDF-1.7....");

function s3Event(key: string): S3Event {
  return {
    Records: [{ s3: { bucket: { name: "photos" }, object: { key } } }],
  } as unknown as S3Event;
}

describe("handler", () => {
  beforeEach(() => vi.resetAllMocks());

  it("marks a real image READY", async () => {
    readFirstBytes.mockResolvedValue(JPEG);

    await handler(s3Event("originals/e1/img-1"));

    expect(markReady).toHaveBeenCalledWith("originals/e1/img-1");
    expect(deleteObject).not.toHaveBeenCalled();
  });

  it("deletes the row BEFORE the object when the file is not an image", async () => {
    readFirstBytes.mockResolvedValue(PDF);

    await handler(s3Event("originals/e1/img-1"));

    expect(markReady).not.toHaveBeenCalled();
    expect(deleteImageRow).toHaveBeenCalledWith("originals/e1/img-1");
    expect(deleteObject).toHaveBeenCalledWith("photos", "originals/e1/img-1");
    // If the Lambda dies in between, S3 retries and the object is still there.
    expect(deleteImageRow.mock.invocationCallOrder[0])
      .toBeLessThan(deleteObject.mock.invocationCallOrder[0]);
  });

  it("decodes the key that S3 sends URL-encoded", async () => {
    readFirstBytes.mockResolvedValue(JPEG);

    await handler(s3Event("originals/my+photo%281%29"));

    expect(readFirstBytes).toHaveBeenCalledWith("photos", "originals/my photo(1)");
    expect(markReady).toHaveBeenCalledWith("originals/my photo(1)");
  });
});
