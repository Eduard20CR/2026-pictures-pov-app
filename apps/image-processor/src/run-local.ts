// `npm run local`: one pass over the local PENDING rows, then exit. Never deployed.
// Same variables and defaults as the API's local run; AWS credentials from your profile.
process.env.DB_URL ??= "jdbc:postgresql://localhost:5432/picturepov";
process.env.AWS_REGION ??= "us-east-1";
const bucket = process.env.PHOTOS_BUCKET;
if (!bucket) {
  throw new Error("Set PHOTOS_BUCKET, as in apps/api/src/main/resources/application-local.properties");
}

async function main(bucket: string): Promise<void> {
  // Imported after the defaults above, as in apps/upload-cleanup/src/run-local.ts.
  const { processPending } = await import("./local");
  const { closePool } = await import("./db");
  try {
    await processPending(bucket);
  } finally {
    await closePool();
  }
}

void main(bucket);
