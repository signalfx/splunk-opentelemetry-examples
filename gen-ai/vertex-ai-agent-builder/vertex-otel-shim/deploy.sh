#!/usr/bin/env bash
set -euo pipefail

# ===== Configure these =====
REGION="us-central1"
SERVICE_NAME="vertex-otel-shim"
PUBSUB_TOPIC="dialogflow-logs"
PUBSUB_SUBSCRIPTION="dialogflow-logs-to-shim"
LOG_SINK_NAME="dialogflow-to-pubsub"

# OTLP destination (example: Splunk Observability Cloud, US1 realm)
OTLP_ENDPOINT="https://ingest.${SPLUNK_REALM}.signalfx.com/v2/trace/otlp"
OTLP_HEADERS="X-SF-Token=${SPLUNK_TOKEN}"

# ===========================

gcloud config set project "${PROJECT_ID}"

echo "==> Enabling required APIs"
gcloud services enable \
    run.googleapis.com \
    pubsub.googleapis.com \
    logging.googleapis.com \
    cloudbuild.googleapis.com \
    artifactregistry.googleapis.com \
    storage.googleapis.com

echo "==> Creating Pub/Sub topic (idempotent)"
gcloud pubsub topics create "${PUBSUB_TOPIC}" 2>/dev/null || true

echo "==> Building & deploying Cloud Run service"
gcloud run deploy "${SERVICE_NAME}" \
    --source . \
    --region "${REGION}" \
    --no-allow-unauthenticated \
    --ingress internal-and-cloud-load-balancing \
    --set-env-vars "OTLP_ENDPOINT=${OTLP_ENDPOINT},OTLP_HEADERS=${OTLP_HEADERS},OTEL_SERVICE_NAME=vertex-conversational-agent" \
    --memory 512Mi \
    --cpu 1 \
    --min-instances 0 \
    --max-instances 5 \
    --timeout 30s

SERVICE_URL=$(gcloud run services describe "${SERVICE_NAME}" --region "${REGION}" --format 'value(status.url)')
echo "==> Service URL: ${SERVICE_URL}"

echo "==> Creating service account for Pub/Sub to invoke Cloud Run"
PUBSUB_SA="pubsub-invoker@${PROJECT_ID}.iam.gserviceaccount.com"
gcloud iam service-accounts create pubsub-invoker \
    --display-name "Pub/Sub Cloud Run invoker" 2>/dev/null || true

gcloud run services add-iam-policy-binding "${SERVICE_NAME}" \
    --region "${REGION}" \
    --member "serviceAccount:${PUBSUB_SA}" \
    --role "roles/run.invoker"

# Allow Pub/Sub service agent to mint tokens for the SA
PROJECT_NUMBER=$(gcloud projects describe "${PROJECT_ID}" --format='value(projectNumber)')
gcloud iam service-accounts add-iam-policy-binding "${PUBSUB_SA}" \
    --member "serviceAccount:service-${PROJECT_NUMBER}@gcp-sa-pubsub.iam.gserviceaccount.com" \
    --role "roles/iam.serviceAccountTokenCreator"

echo "==> Creating push subscription"
gcloud pubsub subscriptions create "${PUBSUB_SUBSCRIPTION}" \
    --topic "${PUBSUB_TOPIC}" \
    --push-endpoint "${SERVICE_URL}/" \
    --push-auth-service-account "${PUBSUB_SA}" \
    --ack-deadline 30 \
    --message-retention-duration 1d 2>/dev/null || \
gcloud pubsub subscriptions update "${PUBSUB_SUBSCRIPTION}" \
    --push-endpoint "${SERVICE_URL}/" \
    --push-auth-service-account "${PUBSUB_SA}"

echo "==> Creating log sink"
gcloud logging sinks create "${LOG_SINK_NAME}" \
    "pubsub.googleapis.com/projects/${PROJECT_ID}/topics/${PUBSUB_TOPIC}" \
    --log-filter="logName=\"projects/${PROJECT_ID}/logs/dialogflow-runtime.googleapis.com%2Frequests\" AND jsonPayload.queryResult.traceBlocks:*" \
    2>/dev/null || \
gcloud logging sinks update "${LOG_SINK_NAME}" \
    "pubsub.googleapis.com/projects/${PROJECT_ID}/topics/${PUBSUB_TOPIC}" \
    --log-filter="logName=\"projects/${PROJECT_ID}/logs/dialogflow-runtime.googleapis.com%2Frequests\" AND jsonPayload.queryResult.traceBlocks:*"

# Grant the sink's writer identity permission to publish
SINK_WRITER=$(gcloud logging sinks describe "${LOG_SINK_NAME}" --format='value(writerIdentity)')
gcloud pubsub topics add-iam-policy-binding "${PUBSUB_TOPIC}" \
    --member "${SINK_WRITER}" \
    --role "roles/pubsub.publisher"

echo "==> Done."
echo "Verify by sending a message to your agent in the simulator,"
echo "then check Splunk Observability for service 'vertex-conversational-agent'."