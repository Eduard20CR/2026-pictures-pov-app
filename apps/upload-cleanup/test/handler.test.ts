import { NotFound } from "@aws-sdk/client-s3";
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

describe("handler", () => {
    beforeEach(() => {
        vi.resetAllMocks();
        findExpired.mockResolvedValue([{ id: "img-1", s3Key: "originals/a.jpg" }]);
    });

    it("marks the image READY when the photo exists in S3", async () => {
        send.mockResolvedValue({}); // Script: the photo exists

        await handler();

        expect(markReady).toHaveBeenCalledWith("img-1");
        expect(release).not.toHaveBeenCalled();
    });

    it("releases the reservation when the photo is not in S3", async () => {
        send.mockRejectedValue(new NotFound({ message: "x", $metadata: {} })); // Script: no photo

        await handler();

        expect(release).toHaveBeenCalledWith("img-1");
        expect(markReady).not.toHaveBeenCalled();
    });

    it("fails and frees nothing when S3 throws another error", async () => {
        send.mockRejectedValue(new Error("AccessDenied")); // Script: S3 fails

        await expect(handler()).rejects.toThrow("AccessDenied");

        expect(release).not.toHaveBeenCalled();
    });

    it("does nothing when there are no expired images", async () => {
        findExpired.mockResolvedValue([]); // Script: nothing expired

        await handler();

        expect(send).not.toHaveBeenCalled();
        expect(release).not.toHaveBeenCalled();
        expect(markReady).not.toHaveBeenCalled();
    });
});