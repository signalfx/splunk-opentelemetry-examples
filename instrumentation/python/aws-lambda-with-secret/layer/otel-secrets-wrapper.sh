#!/bin/sh

set -e

ACCESS_TOKEN_SECRET_ID="SPLUNK_ACCESS_TOKEN"

echo "Retrieving SPLUNK_ACCESS_TOKEN using the AWS Parameters and Secrets Lambda Extension"

set -euo pipefail

log() { printf '[%s] %s\n' "$(date +'%Y-%m-%dT%H:%M:%S%z')" "$*" >&2; }

ACCESS_TOKEN_SECRET_ID="${ACCESS_TOKEN_SECRET_ID:?ACCESS_TOKEN_SECRET_ID is required}"
TARGET_ENV_VAR="${TARGET_ENV_VAR:-SECRET_STRING}"
REGION="${SECRET_REGION:-${AWS_REGION:-${AWS_DEFAULT_REGION:-}}}"

if [[ -z "${REGION}" ]]; then
  log "No region set. Set SECRET_REGION or AWS_REGION/AWS_DEFAULT_REGION."
  exit 1
fi

# Ensure Python/boto3 from layer is in PATH and PYTHONPATH if needed.
export PATH="/opt/bin:${PATH}"
export PYTHONPATH="/opt/python:${PYTHONPATH:-}"

SECRET_VALUE="$(
python3 - <<'PY' "${ACCESS_TOKEN_SECRET_ID}" "${REGION}"
import sys, json, time
from botocore.config import Config
import boto3

secret_id = sys.argv[1]
region = sys.argv[2]

# Increase retries for transient faults.
cfg = Config(region_name=region, retries={'max_attempts': 6, 'mode': 'standard'})
client = boto3.client('secretsmanager', config=cfg)

def get_secret():
    resp = client.get_secret_value(SecretId=secret_id)
    if 'SecretString' in resp and resp['SecretString'] is not None:
        return resp['SecretString']
    if 'SecretBinary' in resp and resp['SecretBinary'] is not None:
        import base64
        return base64.b64decode(resp['SecretBinary']).decode('utf-8', errors='replace')
    raise RuntimeError('Secret contains neither SecretString nor SecretBinary.')

# Simple backoff around client retries (covers cold start propagation issues).
delay = 0.5
for attempt in range(1, 8):
    try:
        val = get_secret()
        sys.stdout.write(val)
        sys.exit(0)
    except Exception as e:
        msg = str(e)
        # Retry on common transient errors.
        if any(s in msg for s in ('Throttling', 'ServiceUnavailable', 'InternalError', 'Timeout')):
            time.sleep(delay)
            delay = min(delay * 2, 5.0)
            continue
        raise
PY
)"

log "Fetched secret via boto3 (length: ${#SECRET_VALUE})."

# Export the secret as the SPLUNK_ACCESS_TOKEN environment variable
export SPLUNK_ACCESS_TOKEN="${SECRET_VALUE}"

# shellcheck disable=SC2163
