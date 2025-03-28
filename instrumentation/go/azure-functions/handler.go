package main

import (
	"context"
	"fmt"
	"net/http"
	"os"

	"github.com/signalfx/splunk-otel-go/distro"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlplog/otlploghttp"
	"go.opentelemetry.io/otel/log/global"
	"go.opentelemetry.io/otel/sdk/log"
	"go.opentelemetry.io/otel/trace"
	"go.uber.org/zap"
)

var logger *zap.Logger
var tracer trace.Tracer

func helloHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	loggerWithTraceContext := withTraceMetadata(ctx, logger)
	loggerWithTraceContext.Info("In helloHandler()")

	message := "Hello, World!\n"
	name := r.URL.Query().Get("name")
	if name != "" {
		message = fmt.Sprintf("Hello, %s!\n", name)
	}
	fmt.Fprint(w, message)
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

func newLoggerProvider(ctx context.Context) (*log.LoggerProvider, error) {
	exporter, err := otlploghttp.New(ctx)
	if err != nil {
		return nil, err
	}
	processor := log.NewBatchProcessor(exporter)
	provider := log.NewLoggerProvider(
		log.WithProcessor(processor),
	)
	return provider, nil
}

func main() {

	ctx := context.Background()

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

	tracer = otel.Tracer("azure_function_go_opentelemetry_example")

	// Create a logger provider.
	// You can pass this instance directly when creating bridges.
	loggerProvider, err := newLoggerProvider(ctx)
	if err != nil {
		panic(err)
	}

	defer func() {
		if err := loggerProvider.Shutdown(ctx); err != nil {
			fmt.Println(err)
		}
	}()

	global.SetLoggerProvider(loggerProvider)

	listenAddr := ":8080"
	if val, ok := os.LookupEnv("FUNCTIONS_CUSTOMHANDLER_PORT"); ok {
		listenAddr = ":" + val
	}

	// Wrap the helloHandler function.
	handler := http.HandlerFunc(helloHandler)
	wrappedHandler := otelhttp.NewHandler(handler, "hello")
	http.Handle("/api/azure_function_go_opentelemetry_example", wrappedHandler)
	logger.Info(fmt.Sprintf("About to listen on %s. Go to https://127.0.0.1%s/", listenAddr, listenAddr))
	http.ListenAndServe(listenAddr, nil)
}
