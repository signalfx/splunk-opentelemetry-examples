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
   //"go.opentelemetry.io/otel/sdk/trace"
)

type HttpHandler = func(w http.ResponseWriter, r *http.Request)
var WrappedHandler HttpHandler
var tracer trace.Tracer
//var tracerProvider trace.TracerProvider

func init() {

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

	tracer = otel.Tracer("google_cloud_function_go_opentelemetry_example")

    // create instrumented handler
    handler := otelhttp.NewHandler(http.HandlerFunc(helloHTTP), "HelloHTTP")

    WrappedHandler = func(w http.ResponseWriter, r *http.Request) {
        // call the actual handler
        handler.ServeHTTP(w, r)

        // TODO: do we need to flush spans here?
    }
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
