import { randomBytes } from "node:crypto";
import { readFile } from "node:fs/promises";
import sharp from "sharp";
import { describe, expect, it } from "vitest";
import { makeThumbnail } from "../src/thumbnail";

const photo = (width: number, height: number) =>
  sharp({ create: { width, height, channels: 3, background: "red" } });

describe("makeThumbnail", () => {
  it("turns a JPEG into a WebP that fits in 400 px", async () => {
    const jpeg = await photo(4000, 3000).jpeg().toBuffer();

    const thumbnail = await sharp(await makeThumbnail(jpeg, "jpeg")).metadata();

    expect(thumbnail.format).toBe("webp");
    expect([thumbnail.width, thumbnail.height]).toEqual([400, 300]);
  });

  it("stands the photo upright according to its EXIF orientation", async () => {
    // Orientation 6: the camera was turned 90°, so the stored landscape is a portrait.
    const sideways = await photo(300, 100).jpeg().withMetadata({ orientation: 6 }).toBuffer();

    const thumbnail = await sharp(await makeThumbnail(sideways, "jpeg")).metadata();

    expect([thumbnail.width, thumbnail.height]).toEqual([100, 300]);
  });

  it("keeps no location metadata", async () => {
    const withGps = await photo(300, 200).jpeg()
      .withExif({ IFD3: { GPSLatitudeRef: "N", GPSLatitude: "9/1 56/1 0/1" } })
      .toBuffer();
    expect((await sharp(withGps).metadata()).exif).toBeDefined(); // the original has it

    const thumbnail = await sharp(await makeThumbnail(withGps, "jpeg")).metadata();

    expect(thumbnail.exif).toBeUndefined();
    expect(thumbnail.xmp).toBeUndefined();
  });

  it("takes at most 100 KB, even for pure noise (the hardest image to compress)", async () => {
    const noise = await sharp(randomBytes(400 * 400 * 3), { raw: { width: 400, height: 400, channels: 3 } })
      .png()
      .toBuffer();

    const thumbnail = await makeThumbnail(noise, "png");

    expect(thumbnail.length).toBeLessThanOrEqual(100 * 1024);
  });

  it("turns an iPhone HEIC into a WebP", async () => {
    // 640x480, made with macOS `sips -s format heic`.
    const heic = await readFile(new URL("fixtures/photo.heic", import.meta.url));

    const thumbnail = await sharp(await makeThumbnail(heic, "heic")).metadata();

    expect(thumbnail.format).toBe("webp");
    expect([thumbnail.width, thumbnail.height]).toEqual([400, 300]);
  });

  it("stands a sideways iPhone HEIC upright once and drops its location", async () => {
    // Stored 300x100 with a 90° HEIC rotation and GPS in its EXIF, made with pillow-heif.
    const heic = await readFile(new URL("fixtures/sideways.heic", import.meta.url));

    const thumbnail = await sharp(await makeThumbnail(heic, "heic")).metadata();

    expect([thumbnail.width, thumbnail.height]).toEqual([100, 300]);
    expect(thumbnail.exif).toBeUndefined();
    expect(thumbnail.xmp).toBeUndefined();
  });

  it("fails on a file that only looks like an image", async () => {
    const fake = new Uint8Array([0xff, 0xd8, 0xff, 0xe0, 1, 2, 3, 4]); // JPEG magic bytes, nothing else

    await expect(makeThumbnail(fake, "jpeg")).rejects.toThrow();
  });
});
