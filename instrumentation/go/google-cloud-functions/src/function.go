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
)

type HttpHandler = func(w http.ResponseWriter, r *http.Request)
var WrappedHandler HttpHandler

type Flusher interface {
	ForceFlush(context.Context) error
}

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
