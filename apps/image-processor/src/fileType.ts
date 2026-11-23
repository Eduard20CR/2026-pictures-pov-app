// Copied in apps/upload-cleanup/src/fileType.ts (no shared packages): keep both in sync.
export type ImageType = "jpeg" | "png" | "webp" | "heic";

// The real type comes from the file's first bytes (magic bytes), never from its name.
export function detectImageType(bytes: Uint8Array): ImageType | null {
  const bytesAt = (offset: number, expected: number[]) =>
    expected.every((byte, i) => bytes[offset + i] === byte);
  const textAt = (offset: number, length: number) =>
    String.fromCharCode(...bytes.subarray(offset, offset + length));

  if (bytesAt(0, [0xff, 0xd8, 0xff])) return "jpeg";
  if (bytesAt(0, [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a])) return "png"; // "\x89PNG\r\n\x1a\n"
  if (textAt(0, 4) === "RIFF" && textAt(8, 4) === "WEBP") return "webp";
  if (textAt(4, 4) === "ftyp" && ["heic", "heix", "mif1"].includes(textAt(8, 4))) return "heic";
  return null;
}
