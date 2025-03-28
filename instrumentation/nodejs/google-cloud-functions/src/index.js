const functions = require('@google-cloud/functions-framework');
const logger = require('pino')()
const opentelemetry = require('@opentelemetry/api');

const tracer = opentelemetry.trace.getTracer('google-cloud-function-nodejs-opentelemetry-example', '0.1.0');

functions.http('helloHttp', (req, res) => {

    tracer.startActiveSpan('google-cloud-function-nodejs-http-trigger', (span) => {
        logger.info(`helloHttp handler was invoked`);
        res.send(`Hello ${req.query.name || req.body.name || 'World'}!`);
        span.end();
    });

});
