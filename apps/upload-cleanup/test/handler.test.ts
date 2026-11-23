import { DeleteObjectCommand, GetObjectCommand, NoSuchKey } from "@aws-sdk/client-s3";
import { beforeEach, describe, expect, it, vi } from "vitest";

// Actor 1: S3. Only send() is fake.
const send = vi.fn();
vi.mock("@aws-sdk/client-s3", async (importOriginal) => ({
    ...(await importOriginal<typeof import("@aws-sdk/client-s3")>()),
    S3Client: class { send = send; },
}));

// Actor 2: the database functions.
const findExpired = vi.fn();
const markReady = vi.fn();
const release = vi.fn();
vi.mock("../src/images", () => ({ findExpired, markReady, release }));

const { handler } = await import("../src/handler");

// Script: S3 holds a file with these first bytes; deletes succeed.
function photoInS3(bytes: Uint8Array) {
    send.mockImplementation(async (command) => {
        if (command instanceof GetObjectCommand) {
            return { Body: { transformToByteArray: async () => bytes } };
        }
        return {};
    });
}

const deletedKeys = () =>
    send.mock.calls
        .map(([command]) => command)
        .filter((command) => command instanceof DeleteObjectCommand)
        .map((command) => command.input.Key);

describe("handler", () => {
    beforeEach(() => {
        vi.resetAllMocks();
        // Script: one expired upload.
        findExpired
            .mockResolvedValueOnce([{ id: "img-1", s3Key: "originals/a.jpg" }])
            .mockResolvedValue([]);
    });

    it("publishes the photo when its real type is a valid image", async () => {
        photoInS3(new Uint8Array([0xff, 0xd8, 0xff, 0xe0]));

        await handler();

        expect(markReady).toHaveBeenCalledWith("img-1");
        expect(release).not.toHaveBeenCalled();
        expect(deletedKeys()).toEqual([]);
    });

    it("removes the file and returns the slot when its real type is not an image", async () => {
        photoInS3(new TextEncoder().encode("%PDF-1.7"));

        await handler();

        expect(markReady).not.toHaveBeenCalled();
        expect(release).toHaveBeenCalledWith("img-1");
        expect(deletedKeys()).toEqual(["originals/a.jpg"]);
    });

    it("returns the slot when the upload never reached S3", async () => {
        // Script: no file in S3
        send.mockRejectedValue(new NoSuchKey({ message: "x", $metadata: {} }));

        await handler();

        expect(markReady).not.toHaveBeenCalled();
        expect(release).toHaveBeenCalledWith("img-1");
    });

    it("fails and frees nothing when S3 throws another error", async () => {
        send.mockRejectedValue(new Error("AccessDenied")); // Script: S3 fails

        await expect(handler()).rejects.toThrow("AccessDenied");

        expect(release).not.toHaveBeenCalled();
        expect(markReady).not.toHaveBeenCalled();
    });

    it("settles a single batch per run", async () => {
        findExpired
            .mockReset()
            .mockResolvedValueOnce([{ id: "img-1", s3Key: "originals/a.jpg" }])
            .mockResolvedValueOnce([{ id: "img-2", s3Key: "originals/b.jpg" }]); // Script: two batches
        send.mockRejectedValue(new NoSuchKey({ message: "x", $metadata: {} }));

        await handler();

        expect(findExpired).toHaveBeenCalledOnce();
        expect(release.mock.calls).toEqual([["img-1"]]);
    });

    it("does nothing when there are no expired uploads", async () => {
        findExpired.mockReset().mockResolvedValue([]); // Script: nothing expired

        await handler();

        expect(send).not.toHaveBeenCalled();
        expect(release).not.toHaveBeenCalled();
        expect(markReady).not.toHaveBeenCalled();
    });
});
