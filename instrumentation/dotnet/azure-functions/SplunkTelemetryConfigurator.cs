using System.Text.Json;
using System.Diagnostics;

using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Console;
using Microsoft.Extensions.Logging.Abstractions;

using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Azure.Functions.Worker;

using OpenTelemetry;
using OpenTelemetry.Trace;
using OpenTelemetry.Exporter;
using OpenTelemetry.Resources;

namespace SplunkTelemetry
{
    public static class SplunkTelemetryConfigurator
    {
        public static TracerProvider ConfigureSplunkTelemetry()
        {
            // Get environment variables from function configuration
            // You need a valid Splunk Observability Cloud access token and realm
            var serviceName = Environment.GetEnvironmentVariable("WEBSITE_SITE_NAME") ?? "Unknown";
            var accessToken = Environment.GetEnvironmentVariable("SPLUNK_ACCESS_TOKEN")?.Trim();
            var realm = Environment.GetEnvironmentVariable("SPLUNK_REALM")?.Trim();

            ArgumentNullException.ThrowIfNull(accessToken, "SPLUNK_ACCESS_TOKEN");
            ArgumentNullException.ThrowIfNull(realm, "SPLUNK_REALM");

            var builder = Sdk.CreateTracerProviderBuilder()
            // Use Add[instrumentation-name]Instrumentation to instrument missing services
            // Use Nuget to find different instrumentation libraries
            .AddHttpClientInstrumentation(opts =>
            {
                // This filter prevents background (parent-less) http client activity
                opts.FilterHttpWebRequest = req => Activity.Current?.Parent != null;
                opts.FilterHttpRequestMessage = req => Activity.Current?.Parent != null;
            })
            // Use AddSource to add your custom DiagnosticSource source names
            //.AddSource("My.Source.Name")
            // Creates root spans for function executions
            .AddSource("Microsoft.Azure.Functions.Worker")
            .SetSampler(new AlwaysOnSampler())
            .ConfigureResource(configure => configure
                .AddService(serviceName: serviceName, serviceVersion: "1.0.0")
                // See https://github.com/open-telemetry/opentelemetry-dotnet-contrib/tree/main/src/OpenTelemetry.Resources.Azure
                // for other types of Azure detectors
                .AddAzureAppServiceDetector())
            .AddOtlpExporter(opts =>
            {
                opts.Endpoint = new Uri($"https://ingest.{realm}.signalfx.com/v2/trace/otlp");
                opts.Protocol = OtlpExportProtocol.HttpProtobuf;
                opts.Headers = $"X-SF-TOKEN={accessToken}";
            }) 
            // Add the console exporter, which is helpful for debugging as the 
            // spans get written to the console but should be removed in production
            .AddConsoleExporter();

          return builder.Build()!;
       }

       public static ILogger<T> ConfigureLogger<T>()
       {
           var loggerFactory = LoggerFactory.Create(logging =>
           {
               logging.ClearProviders(); // Clear existing providers
               logging.Configure(options =>
               {
                   options.ActivityTrackingOptions = ActivityTrackingOptions.SpanId
                                   | ActivityTrackingOptions.TraceId
                                   | ActivityTrackingOptions.ParentId
                                   | ActivityTrackingOptions.Baggage
                                   | ActivityTrackingOptions.Tags;
               }).AddConsole(options =>
               {
                   options.FormatterName = "splunkLogsJson";
               });
               logging.AddConsoleFormatter<SplunkTelemetryConsoleFormatter, ConsoleFormatterOptions>();
           });

           return loggerFactory.CreateLogger<T>();
       }

       public static void AddSpanAttributes(HttpRequestData req, FunctionContext fc)
       {
           // Add span attributes using the HttpRequestData and FunctionContext
           // if needed
           var activity = Activity.Current;
           activity?.SetTag("sometag", "somevalue");
        }

        // Define helper functions for manual instrumentation
        public static ActivitySource ManualInstrumentationSource = new ActivitySource("manualInstrumentation");
        public static Activity? StartActivity(HttpRequestData req, FunctionContext fc)
        {
            // Retrieve resource attributes
            var answer = ManualInstrumentationSource.StartActivity(req.Method.ToUpper() + " " + req.Url.AbsolutePath, ActivityKind.Server);
            answer?.AddTag("http.url", req.Url);
            answer?.AddTag("faas.invocation_id", fc.InvocationId.ToString());
            answer?.AddTag("faas.name", Environment.GetEnvironmentVariable("WEBSITE_SITE_NAME") + "/" + fc.FunctionDefinition.Name);
            return answer;
        }
        public static HttpResponseData FinishActivity(HttpResponseData response, Activity? activity)
        {
            activity?.AddTag("http.status_code", ((int)response.StatusCode));
            return response;
        }
   }

   public class SplunkTelemetryConsoleFormatter : ConsoleFormatter
   {
       public SplunkTelemetryConsoleFormatter() : base("splunkLogsJson") { }

       public override void Write<TState>(in LogEntry<TState> logEntry, IExternalScopeProvider? scopeProvider, TextWriter textWriter)
       {
           var serviceName = Environment.GetEnvironmentVariable("WEBSITE_SITE_NAME") ?? "Unknown";
           var severity = logEntry.LogLevel switch
           {
               Microsoft.Extensions.Logging.LogLevel.Trace => "DEBUG",
               Microsoft.Extensions.Logging.LogLevel.Debug => "DEBUG",
               Microsoft.Extensions.Logging.LogLevel.Information => "INFO",
               Microsoft.Extensions.Logging.LogLevel.Warning => "WARN",
               Microsoft.Extensions.Logging.LogLevel.Error => "ERROR",
               Microsoft.Extensions.Logging.LogLevel.Critical => "FATAL",
               Microsoft.Extensions.Logging.LogLevel.None => "NONE",
               _ => "INFO"
           };
           var logObject = new Dictionary<string, object>
           {
               { "event_id", logEntry.EventId.Id },
               { "log_level", logEntry.LogLevel.ToString().ToLower() },
               { "category", logEntry.Category },
               { "message", logEntry.Formatter(logEntry.State, logEntry.Exception) },
               { "timestamp", DateTime.UtcNow.ToString("o") },
               { "service.name", serviceName },
               { "severity", severity }
           };
           // Add exception if present
           if (logEntry.Exception != null)
           {
               logObject["exception"] = logEntry.Exception.ToString();
           }
           // Include scopes if enabled
           if (scopeProvider != null)
           {
               scopeProvider.ForEachScope((scope, state) =>
               {
                   if (scope is IReadOnlyList<KeyValuePair<string, object>> scopeItems)
                   {
                       foreach (var kvp in scopeItems)
                       {
                           if (kvp.Key.Equals("SpanId", StringComparison.OrdinalIgnoreCase))
                               logObject["span_id"] = kvp.Value;
                           else if (kvp.Key.Equals("TraceId", StringComparison.OrdinalIgnoreCase))
                               logObject["trace_id"] = kvp.Value;
                           else if (kvp.Key.Equals("ParentId", StringComparison.OrdinalIgnoreCase))
                               logObject["parent_id"] = kvp.Value;
                           else
                               logObject[kvp.Key] = kvp.Value;
                       }
                   }
                   else if (scope is IEnumerable<KeyValuePair<string, string>> baggage)
                   {
                       foreach (var kvp in baggage)
                       {
                           logObject[$"baggage_{kvp.Key}"] = kvp.Value;
                       }
                   }
                   else if (scope is IEnumerable<KeyValuePair<string, object>> tags)
                   {
                       foreach (var kvp in tags)
                       {
                           logObject[$"tag_{kvp.Key}"] = kvp.Value;
                       }
                   }
               }, logObject);
           }

           var logJson = JsonSerializer.Serialize(logObject);
           textWriter.WriteLine(logJson);
       }
   }
}