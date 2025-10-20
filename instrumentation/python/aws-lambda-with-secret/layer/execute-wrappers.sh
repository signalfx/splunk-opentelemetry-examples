#!/bin/bash
# A custom wrapper script to execute two different tools

# First, execute a custom wrapper to retrieve the SPLUNK_ACCESS_TOKEN from a secret
source /opt/otel-secrets-wrapper.sh "$@"

# Then, execute the otel-instrument wrapper to instrument the Python Lambda function with OpenTelemetry
/opt/otel-instrument "$@"