package gcfv2;

import java.io.BufferedWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public class HelloHttpFunction implements HttpFunction {

    private final OpenTelemetrySdk openTelemetrySdk = SplunkTelemetryConfigurator.configureOpenTelemetry();
    private final Tracer tracer = openTelemetrySdk.getTracer(HelloHttpFunction.class.getName(), "0.1.0");
    private static final Logger logger = LogManager.getLogger(HelloHttpFunction.class);

    public void service(final HttpRequest request, final HttpResponse response) throws Exception {
        Span span = tracer.spanBuilder("HelloHttpFunction").startSpan();

        try (Scope scope = span.makeCurrent()) {
            logger.info("Handling the HelloHttpFunction call");

            final BufferedWriter writer = response.getWriter();
            writer.write("Hello world!");
        }
        catch (Throwable t) {
            span.recordException(t);
        }
        finally {
            span.end();
        }
    }
}
