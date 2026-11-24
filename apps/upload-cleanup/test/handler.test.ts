import { DeleteObjectCommand } from "@aws-sdk/client-s3";
import { beforeEach, describe, expect, it, vi } from "vitest";

// Actor 1: S3. Only send() is fake.
const send = vi.fn();
vi.mock("@aws-sdk/client-s3", async (importOriginal) => ({
    ...(await importOriginal<typeof import("@aws-sdk/client-s3")>()),
    S3Client: class { send = send; },
}));

// Actor 2: the database functions.
const findExpired = vi.fn();
const release = vi.fn();
vi.mock("../src/images", () => ({ findExpired, release }));

const { handler } = await import("../src/handler");

const deletedKeys = () =>
    send.mock.calls
        .map(([command]) => command)
        .filter((command) => command instanceof DeleteObjectCommand)
        .map((command) => command.input.Key);

describe("handler", () => {
    beforeEach(() => {
        vi.resetAllMocks();
        // Script: one expired upload; S3 deletes succeed, even for a missing file.
        findExpired
            .mockResolvedValueOnce([{ id: "img-1", s3Key: "originals/a.jpg" }])
            .mockResolvedValue([]);
        send.mockResolvedValue({});
    });

    it("returns the slot and removes the file of an upload never published", async () => {
        await handler();

        expect(release).toHaveBeenCalledWith("img-1");
        expect(deletedKeys()).toEqual(["originals/a.jpg"]);
    });

    it("deletes the file BEFORE releasing the row", async () => {
        await handler();

        // If the Lambda dies in between, the next run finds the row again: never an orphan file.
        expect(send.mock.invocationCallOrder[0])
            .toBeLessThan(release.mock.invocationCallOrder[0]);
    });

    it("fails and keeps the row when S3 cannot delete, so the next run retries", async () => {
        send.mockRejectedValue(new Error("AccessDenied")); // Script: S3 fails

        await expect(handler()).rejects.toThrow("AccessDenied");

        expect(release).not.toHaveBeenCalled();
    });

    it("frees a single batch per run", async () => {
        findExpired
            .mockReset()
            .mockResolvedValueOnce([{ id: "img-1", s3Key: "originals/a.jpg" }])
            .mockResolvedValueOnce([{ id: "img-2", s3Key: "originals/b.jpg" }]); // Script: two batches

        await handler();

        expect(findExpired).toHaveBeenCalledOnce();
        expect(release.mock.calls).toEqual([["img-1"]]);
    });

    it("does nothing when there are no expired uploads", async () => {
        findExpired.mockReset().mockResolvedValue([]); // Script: nothing expired

        await handler();

        expect(send).not.toHaveBeenCalled();
        expect(release).not.toHaveBeenCalled();
    });
});
