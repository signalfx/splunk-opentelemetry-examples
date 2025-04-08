package helloworld

import (
  "encoding/json"
  "fmt"
  "html"
  "net/http"

  "context"
  "github.com/signalfx/splunk-otel-go/distro"
   "go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
   "go.opentelemetry.io/otel"
   "go.opentelemetry.io/otel/trace"
)

type HttpHandler = func(w http.ResponseWriter, r *http.Request)
var WrappedHandler HttpHandler
var tracer trace.Tracer

type Flusher interface {
	ForceFlush(context.Context) error
}

func init() {

    tracer = initOpenTelemetry()
    flusher := otel.GetTracerProvider().(Flusher)

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
   // comment this out otherwise the SDK will shutdown at the end of the init function
   //defer func() {
   //   if err := sdk.Shutdown(context.Background()); err != nil {
   //      panic(err)
   //   }
   //}()

    return otel.Tracer("go-gcloud-function-example")
}

// helloHTTP is an HTTP Cloud Function with a request parameter.
func helloHTTP(w http.ResponseWriter, r *http.Request) {

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
