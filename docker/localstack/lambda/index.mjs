// Stub virus-scanner Lambda for local development.
//
// Triggered via SNS when an object lands in the quarantine bucket. A real
// implementation would scan the object (e.g. ClamAV); this stub treats
// everything as clean unless the key contains "eicar", which lets the
// INFECTED path be exercised locally. Clean objects are promoted to the clean
// bucket, infected ones are deleted, and in every case a verdict is published
// to the scan-results topic so the backend can update the attachment row.
//
// @aws-sdk/client-s3 and @aws-sdk/client-sns are bundled in the LocalStack Node
// runtime, so no npm install needed.
import { S3Client, CopyObjectCommand, DeleteObjectCommand } from "@aws-sdk/client-s3";
import { SNSClient, PublishCommand } from "@aws-sdk/client-sns";

const CLEAN_BUCKET = process.env.CLEAN_BUCKET;
const RESULTS_TOPIC_ARN = process.env.RESULTS_TOPIC_ARN;

const s3 = new S3Client({
  endpoint: process.env.AWS_ENDPOINT_URL,
  forcePathStyle: true,
});
const sns = new SNSClient({ endpoint: process.env.AWS_ENDPOINT_URL });

export const handler = async (event) => {
  for (const record of event.Records ?? []) {
    const s3Event = JSON.parse(record.Sns.Message);
    for (const s3Record of s3Event.Records ?? []) {
      const bucket = s3Record.s3.bucket.name;
      const key = decodeURIComponent(s3Record.s3.object.key.replace(/\+/g, " "));
      console.log(`scanning s3://${bucket}/${key}`);

      const result = await scan(bucket, key);
      console.log(`verdict for ${key}: ${result.verdict}${result.reason ? ` (${result.reason})` : ""}`);

      await sns.send(
        new PublishCommand({
          TopicArn: RESULTS_TOPIC_ARN,
          Message: JSON.stringify({ storageKey: key, bucket: CLEAN_BUCKET, ...result }),
        }),
      );
    }
  }
  return { ok: true };
};

// Returns { verdict: "CLEAN" | "INFECTED" | "FAILED", reason? }.
async function scan(bucket, key) {
  try {
    // TODO: real scan goes here
    if (key.toLowerCase().includes("eicar")) {
      await s3.send(new DeleteObjectCommand({ Bucket: bucket, Key: key }));
      return { verdict: "INFECTED", reason: "stub: key matched eicar" };
    }

    await s3.send(
      new CopyObjectCommand({
        Bucket: CLEAN_BUCKET,
        Key: key,
        CopySource: `${bucket}/${encodeURIComponent(key)}`,
      }),
    );
    await s3.send(new DeleteObjectCommand({ Bucket: bucket, Key: key }));
    return { verdict: "CLEAN" };
  } catch (err) {
    console.error(`scan failed for ${key}`, err);
    return { verdict: "FAILED", reason: err?.message ?? String(err) };
  }
}
