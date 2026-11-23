import type { S3Event } from "aws-lambda";
import { beforeEach, describe, expect, it, vi } from "vitest";

// Actor 1: the deployed handler.
const handler = vi.fn();
vi.mock("../src/handler", () => ({ handler }));

// Actor 2: the database and S3 helpers.
const findPendingKeys = vi.fn();
vi.mock("../src/images", () => ({ findPendingKeys }));
const objectExists = vi.fn();
vi.mock("../src/s3", () => ({ objectExists }));

const { replayPending } = await import("../src/replayPending");

// What the handler would see in AWS: the decoded key of each event.
const handledKeys = () =>
  (handler.mock.calls as [S3Event][]).map(([event]) =>
    decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, " ")));

describe("replayPending", () => {
  beforeEach(() => vi.resetAllMocks());

  it("calls the handler once per PENDING row with an S3 event for its bucket", async () => {
    findPendingKeys.mockResolvedValue(["originals/e1/a", "originals/e1/my photo (1)"]);
    objectExists.mockResolvedValue(true);

    await replayPending("photos");

    expect(handler).toHaveBeenCalledTimes(2);
    expect(handledKeys()).toEqual(["originals/e1/a", "originals/e1/my photo (1)"]);
    const [[event]] = handler.mock.calls as [[S3Event]];
    expect(event.Records).toHaveLength(1);
    expect(event.Records[0].s3.bucket.name).toBe("photos");
  });

  it("skips a row whose file is not in the bucket yet", async () => {
    findPendingKeys.mockResolvedValue(["originals/e1/a", "originals/e1/b"]);
    objectExists.mockImplementation(async (_bucket: string, key: string) => key === "originals/e1/b");

    await replayPending("photos");

    expect(objectExists).toHaveBeenCalledWith("photos", "originals/e1/a");
    expect(handledKeys()).toEqual(["originals/e1/b"]);
  });
});
