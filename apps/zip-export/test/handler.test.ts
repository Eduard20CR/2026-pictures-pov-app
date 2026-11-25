import type { SQSEvent } from "aws-lambda";
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.stubEnv("PHOTOS_BUCKET", "photos");
vi.stubEnv("MAX_RECEIVE_COUNT", "3");

// Actor 1: the database functions.
const claim = vi.fn();
const findOriginals = vi.fn();
const markAvailable = vi.fn();
const markFailed = vi.fn();
vi.mock("../src/exports", () => ({ claim, findOriginals, markAvailable, markFailed }));

// Actor 2: the ZIP writer.
const writeZip = vi.fn();
vi.mock("../src/zip", () => ({ writeZip }));

const { handler } = await import("../src/handler");

const message = (exportId: string, receiveCount = 1) => ({
  Records: [{
    body: JSON.stringify({ exportId }),
    attributes: { ApproximateReceiveCount: String(receiveCount) },
  }],
}) as unknown as SQSEvent;

const originals = [
  { id: "a", s3Key: "originals/a", contentType: "image/jpeg" },
  { id: "b", s3Key: "originals/b", contentType: "image/png" },
];

describe("handler", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    // Script: a queued export of an event with two available photos.
    claim.mockResolvedValue({ eventId: "event-1" });
    findOriginals.mockResolvedValue(originals);
    writeZip.mockResolvedValue(1234);
  });

  it("stores the event's ZIP and makes the export available with its count and size", async () => {
    await handler(message("exp-1"));

    expect(findOriginals).toHaveBeenCalledWith("event-1");
    expect(writeZip).toHaveBeenCalledWith("photos", "zips/exp-1.zip", originals);
    expect(markAvailable).toHaveBeenCalledWith("exp-1", "zips/exp-1.zip", 2, 1234);
  });

  it("does nothing for an export already finished: a redelivered message keeps the single ZIP", async () => {
    claim.mockResolvedValue(null); // Script: available or failed already

    await handler(message("exp-1", 2));

    expect(writeZip).not.toHaveBeenCalled();
    expect(markAvailable).not.toHaveBeenCalled();
    expect(markFailed).not.toHaveBeenCalled();
  });

  it("fails without marking the export, so SQS retries it", async () => {
    writeZip.mockRejectedValue(new Error("SlowDown")); // Script: S3 fails

    await expect(handler(message("exp-1", 1))).rejects.toThrow("SlowDown");

    expect(markFailed).not.toHaveBeenCalled();
    expect(markAvailable).not.toHaveBeenCalled();
  });

  it("marks the export failed with the reason on the last receive, before the message goes to the dead-letter queue", async () => {
    writeZip.mockRejectedValue(new Error("SlowDown")); // Script: S3 keeps failing

    await expect(handler(message("exp-1", 3))).rejects.toThrow("SlowDown");

    expect(markFailed).toHaveBeenCalledWith("exp-1", "SlowDown");
  });
});
