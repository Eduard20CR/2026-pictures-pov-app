// `npm run local`: validates the local PENDING uploads once, then exits. Never deployed.
// Same variables and defaults as the API's local run; AWS credentials from your profile.
process.env.DB_URL ??= "jdbc:postgresql://localhost:5432/picturepov";
process.env.AWS_REGION ??= "us-east-1";

async function main(): Promise<void> {
  const bucket = process.env.PHOTOS_BUCKET;
  if (!bucket) {
    throw new Error("Set PHOTOS_BUCKET, as in apps/api/src/main/resources/application-local.properties");
  }

  // Loaded after the defaults above, as in apps/upload-cleanup/src/runLocal.ts.
  const { replayPending } = await import("./replayPending");
  const { closePool } = await import("./db");
  try {
    await replayPending(bucket);
  } finally {
    await closePool();
  }
}

void main();
