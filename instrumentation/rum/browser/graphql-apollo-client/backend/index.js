import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { trace } from '@opentelemetry/api';

const tracer = trace.getTracer('books-graphql-service');

// A schema is a collection of type definitions (hence "typeDefs")
// that together define the "shape" of queries that are executed against
// your data.
const typeDefs = `#graphql
  # Comments in GraphQL strings (such as this one) start with the hash (#) symbol.

  # This "Book" type defines the queryable fields for every book in our data source.
  type Book {
    title: String
    author: String
  }

  # The "Query" type is special: it lists all of the available queries that
  # clients can execute, along with the return type for each. In this
  # case, the "books" query returns an array of zero or more Books (defined above).
  type Query {
    books: [Book]
  }
`;

const books = [
    {
        title: 'The Awakening',
        author: 'Kate Chopin',
    },
    {
        title: 'City of Glass',
        author: 'Paul Auster',
    },
];

// Resolvers define how to fetch the types defined in your schema.
// This resolver retrieves books from the "books" array above.
const resolvers = {
    Query: {
        books: () => books,
    },
};

// Apollo Server Plugin to inject Server-Timing header
const serverTimingPlugin = {
    async requestDidStart(requestContext) {
        console.log('Apollo Server: requestDidStart');
        return {
            async willSendResponse({ response, contextValue }) {

                const currentSpan = contextValue.otelSpan;
                if (currentSpan) {
                    // Add any final attributes or status
                    currentSpan.setAttribute('graphql.operationName', requestContext.request.operationName || 'anonymous');
                    //currentSpan.end(); // End the span at the end of the request
                }

                // Existing Server-Timing / currentSpan logic
                //const currentSpan = trace.getActiveSpan();
                console.log(
                    'Current OpenTelemetry Span:',
                    currentSpan ? currentSpan.spanContext() : 'No active span'
                );

                if (currentSpan) {
                    const spanContext = currentSpan.spanContext();
                    const { traceId, spanId, traceFlags } = spanContext;

                    if (traceId && spanId) {
                        const flags = (traceFlags & 1) === 1 ? '01' : '00'; // Sampled flag
                        const serverTimingValue = `traceparent;desc="00-${traceId}-${spanId}-${flags}"`;

                        // Attempt to set header using response.http (preferred for standalone)
                        if (response.http && response.http.headers) {
                            response.http.headers.set('Server-Timing', serverTimingValue);
                            response.http.headers.set('Access-Control-Expose-Headers', 'Server-Timing');
                            console.log(`Injected Server-Timing via response.http: ${serverTimingValue}`);
                        }
                        // Fallback: Use contextValue.res if available (requires `context` function in startStandaloneServer)
                        else if (contextValue.res && typeof contextValue.res.setHeader === 'function') {
                            contextValue.res.setHeader('Server-Timing', serverTimingValue);
                            contextValue.res.setHeader('Access-Control-Expose-Headers', 'Server-Timing');
                            console.log(`Injected Server-Timing via contextValue.res: ${serverTimingValue}`);
                        } else {
                            console.warn('Could not find a suitable HTTP response object to set headers.');
                        }
                    } else {
                        console.warn('OpenTelemetry span context missing traceId or spanId.');
                    }

                    currentSpan.end(); // End the span at the end of the request
                } else {
                    console.warn('No active OpenTelemetry span found at willSendResponse stage.');
                }
            },
        };
    },
};

// The ApolloServer constructor requires two parameters: your schema
// definition and your set of resolvers.
const server = new ApolloServer({
    typeDefs,
    resolvers,
    plugins: [serverTimingPlugin], // Add your custom plugin here
});

startStandaloneServer(server, {
    listen: { port: 4000 },
    context: async ({ req, res }) => {
        let requestSpan;

        await tracer.startActiveSpan(
            'graphql.request',
            {
                attributes: {
                    'http.method': req.method,
                    'http.url': req.url,
                },
            },
            async span => {
                // Save the span so resolvers/plugins can access it via context
                requestSpan = span;
                // You could do additional per-request setup here if needed
            }
        );

        return {
            req,
            res,
            otelSpan: requestSpan,
        };
    },
}).then(({ url }) => {
    console.log(`🚀 Server listening at ${url}`);
});