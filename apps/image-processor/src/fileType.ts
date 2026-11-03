export type ImageType = "jpeg" | "png" | "webp" | "heic";

export function detectImageType(bytes: Uint8Array): ImageType | null {
    const startsWith = (offset: number, expected: number[]) =>
        expected.every((b, i) => bytes[offset + i] === b);
    const text = (offset: number, length: number) =>
        String.fromCharCode(...bytes.subarray(offset, offset + length));

    if (startsWith(0, [0xff, 0xd8, 0xff])) return "jpeg";
    if (startsWith(0, [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A])) return "png";
    if (text(0, 4) === "RIFF" && text(8, 4) === "WEBP") return "webp";
    if (text(4, 4) === "ftyp" && ["heic", "heix", "mif1"].includes(text(8, 4))) return "heic";
    return null;
}