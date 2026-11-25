// `npm run local -- <exportId>`: builds one queued export against the local database, then exits. Never deployed.
// Same variables and defaults as the API's local run; AWS credentials from your profile.
import type { SQSEvent } from "aws-lambda";

process.env.DB_URL ??= "jdbc:postgresql://localhost:5432/picturepov";
process.env.AWS_REGION ??= "us-east-1";
process.env.MAX_RECEIVE_COUNT ??= "1"; // A local failure marks the export failed at once.

async function main(): Promise<void> {
  const exportId = process.argv[2];
  if (!process.env.PHOTOS_BUCKET || !exportId) {
    throw new Error("Usage: PHOTOS_BUCKET=<bucket> npm run local -- <exportId>");
  }

  // Loaded after the defaults above: handler.ts reads them on load.
  const { handler } = await import("./handler");
  const { closePool } = await import("./db");
  try {
    await handler({
      Records: [{ body: JSON.stringify({ exportId }), attributes: { ApproximateReceiveCount: "1" } }],
    } as unknown as SQSEvent);
  } finally {
    await closePool();
  }
}

void main();
