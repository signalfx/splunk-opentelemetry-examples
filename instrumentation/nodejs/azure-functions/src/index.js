const { AzureFunctionsInstrumentation } = require('@azure/functions-opentelemetry-instrumentation');
const { NodeSDK } = require('@opentelemetry/sdk-node');
const { getNodeAutoInstrumentations } = require('@opentelemetry/auto-instrumentations-node');
const { OTLPTraceExporter } = require('@opentelemetry/exporter-trace-otlp-http');
const { OTLPMetricExporter } = require('@opentelemetry/exporter-metrics-otlp-http');
const { PeriodicExportingMetricReader } = require('@opentelemetry/sdk-metrics');
const { BatchLogRecordProcessor } = require('@opentelemetry/sdk-logs');
const { OTLPLogExporter } = require ('@opentelemetry/exporter-logs-otlp-http');

const sdk = new NodeSDK({
  traceExporter: new OTLPTraceExporter(),
  metricReader: new PeriodicExportingMetricReader({
    exporter: new OTLPMetricExporter(),
  }),
  logRecordProcessor: new BatchLogRecordProcessor(
    new OTLPLogExporter()
  ),
  instrumentations: [getNodeAutoInstrumentations(), new AzureFunctionsInstrumentation()],
});
sdk.start();

