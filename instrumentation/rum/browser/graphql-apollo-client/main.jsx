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
import { trace } from "@opentelemetry/api";
import App from "./App";

const GRAPHQL_ENDPOINT = "https://flyby-router-demo.herokuapp.com/";

// HttpLink to your GraphQL endpoint.
const httpLink = new HttpLink({
    uri: GRAPHQL_ENDPOINT,
});

// OpenTelemetry tracer for GraphQL spans.
const tracer = trace.getTracer("frontend-graphql");

// Apollo Link that creates a span for every GraphQL operation.
const tracingLink = new ApolloLink((operation, forward) => {
    if (!forward) {
        // If there is no next link, just return the observable from forward.
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

    span.setAttribute('link.traceId', span.spanContext().traceId);
    span.setAttribute('link.spanId', '0000000000000000');

    // Wrap the next link's observable in a new Observable.
    return new Observable((observer) => {
        const subscription = forward(operation).subscribe({
            next: (result) => {
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

                // Example: Generate a custom RUM event for a specific operation.
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
                // End the span if it hasn't already been ended in `error`.
                span.end();
                observer.complete();
            },
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