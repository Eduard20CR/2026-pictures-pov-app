import { describe, expect, it } from "vitest";
import { detectImageType } from "../src/fileType";

describe("detectImageType", () => {
    it("detects a JPEG", () => {
        const bytes = new Uint8Array([0xff, 0xd8, 0xff, 0xe0]);
        expect(detectImageType(bytes)).toBe("jpeg");
    });

    it("detects a PNG", () => {
        const bytes = new Uint8Array([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
        expect(detectImageType(bytes)).toBe("png");
    });

    it("detects a WebP", () => {
        const bytes = new TextEncoder().encode("RIFF????WEBP");
        expect(detectImageType(bytes)).toBe("webp");
    });

    it("detects a HEIC", () => {
        const bytes = new TextEncoder().encode("????ftypheic");
        expect(detectImageType(bytes)).toBe("heic");
    });

    it("rejects a PDF renamed to .jpg", () => {
        const bytes = new TextEncoder().encode("%PDF-1.7");
        expect(detectImageType(bytes)).toBeNull();
    });

    it("rejects a WAV audio (RIFF but not WEBP)", () => {
        const bytes = new TextEncoder().encode("RIFF????WAVE");
        expect(detectImageType(bytes)).toBeNull();
    });
});