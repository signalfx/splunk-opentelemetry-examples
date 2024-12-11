const { app } = require('@azure/functions');
const { start } = require('@splunk/otel');
const { getInstrumentations } = require('@splunk/otel/lib/instrumentations');
const { AzureFunctionsInstrumentation } = require('@azure/functions-opentelemetry-instrumentation');

start({
   tracing: {
      instrumentations: [
         ...getInstrumentations(), // Adds default instrumentations
         new AzureFunctionsInstrumentation()
      ],
   },
});

app.setup({
    enableHttpStream: true,
});