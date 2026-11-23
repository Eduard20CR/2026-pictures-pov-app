// Copied in apps/upload-cleanup/test/db.test.ts (no shared packages): keep both in sync.
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

// Actor 1: SSM. Must not be called for a local database.
const send = vi.fn();
vi.mock("@aws-sdk/client-ssm", async (importOriginal) => ({
  ...(await importOriginal<typeof import("@aws-sdk/client-ssm")>()),
  SSMClient: class { send = send; },
}));

// Actor 2: pg. Records the config the pool is built with.
const poolConfig = vi.fn();
vi.mock("pg", () => ({ Pool: class { constructor(config: unknown) { poolConfig(config); } } }));

describe("getPool with DB_URL", () => {
  beforeEach(() => {
    vi.resetModules();
    vi.resetAllMocks();
  });
  afterEach(() => vi.unstubAllEnvs());

  it("connects to DB_URL without SSL and never reads SSM", async () => {
    vi.stubEnv("DB_URL", "jdbc:postgresql://localhost:5433/picturepov");
    vi.stubEnv("DB_USERNAME", "me");
    vi.stubEnv("DB_PASSWORD", "secret");
    const { getPool } = await import("../src/db");

    await getPool();

    expect(poolConfig).toHaveBeenCalledWith(expect.objectContaining({
      host: "localhost", port: 5433, database: "picturepov",
      user: "me", password: "secret", ssl: false,
    }));
    expect(send).not.toHaveBeenCalled();
  });

  it("uses the API's default credentials when only DB_URL is set", async () => {
    vi.stubEnv("DB_URL", "jdbc:postgresql://localhost:5432/picturepov");
    vi.stubEnv("DB_USERNAME", undefined);
    vi.stubEnv("DB_PASSWORD", undefined);
    const { getPool } = await import("../src/db");

    await getPool();

    expect(poolConfig).toHaveBeenCalledWith(expect.objectContaining({
      port: 5432, user: "app", password: "app",
    }));
  });
});
