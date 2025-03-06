package gcfv2;

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
        String otelExporterEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");

        if (serviceName == null)
            throw new IllegalArgumentException("The OTEL_SERVICE_NAME environment variable must be populated");
        if (deploymentEnvironment == null)
            throw new IllegalArgumentException("The DEPLOYMENT_ENVIRONMENT environment variable must be populated");
        if (otelExporterEndpoint == null)
            throw new IllegalArgumentException("The OTEL_EXPORTER_OTLP_ENDPOINT environment variable must be populated");

        Resource resource = Resource
            .getDefault()
            .toBuilder()
            .put(ResourceAttributes.SERVICE_NAME, serviceName)
            .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment)
            .build();

        OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
            .setEndpoint(String.format("%s/v1/traces", otelExporterEndpoint))
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
