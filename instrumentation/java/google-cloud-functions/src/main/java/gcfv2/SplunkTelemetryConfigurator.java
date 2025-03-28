package gcfv2;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.contrib.gcp.resource.GCPResourceProvider;

public class SplunkTelemetryConfigurator {

    public static OpenTelemetrySdk configureOpenTelemetry() {

        String serviceName = System.getenv("OTEL_SERVICE_NAME");
        String otelResourceAttributes = System.getenv("OTEL_RESOURCE_ATTRIBUTES");
        String otelExporterEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");

        if (serviceName == null)
            throw new IllegalArgumentException("The OTEL_SERVICE_NAME environment variable must be populated");
        if (otelResourceAttributes == null)
            throw new IllegalArgumentException("The OTEL_RESOURCE_ATTRIBUTES environment variable must be populated");
        if (otelExporterEndpoint == null)
            throw new IllegalArgumentException("The OTEL_EXPORTER_OTLP_ENDPOINT environment variable must be populated");

        OpenTelemetrySdk openTelemetrySdk =
            AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

        Resource autoResource = ResourceConfiguration.createEnvironmentResource();

        GCPResourceProvider resourceProvider = new GCPResourceProvider();

        return openTelemetrySdk;
    }
}
