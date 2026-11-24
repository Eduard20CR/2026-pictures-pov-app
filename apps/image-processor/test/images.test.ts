import { beforeEach, describe, expect, it, vi } from "vitest";

const query = vi.fn();

vi.mock("../src/db", () => ({ getPool: async () => ({ query }) }));

const { markReady, releasePending } = await import("../src/images");

describe("markReady", () => {
  beforeEach(() => query.mockReset().mockResolvedValue({ rowCount: 1 }));

  it("publishes only a PENDING image for the given key, with its thumbnail", async () => {
    await markReady("originals/e1/img-1", "thumbs/e1/img-1.webp");

    expect(query).toHaveBeenCalledOnce();
    const [sql, params] = query.mock.calls[0];
    expect(sql).toContain("status = 'READY'");
    expect(sql).toContain("thumb_key = $2");
    expect(sql).toContain("status = 'PENDING'");
    expect(params).toEqual(["originals/e1/img-1", "thumbs/e1/img-1.webp"]);
  });

  it("does not fail when no row matches (idempotent)", async () => {
    query.mockResolvedValue({ rowCount: 0 });

    await expect(markReady("originals/e1/img-1", "thumbs/e1/img-1.webp")).resolves.toBeUndefined();
  });
});

describe("releasePending", () => {
  beforeEach(() => query.mockReset().mockResolvedValue({ rowCount: 1 }));

  it("deletes the PENDING row and gives the quota back", async () => {
    await releasePending("events/1/photo.jpg");

    expect(query).toHaveBeenCalledOnce();
    const [sql, params] = query.mock.calls[0];
    expect(sql).toContain("DELETE FROM images");
    expect(sql).toContain("upload_count - 1");
    expect(params).toEqual(["events/1/photo.jpg"]);
  });
});
