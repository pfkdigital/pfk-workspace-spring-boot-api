#!/usr/bin/env bash
# Runs once LocalStack is ready (mounted into /etc/localstack/init/ready.d).
#
# Sets up the attachment upload/scan pipeline:
#   quarantine bucket --s3:ObjectCreated--> "uploads" topic --> scanner Lambda
#   scanner Lambda --publish verdict--> "scan-results" topic --> SQS queue --> Spring app
set -euo pipefail

REGION="${AWS_DEFAULT_REGION:-eu-west-2}"
export AWS_DEFAULT_REGION="$REGION"
QUARANTINE_BUCKET="pfk-workspace-quarantine"
CLEAN_BUCKET="pfk-workspace"
UPLOADS_TOPIC_NAME="pfk-workspace-uploads"
RESULTS_TOPIC_NAME="pfk-workspace-scan-results"
RESULTS_QUEUE_NAME="pfk-workspace-scan-results"
RESULTS_DLQ_NAME="pfk-workspace-scan-results-dlq"
FUNCTION_NAME="pfk-workspace-scanner"
MAX_RECEIVE_COUNT=3
DLQ_RETENTION_SECONDS=1209600
QUARANTINE_EXPIRY_DAYS=1

echo ">> creating buckets"
awslocal s3 mb "s3://${QUARANTINE_BUCKET}" --region "$REGION" || true
awslocal s3 mb "s3://${CLEAN_BUCKET}" --region "$REGION" || true

echo ">> setting ${QUARANTINE_EXPIRY_DAYS}-day expiry on ${QUARANTINE_BUCKET}"
awslocal s3api put-bucket-lifecycle-configuration \
  --bucket "$QUARANTINE_BUCKET" \
  --region "$REGION" \
  --lifecycle-configuration "{
    \"Rules\": [{
      \"ID\": \"expire-unscanned-uploads\",
      \"Status\": \"Enabled\",
      \"Filter\": {\"Prefix\": \"\"},
      \"Expiration\": {\"Days\": ${QUARANTINE_EXPIRY_DAYS}},
      \"AbortIncompleteMultipartUpload\": {\"DaysAfterInitiation\": ${QUARANTINE_EXPIRY_DAYS}}
    }]
  }"

echo ">> creating SNS topics"
UPLOADS_TOPIC_ARN=$(awslocal sns create-topic --name "$UPLOADS_TOPIC_NAME" --region "$REGION" --query TopicArn --output text)
RESULTS_TOPIC_ARN=$(awslocal sns create-topic --name "$RESULTS_TOPIC_NAME" --region "$REGION" --query TopicArn --output text)

echo ">> wiring s3:ObjectCreated:* on ${QUARANTINE_BUCKET} -> ${UPLOADS_TOPIC_ARN}"
awslocal s3api put-bucket-notification-configuration \
  --bucket "$QUARANTINE_BUCKET" \
  --notification-configuration "{
    \"TopicConfigurations\": [{
      \"TopicArn\": \"${UPLOADS_TOPIC_ARN}\",
      \"Events\": [\"s3:ObjectCreated:*\"]
    }]
  }"

echo ">> creating scan-results dead-letter queue"
RESULTS_DLQ_URL=$(awslocal sqs create-queue \
  --queue-name "$RESULTS_DLQ_NAME" \
  --attributes "MessageRetentionPeriod=${DLQ_RETENTION_SECONDS}" \
  --region "$REGION" --query QueueUrl --output text)
RESULTS_DLQ_ARN=$(awslocal sqs get-queue-attributes --queue-url "$RESULTS_DLQ_URL" --attribute-names QueueArn --region "$REGION" --query Attributes.QueueArn --output text)

echo ">> creating scan-results queue and subscribing it to ${RESULTS_TOPIC_ARN}"
RESULTS_QUEUE_URL=$(awslocal sqs create-queue --queue-name "$RESULTS_QUEUE_NAME" --region "$REGION" --query QueueUrl --output text)
RESULTS_QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url "$RESULTS_QUEUE_URL" --attribute-names QueueArn --region "$REGION" --query Attributes.QueueArn --output text)

echo ">> attaching redrive policy (${MAX_RECEIVE_COUNT} attempts -> ${RESULTS_DLQ_NAME})"
REDRIVE_POLICY=$(printf '{"deadLetterTargetArn":"%s","maxReceiveCount":"%s"}' "$RESULTS_DLQ_ARN" "$MAX_RECEIVE_COUNT")
QUEUE_ATTRIBUTES=$(python3 -c 'import json,sys; print(json.dumps({"RedrivePolicy": sys.argv[1]}))' "$REDRIVE_POLICY")
awslocal sqs set-queue-attributes \
  --queue-url "$RESULTS_QUEUE_URL" \
  --attributes "$QUEUE_ATTRIBUTES" \
  --region "$REGION"
awslocal sns subscribe \
  --topic-arn "$RESULTS_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$RESULTS_QUEUE_ARN" \
  --attributes RawMessageDelivery=true \
  --region "$REGION" >/dev/null

echo ">> deploying scanner lambda"
ZIP=/tmp/scanner.zip
python3 -c "import zipfile; zipfile.ZipFile('$ZIP', 'w').write('/opt/lambda/index.mjs', 'index.mjs')"
LAMBDA_ENV="Variables={CLEAN_BUCKET=${CLEAN_BUCKET},RESULTS_TOPIC_ARN=${RESULTS_TOPIC_ARN}}"
if awslocal lambda get-function --function-name "$FUNCTION_NAME" --region "$REGION" >/dev/null 2>&1; then
  echo "   (function already exists, updating code + env)"
  awslocal lambda update-function-code --function-name "$FUNCTION_NAME" --zip-file "fileb://${ZIP}" --region "$REGION" >/dev/null
  awslocal lambda wait function-updated-v2 --function-name "$FUNCTION_NAME" --region "$REGION"
  awslocal lambda update-function-configuration --function-name "$FUNCTION_NAME" --environment "$LAMBDA_ENV" --region "$REGION" >/dev/null
else
  awslocal lambda create-function \
    --function-name "$FUNCTION_NAME" \
    --runtime nodejs20.x \
    --handler index.handler \
    --role arn:aws:iam::000000000000:role/lambda-role \
    --zip-file "fileb://${ZIP}" \
    --environment "$LAMBDA_ENV" \
    --region "$REGION" >/dev/null
fi
awslocal lambda wait function-active-v2 --function-name "$FUNCTION_NAME" --region "$REGION"

FUNCTION_ARN=$(awslocal lambda get-function --function-name "$FUNCTION_NAME" --region "$REGION" --query Configuration.FunctionArn --output text)

echo ">> subscribing lambda to ${UPLOADS_TOPIC_ARN}"
awslocal sns subscribe \
  --topic-arn "$UPLOADS_TOPIC_ARN" \
  --protocol lambda \
  --notification-endpoint "$FUNCTION_ARN" \
  --region "$REGION" >/dev/null

echo ">> done"
awslocal s3 ls
echo "uploads topic      -> $(awslocal sns list-subscriptions-by-topic --topic-arn "$UPLOADS_TOPIC_ARN" --region "$REGION" --query 'Subscriptions[].Endpoint' --output text)"
echo "scan-results topic -> $(awslocal sns list-subscriptions-by-topic --topic-arn "$RESULTS_TOPIC_ARN" --region "$REGION" --query 'Subscriptions[].Endpoint' --output text)"
echo "scan-results dlq   -> $(awslocal sqs get-queue-attributes --queue-url "$RESULTS_QUEUE_URL" --attribute-names RedrivePolicy --region "$REGION" --query Attributes.RedrivePolicy --output text)"
echo "quarantine expiry  -> $(awslocal s3api get-bucket-lifecycle-configuration --bucket "$QUARANTINE_BUCKET" --region "$REGION" --query 'Rules[0].Expiration.Days' --output text) day(s)"
