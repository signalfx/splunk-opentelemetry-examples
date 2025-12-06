import http from 'http';
import express from 'express';
import bodyParser from 'body-parser';
import cors from 'cors';

import { ApolloServer } from '@apollo/server';
import { expressMiddleware } from '@as-integrations/express4';
import { trace } from '@opentelemetry/api';

const tracer = trace.getTracer('books-graphql-service');

const typeDefs = `#graphql
  type Book {
    title: String
    author: String
  }

  type Query {
    books: [Book]
  }
`;

const books = [
    { title: 'The Awakening', author: 'Kate Chopin' },
    { title: 'City of Glass', author: 'Paul Auster' },
];

const resolvers = {
    Query: {
        books: () => books,
    },
};

const serverTimingPlugin = {
    async requestDidStart(requestContext) {
        return {
            async willSendResponse({ response, contextValue }) {
                const currentSpan = contextValue.otelSpan;

                if (currentSpan) {
                    currentSpan.setAttribute(
                        'graphql.operationName',
                        requestContext.request.operationName || 'anonymous'
                    );

                    const spanContext = currentSpan.spanContext();
                    const { traceId, spanId, traceFlags } = spanContext;

                    if (traceId && spanId) {
                        const flags = (traceFlags & 1) === 1 ? '01' : '00';
                        const serverTimingValue = `traceparent;desc="00-${traceId}-${spanId}-${flags}"`;

                        // When using express, use res.setHeader
                        if (contextValue.res && typeof contextValue.res.setHeader === 'function') {
                            contextValue.res.setHeader('Server-Timing', serverTimingValue);
                            contextValue.res.setHeader(
                                'Access-Control-Expose-Headers',
                                'Server-Timing'
                            );
                        }
                    }

                    currentSpan.end();
                }
            },
        };
    },
};

async function start() {
    const apolloServer = new ApolloServer({
        typeDefs,
        resolvers,
        plugins: [serverTimingPlugin],
    });

    await apolloServer.start();

    const app = express();

    // CORS for your frontend origin + credentials if needed
    app.use(
        '/graphql',
        cors({
            origin: 'http://localhost:5173',
            credentials: true,
        }),
        bodyParser.json(),
        expressMiddleware(apolloServer, {
            context: async ({ req, res }) => {
                const requestSpan = tracer.startSpan('graphql.request', {
                    attributes: {
                        'http.method': req.method,
                        'http.url': req.url,
                    },
                });

                return { req, res, otelSpan: requestSpan };
            },
        })
    );

    // Optional: handle preflight explicitly (usually cors() does this)
    app.options(
        '/graphql',
        cors({
            origin: 'http://localhost:5173',
            credentials: true,
        })
    );

    const httpServer = http.createServer(app);
    const port = 4000;

    httpServer.listen(port, () => {
        console.log(`🚀 Server listening at http://localhost:${port}/graphql`);
    });
}

start().catch(err => {
    console.error('Failed to start server', err);
    process.exit(1);
});