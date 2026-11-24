import decodeHeicPixels from "heic-decode";
import sharp, { type Sharp } from "sharp";
import type { ImageType } from "./fileType";

const MAX_BYTES = 100 * 1024; // RNF-REN-02
// 400 px fits a phone gallery grid; at quality 40 even pure noise stays under the limit.
// The 200 px step is a safety net that makes the limit a guarantee.
const ATTEMPTS = [
  { side: 400, quality: 80 },
  { side: 400, quality: 60 },
  { side: 400, quality: 40 },
  { side: 200, quality: 40 },
];

// Upright (EXIF orientation) and without metadata, so no location (RF-SIS-03, RNF-PRI-01):
// sharp drops all metadata unless asked to keep it. Throws if the bytes cannot be decoded.
export async function makeThumbnail(bytes: Uint8Array, type: ImageType): Promise<Buffer> {
  const image = type === "heic" ? await decodeHeic(bytes) : sharp(bytes).autoOrient();

  let webp = Buffer.alloc(0);
  for (const { side, quality } of ATTEMPTS) {
    webp = await image.clone()
      .resize(side, side, { fit: "inside", withoutEnlargement: true })
      .webp({ quality })
      .toBuffer();
    if (webp.length <= MAX_BYTES) break;
  }
  return webp;
}

// sharp's prebuilt binaries cannot decode HEVC (patents), so libheif in WASM does it.
// libheif already applies the HEIC rotation, and raw pixels carry no metadata.
// A burst or Live Photo yields its first image, which iPhones store as the primary one.
async function decodeHeic(bytes: Uint8Array): Promise<Sharp> {
  const { width, height, data } = await decodeHeicPixels({ buffer: bytes });
  return sharp(data, { raw: { width, height, channels: 4 } });
}
