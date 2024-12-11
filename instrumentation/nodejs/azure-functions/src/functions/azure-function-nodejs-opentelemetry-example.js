const { app } = require('@azure/functions');
const logger = require('pino')()
const opentelemetry = require('@opentelemetry/api');

const tracer = opentelemetry.trace.getTracer('azure-function-nodejs-opentelemetry-example', '0.1.0');

app.http('azure-function-nodejs-opentelemetry-example', {
    methods: ['GET', 'POST'],
    authLevel: 'anonymous',
    handler: async (request, context) => {
        return tracer.startActiveSpan('nodejs-azure-http-trigger', (span) => {

            logger.info(`Http function processed request for url "${request.url}"`);

            const name = request.query.get('name') || 'world';
            
            span.setAttribute('app.name', name);
            span.end();

            return { body: `Hello, ${name}!` };
        });
    }
});
