import type { SQSEvent, SQSRecord } from "aws-lambda";
import { claim, findOriginals, markAvailable, markFailed } from "./exports";
import { writeZip } from "./zip";

const bucket = process.env.PHOTOS_BUCKET!;
// The queue's maxReceiveCount, set in Terraform: after this receive the message goes to the dead-letter queue.
const maxReceiveCount = Number(process.env.MAX_RECEIVE_COUNT!);

// One message per run (batch size 1 in Terraform): { "exportId": "<uuid>" }.
export async function handler(event: SQSEvent): Promise<void> {
  for (const record of event.Records) {
    await exportZip(record);
  }
}

async function exportZip(record: SQSRecord): Promise<void> {
  const { exportId } = JSON.parse(record.body) as { exportId: string };

  try {
    // Null once available or failed: a redelivered message leaves the single ZIP as it is.
    const job = await claim(exportId);
    if (!job) return;

    const originals = await findOriginals(job.eventId);
    // One key per export: a retry overwrites the same ZIP.
    const key = `zips/${exportId}.zip`;
    const size = await writeZip(bucket, key, originals);
    await markAvailable(exportId, key, originals.length, size);
  } catch (error) {
    // Earlier receives throw only: SQS retries. The last one also records why.
    if (Number(record.attributes.ApproximateReceiveCount) >= maxReceiveCount) {
      await markFailed(exportId, error instanceof Error ? error.message : String(error));
    }
    throw error;
  }
}
