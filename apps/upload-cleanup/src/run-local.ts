// `npm run local`: one cleanup pass against the local database, then exit. Never deployed.
// Same variables and defaults as the API's local run; AWS credentials from your profile.
process.env.DB_URL ??= "jdbc:postgresql://localhost:5432/picturepov";
process.env.AWS_REGION ??= "us-east-1";
process.env.UPLOAD_TTL_MINUTES ??= "15"; // P-10, as in Terraform and the API default
if (!process.env.PHOTOS_BUCKET) {
  throw new Error("Set PHOTOS_BUCKET, as in apps/api/src/main/resources/application-local.properties");
}

async function main(): Promise<void> {
  // Imported after the defaults above: handler and images read them on load.
  const { handler } = await import("./handler");
  const { closePool } = await import("./db");
  try {
    await handler();
  } finally {
    await closePool();
  }
}

void main();
