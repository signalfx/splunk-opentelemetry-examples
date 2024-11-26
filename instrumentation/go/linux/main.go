package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"time"
    "go.uber.org/zap"

    "github.com/signalfx/splunk-otel-go/distro"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
    "go.opentelemetry.io/otel"
    "go.opentelemetry.io/otel/trace"
    "go.opentelemetry.io/otel/attribute"
)

var logger *zap.Logger
var tracer trace.Tracer

func slowFunction(ctx context.Context) {
    sleepTime := 1 * time.Second
	ctx, span := tracer.Start(ctx, "slowFunction", trace.WithAttributes(attribute.String("sleepTime", sleepTime.String())))
	defer span.End()
	time.Sleep(sleepTime)
}

// httpHandler is an HTTP handler function that is going to be instrumented.
func httpHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
    loggerWithTraceContext := withTraceMetadata(ctx, logger)

	loggerWithTraceContext.Info("In httpHandler()")

	fmt.Fprintf(w, "Hello, World!")

    loggerWithTraceContext.Info("Calling slowFunction()")
	slowFunction(ctx)
    loggerWithTraceContext.Info("Finished calling slowFunction()")
}

func withTraceMetadata(ctx context.Context, logger *zap.Logger) *zap.Logger {
        spanContext := trace.SpanContextFromContext(ctx)
        if !spanContext.IsValid() {
                // ctx does not contain a valid span.
                // There is no trace metadata to add.
                return logger
        }
        return logger.With(
                zap.String("trace_id", spanContext.TraceID().String()),
                zap.String("span_id", spanContext.SpanID().String()),
                zap.String("trace_flags", spanContext.TraceFlags().String()),
        )
}

func main() {
    sdk, err := distro.Run()
    if err != nil {
      panic(err)
    }
    // Flush all spans before the application exits
    defer func() {
      if err := sdk.Shutdown(context.Background()); err != nil {
         panic(err)
      }
    }()

    logger, err = zap.NewProduction()
    if err != nil {
            panic(err)
    }
    defer logger.Sync()

    tracer = otel.Tracer("go-linux-otel-example")

    // Wrap the httpHandler function.
    handler := http.HandlerFunc(httpHandler)
    wrappedHandler := otelhttp.NewHandler(handler, "hello")
    http.Handle("/hello", wrappedHandler)

    // And start the HTTP serve.
    log.Fatal(http.ListenAndServe(":8080", nil))
}

