# Instrumenting a Go Google Cloud Run Function with OpenTelemetry 

This example demonstrates how to instrument an serverless Google Cloud Run function written in
Golang using OpenTelemetry, and then export the data to Splunk Observability
Cloud.  We'll use Go version 1.23 for this example, but the steps for other Go versions are
similar.

## Prerequisites

The following tools are required to deploy Go Google Cloud Run functions:

* An Google Cloud Platform account with permissions to create and execute Google Cloud Run functions
* An OpenTelemetry collector that's accessible to the Google Cloud Run function
* [gCloud CLI](https://cloud.google.com/sdk/docs/install)

## Splunk Distribution of the OpenTelemetry Collector

For this example, we deployed the Splunk Distribution of the OpenTelemetry Collector onto a virtual machine
in GCP using Gateway mode, and ensured it's accessible to our Google Cloud Run function.

We configured it with the `SPLUNK_HEC_TOKEN` and `SPLUNK_HEC_URL` environment variables, so that it
exports logs to our Splunk Cloud instance.

Please refer to [Install the Collector using packages and deployment tools](https://docs.splunk.com/observability/en/gdi/opentelemetry/install-the-collector.html#collector-package-install)
for collector installation instructions.

## Application Overview

If you just want to build and deploy the example, feel free to skip this section.

The application used for this example is a simple Hello World application.

To instrument the Go function with OpenTelemetry, we added a helper function to initialize
the Splunk Distribution of OpenTelemetry Go: 

````
func initOpenTelemetry() (trace.Tracer) {
   _, err := distro.Run()
   if err != nil {
      panic(err)
   }

    return otel.Tracer("go-gcloud-function-example")
}
````

We also added code to initialize the `zap` logger, and create an OpenTelemetry logging provider: 

````
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
````

The `init()` function then uses these helper functions to initialize OpenTelemetry and 
the logger.  It then wraps the `helloHttp` function using `otelhttp`, which ensures that 
a new span with `span.kind` of `SERVER` is started whenever the `helloHttp` function is called: 

````
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
````

Finally, in the `helloHTTP` function, we demonstrate how to obtain and use a logger 
that automatically adds trace metadata (such as `span_id` and `trace_id`): 

````
func helloHTTP(w http.ResponseWriter, r *http.Request) {

	ctx := r.Context()
	loggerWithTraceContext := withTraceMetadata(ctx, logger)
	loggerWithTraceContext.Info("In helloHTTP()")
    ...
}
````

### Install Go Modules (Optional)

We used the following commands to add the Go modules required to instrument this application
with OpenTelemetry.  Please note that these commands don't need to be executed again, but are
provided for reference in case you'd like to apply instrumentation to your own Azure function.

Our application uses the `net/http` package so we'll add that with the following command:

````
cd src
go get go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp
````

Our application also uses [zap](https://github.com/uber-go/zap) for logging, so we'll add that
as well:

````
go get go.uber.org/zap
````

We then installed the Splunk distribution of OpenTelemetry Go with the following command:

````
go get github.com/signalfx/splunk-otel-go/distro
````

Then we installed the otlploghttp exporter to export logs: 

````
go get go.opentelemetry.io/otel/exporters/otlp/otlplog/otlploghttp
````

There's no need to run these commands again as you can use the `go.mod` file that
was already created.

## Build and Deploy

Open a terminal and navigate to the following directory:

````
splunk-opentelemetry-examples/instrumentation/go/google-cloud-functions
````

### Initialize the gCloud CLI

If you haven't already done so, [install](https://cloud.google.com/sdk/docs/install)
and [initialize](https://cloud.google.com/sdk/docs/initializing) the gcloud CLI.

### Build and Deploy the Google Cloud Run Function

Use the following command to deploy the Google Cloud Run function, substituting the
[region](https://cloud.google.com/functions/docs/locations)
that's best for you.  To allow OpenTelemetry to send trace data to Splunk Observability Cloud,
we also need to set the `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_SERVICE_NAME`, `OTEL_RESOURCE_ATTRIBUTES`,
and other environment variables as part of the gcloud deploy command:

```bash
gcloud functions deploy go-gcloud-function-example \
    --gen2 \
    --region=us-central1 \
    --runtime=go123 \
    --source=./src \
    --entry-point=WrappedHandler \
    --trigger-http \
    --set-env-vars OTEL_SERVICE_NAME=go-gcloud-function-example,OTEL_EXPORTER_OTLP_ENDPOINT=http://<collector IP address>:4317,OTEL_RESOURCE_ATTRIBUTES=deployment.environment=test
```

Answer "y" to the following question when asked:

````
Allow unauthenticated invocations of new function [go-gcloud-function-example]? (y/N)? 
````

If the function is created successfully, it should provide you with a URL such as the following:

````
https://us-central1-gcp-<account name>.cloudfunctions.net/go-gcloud-function-example
````

### Test the Google Cloud Run Function

Take the URL provided by the gcloud CLI above and enter it into your browser. It should return:

````
Hello World! 
````

### View Traces in Splunk Observability Cloud

After a minute or so, you should start to see traces for the serverless function
appearing in Splunk Observability Cloud:

![Trace](./images/trace.png)

Note that the bottom-right of the trace includes a button that links to the related log entries.

### Add Trace Context to Logs

Logs generated by a Google Cloud Run function get sent to Google Cloud Logging.
Various methods exist for streaming logs into Splunk platform from Google Cloud Logging,
as described in [Stream logs from Google Cloud to Splunk](https://cloud.google.com/architecture/stream-logs-from-google-cloud-to-splunk).

Once the logs are in Splunk platform, they can be made available to
Splunk Observability Cloud using Log Observer Connect.

In the following example, we can see that the trace context was injected successfully into the logs
using the custom logging changes added to [function.go](./src/function.go):

````
{
    caller: "serverless_function_source_code/function.go:116"
    level: "info"
    msg: "In helloHTTP()"
    span_id: "b4528560ef5c8df2"
    trace_flags: "01"
    trace_id: "3a6664e92a3849da2716d400796b90fc"
    ts: 1744133548.6599555
}
````

This will ensure full correlation between traces generated by the OpenTelemetry instrumentation
with metrics and logs. 