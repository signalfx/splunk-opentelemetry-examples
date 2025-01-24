const { NodeSDK } = require('@opentelemetry/sdk-node');
const { getNodeAutoInstrumentations } = require('@opentelemetry/auto-instrumentations-node');
const { OTLPTraceExporter } = require('@opentelemetry/exporter-trace-otlp-http');
const { OTLPMetricExporter } = require('@opentelemetry/exporter-metrics-otlp-http');
const { PeriodicExportingMetricReader } = require('@opentelemetry/sdk-metrics');
const { BatchLogRecordProcessor } = require('@opentelemetry/sdk-logs');
const { OTLPLogExporter } = require ('@opentelemetry/exporter-logs-otlp-http');
const functions = require('@google-cloud/functions-framework');
const logger = require('pino')()
const opentelemetry = require('@opentelemetry/api');

const sdk = new NodeSDK({
    traceExporter: new OTLPTraceExporter(),
    metricReader: new PeriodicExportingMetricReader({
        exporter: new OTLPMetricExporter(),
    }),
    logRecordProcessor: new BatchLogRecordProcessor(
        new OTLPLogExporter()
    ),
    instrumentations: [getNodeAutoInstrumentations()],
});
sdk.start();

const tracer = opentelemetry.trace.getTracer('google-cloud-function-nodejs-opentelemetry-example', '0.1.0');

functions.http('helloHttp', (req, res) => {

    tracer.startActiveSpan('google-cloud-function-nodejs-http-trigger', (span) => {
        logger.info(`helloHttp handler was invoked`);
        res.send(`Hello ${req.query.name || req.body.name || 'World'}!`);
        span.end();
    });

});
