import React from "react";
import * as ReactDOM from "react-dom/client";
import {
    ApolloClient,
    InMemoryCache,
    ApolloLink,
    HttpLink,
} from "@apollo/client";
import { ApolloProvider } from "@apollo/client/react";
import { Observable } from "@apollo/client/utilities";
import { trace, context, propagation } from "@opentelemetry/api";
import App from "./App";
const GRAPHQL_ENDPOINT = "http://localhost:4000/";
// Enhanced HttpLink with trace context propagation and Server-Timing capture
const httpLink = new HttpLink({
    uri: GRAPHQL_ENDPOINT,
    fetch: async (uri, options) => {
        // Inject trace context into headers for backend correlation
        const headers = { ...options.headers };
        propagation.inject(context.active(), headers);
        const response = await fetch(uri, {
            ...options,
            headers,
        });
        // Capture Server-Timing header for trace correlation
        const serverTiming = response.headers.get('Server-Timing');
        if (serverTiming) {
            // Store for use in the tracing link
            response._serverTiming = serverTiming;
        }
        return response;
    },
});
// OpenTelemetry tracer for GraphQL spans.
const tracer = trace.getTracer("frontend-graphql");
// Function to extract trace correlation from Server-Timing header
function extractTraceCorrelation(serverTiming, span) {
    if (!serverTiming) return;
    // Match Splunk's Server-Timing format: traceparent;desc="00-traceId-spanId-01"
    const traceParentMatch = serverTiming.match(/traceparent;desc="([^"]+)"/);
    if (traceParentMatch) {
        const traceParent = traceParentMatch[1];
        const parts = traceParent.split('-');
        if (parts.length >= 3) {
            span.setAttribute('link.traceId', parts[1]);
            span.setAttribute('link.spanId', parts[2]);
        }
    }
}
// Apollo Link that creates a span for every GraphQL operation.
const tracingLink = new ApolloLink((operation, forward) => {
    if (!forward) {
        return null;
    }
    const { operationName, query, variables } = operation;
    // Derive operation type: query / mutation / subscription.
    const mainDef = query.definitions?.find(
        (def) => def.kind === "OperationDefinition"
    );
    const opType = mainDef?.operation || "query";
    const spanName = `graphql.${operationName || "anonymous"}`;
    const span = tracer.startSpan(spanName, {
        attributes: {
            "workflow.name": operationName || "anonymous",
            "graphql.operation.name": operationName || "anonymous",
            "graphql.operation.type": opType,
            "graphql.endpoint": GRAPHQL_ENDPOINT,
            "graphql.variables.present":
                !!variables && Object.keys(variables).length > 0,
        },
    });
    if (typeof window !== "undefined") {
        span.setAttribute("page.url", window.location.href);
    }
    // Set the span as active for trace context propagation
    const activeContext = trace.setSpan(context.active(), span);
    // Wrap the next link's observable in a new Observable.
    return new Observable((observer) => {
        const subscription = context.with(activeContext, () => {
            return forward(operation).subscribe({
                next: (result) => {
                    // Handle GraphQL errors
                    if (result.errors && result.errors.length > 0) {
                        span.setAttribute("graphql.status", "error");
                        span.setAttribute("error", true);
                        span.setAttribute(
                            "error.message",
                            result.errors.map((e) => e.message).join("; ")
                        );
                    } else {
                        span.setAttribute("graphql.status", "success");
                    }
                    // Extract trace correlation from Server-Timing header
                    // Note: This requires the response to be available in the result
                    // In practice, you might need to modify this based on your setup
                    if (result.extensions?.response?._serverTiming) {
                        extractTraceCorrelation(result.extensions.response._serverTiming, span);
                    }
                    // Custom RUM event for specific operations
                    if (operationName === "GetLocations" && window.SplunkRum?.addEvent) {
                        window.SplunkRum.addEvent("graphql_GetLocations_completed", {
                            graphql_operation_name: operationName || "anonymous",
                            graphql_operation_type: opType,
                            status:
                                result.errors && result.errors.length > 0 ? "error" : "success",
                        });
                    }
                    observer.next(result);
                },
                error: (networkError) => {
                    span.setAttribute("graphql.status", "network_error");
                    span.setAttribute("error", true);
                    span.setAttribute("error.message", networkError.message);
                    span.end();
                    observer.error(networkError);
                },
                complete: () => {
                    span.end();
                    observer.complete();
                },
            });
        });
        // Cleanup/unsubscribe.
        return () => {
            subscription.unsubscribe();
        };
    });
});
// Combine tracing link with the HTTP link.
const link = ApolloLink.from([tracingLink, httpLink]);
const client = new ApolloClient({
    link,
    cache: new InMemoryCache(),
});
// Supported in React 18+
const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
    <ApolloProvider client={client}>
        <App />
    </ApolloProvider>
);