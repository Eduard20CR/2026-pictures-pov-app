import { beforeEach, describe, expect, it, vi } from "vitest";

const query = vi.fn();

vi.mock("../src/db", () => ({ getPool: async () => ({ query }) }));

const { markReady, deleteImageRow } = await import("../src/images");

describe("markReady", () => {
  beforeEach(() => query.mockReset().mockResolvedValue({ rowCount: 1 }));

  it("updates only PENDING images for the given key", async () => {
    await markReady("events/1/photo.jpg");

    expect(query).toHaveBeenCalledOnce();
    const [sql, params] = query.mock.calls[0];
    expect(sql).toContain("SET status = 'READY'");
    expect(sql).toContain("status = 'PENDING'");
    expect(params).toEqual(["events/1/photo.jpg"]);
  });

  it("does not fail when no row matches (idempotent)", async () => {
    query.mockResolvedValue({ rowCount: 0 });

    await expect(markReady("events/1/photo.jpg")).resolves.toBeUndefined();
  });
});

describe("deleteImageRow", () => {
  beforeEach(() => query.mockReset().mockResolvedValue({ rowCount: 1 }));

  it("deletes the PENDING row and gives the quota back", async () => {
    await deleteImageRow("events/1/photo.jpg");

    expect(query).toHaveBeenCalledOnce();
    const [sql, params] = query.mock.calls[0];
    expect(sql).toContain("DELETE FROM images");
    expect(sql).toContain("upload_count - 1");
    expect(params).toEqual(["events/1/photo.jpg"]);
  });
});