package com.function;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;

public class SplunkTelemetryConfigurator {

    public static OpenTelemetry configureOpenTelemetry() {

        String serviceName = System.getenv("OTEL_SERVICE_NAME");
        String deploymentEnvironment = System.getenv("DEPLOYMENT_ENVIRONMENT");
        String realm = System.getenv("SPLUNK_REALM");
        String accessToken = System.getenv("SPLUNK_ACCESS_TOKEN");

        if (serviceName == null)
            throw new IllegalArgumentException("The OTEL_SERVICE_NAME environment variable must be populated");
        if (deploymentEnvironment == null)
            throw new IllegalArgumentException("The DEPLOYMENT_ENVIRONMENT environment variable must be populated");
        if (realm == null)
            throw new IllegalArgumentException("The SPLUNK_REALM environment variable must be populated");
        if (accessToken == null)
            throw new IllegalArgumentException("The SPLUNK_ACCESS_TOKEN environment variable must be populated");

        // Note:  an Azure resource detector isn't currently available but should be 
        // added here once it is 
        Resource resource = Resource
            .getDefault()
            .toBuilder()
            .put(ResourceAttributes.SERVICE_NAME, serviceName)
            .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment)
            .build();

        OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
            .setEndpoint(String.format("https://ingest.%s.signalfx.com/v2/trace/otlp", realm))
            .addHeader("X-SF-TOKEN", accessToken)
            .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .setResource(resource)
            .build();
            
        return OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();
    }
}
