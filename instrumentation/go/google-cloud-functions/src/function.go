package helloworld

import (
    "encoding/json"
    "fmt"
    "html"
    "net/http"
    "context"

    "go.uber.org/zap"

    "github.com/signalfx/splunk-otel-go/distro"
    "go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
    "go.opentelemetry.io/otel"
    "go.opentelemetry.io/otel/trace"
	"go.opentelemetry.io/otel/exporters/otlp/otlplog/otlploghttp"
	"go.opentelemetry.io/otel/log/global"
	"go.opentelemetry.io/otel/sdk/log"
)

var logger *zap.Logger

type HttpHandler = func(w http.ResponseWriter, r *http.Request)
var WrappedHandler HttpHandler
var tracer trace.Tracer

type Flusher interface {
	ForceFlush(context.Context) error
}

func init() {

    tracer = initOpenTelemetry()
    flusher := otel.GetTracerProvider().(Flusher)

    initLogger()

    // create instrumented handler
    handler := otelhttp.NewHandler(http.HandlerFunc(helloHTTP), "HelloHTTP")

    WrappedHandler = func(w http.ResponseWriter, r *http.Request) {
        // call the actual handler
        handler.ServeHTTP(w, r)

        // ensure any spans are flushed
        flusher.ForceFlush(r.Context())
    }
}

func initOpenTelemetry() (trace.Tracer) {
   _, err := distro.Run()
   if err != nil {
      panic(err)
   }

    return otel.Tracer("go-gcloud-function-example")
}

func initLogger() {
    var err error
	logger, err = zap.NewProduction()
	if err != nil {
		panic(err)
	}
	defer logger.Sync()

	ctx := context.Background()

	// Create a logger provider.
	// You can pass this instance directly when creating bridges.
	loggerProvider, err := newLoggerProvider(ctx)
	if err != nil {
		panic(err)
	}

	global.SetLoggerProvider(loggerProvider)
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

// helloHTTP is an HTTP Cloud Function with a request parameter.
func helloHTTP(w http.ResponseWriter, r *http.Request) {

	ctx := r.Context()
	loggerWithTraceContext := withTraceMetadata(ctx, logger)
	loggerWithTraceContext.Info("In helloHTTP()")

    var d struct {
        Name string `json:"name"`
    }
    if err := json.NewDecoder(r.Body).Decode(&d); err != nil {
        fmt.Fprint(w, "Hello, World!")
        return
    }
    if d.Name == "" {
        fmt.Fprint(w, "Hello, World!")
        return
    }
    fmt.Fprintf(w, "Hello, %s!", html.EscapeString(d.Name))
}
