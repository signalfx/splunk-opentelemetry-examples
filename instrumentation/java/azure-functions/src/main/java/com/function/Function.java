package com.function;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public class Function {

    private final OpenTelemetry openTelemetry = SplunkTelemetryConfigurator.configureOpenTelemetry();
    private final Tracer tracer = openTelemetry.getTracer(Function.class.getName(), "0.1.0");
    private static final Logger logger = LogManager.getLogger(Function.class);

    @FunctionName("Hello")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        Span span = tracer.spanBuilder("helloFunction").startSpan();

        try (Scope scope = span.makeCurrent()) {
            logger.info("Handling the Hello function call");
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, World!").build(); 
        }
        catch (Throwable t) {
            span.recordException(t);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("An error occurred while processing the request").build(); 
        }
        finally {
            span.end();
        }
    }
}
