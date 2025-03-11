using System;
using System.IO;
using System.Text.Json;
using System.Diagnostics;
using System.Collections.Generic;

using Google.Cloud.Functions.Framework;

using Microsoft.AspNetCore.Http;

using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Console;
using Microsoft.Extensions.Logging.Abstractions;

using OpenTelemetry;
using OpenTelemetry.Trace;
using OpenTelemetry.Exporter;
using OpenTelemetry.Resources;

namespace HelloHttp
{
    public static class SplunkTelemetryConfigurator
    {
        public static TracerProvider ConfigureSplunkTelemetry()
        {
            // Get environment variables from function configuration
            var serviceName = Environment.GetEnvironmentVariable("OTEL_SERVICE_NAME") ?? "Unknown";
            var otelExporterEndpoint = Environment.GetEnvironmentVariable("OTEL_EXPORTER_OTLP_ENDPOINT") ?? "Unknown";
            var otelResourceAttributes = Environment.GetEnvironmentVariable("OTEL_RESOURCE_ATTRIBUTES") ?? "Unknown";

            ArgumentNullException.ThrowIfNull(serviceName, "OTEL_SERVICE_NAME");
            ArgumentNullException.ThrowIfNull(otelExporterEndpoint, "OTEL_EXPORTER_OTLP_ENDPOINT");
            ArgumentNullException.ThrowIfNull(otelResourceAttributes, "OTEL_RESOURCE_ATTRIBUTES");

            var builder = Sdk.CreateTracerProviderBuilder()
            // Use Add[instrumentation-name]Instrumentation to instrument missing services
            // Use Nuget to find different instrumentation libraries
            .AddHttpClientInstrumentation(opts =>
            {
                // This filter prevents background (parent-less) http client activity
                opts.FilterHttpWebRequest = req => Activity.Current?.Parent != null;
                opts.FilterHttpRequestMessage = req => Activity.Current?.Parent != null;
            })
            // Use AddSource to add a custom DiagnosticSource source name
            .AddSource("Google.Cloud.Function")
            .SetSampler(new AlwaysOnSampler())
            // Add resource attributes to all spans
            .SetResourceBuilder(
               ResourceBuilder.CreateDefault()
               .AddService(serviceName: serviceName, serviceVersion: "1.0.0")
                // TODO: add the Gcp Detector once it's available as a NuGet package
                // See https://github.com/open-telemetry/opentelemetry-dotnet-contrib/tree/main/src/OpenTelemetry.Resources.Gcp
                //.AddGcpDetector())
            )
            .AddOtlpExporter(opts =>
            {
                opts.Endpoint = new Uri($"{otelExporterEndpoint}/v1/traces");
                opts.Protocol = OtlpExportProtocol.HttpProtobuf;
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

       public static void AddSpanAttributes(HttpRequest request, HttpContext context)
       {
           // Add span attributes using the HttpRequest and HttpContext
           // if needed
           var activity = Activity.Current;
           activity?.SetTag("name", ((string) request.Query["name"]) ?? "world");
        }

        // Define helper functions for manual instrumentation
        public static ActivitySource ManualInstrumentationSource = new ActivitySource("Google.Cloud.Function");
        public static Activity? StartActivity(HttpRequest req, HttpContext fc)
        {
            // Retrieve resource attributes
            var activity = ManualInstrumentationSource.StartActivity("HelloHttp.Function", ActivityKind.Server);
            return activity;
        }
        public static HttpResponse FinishActivity(HttpResponse response, Activity? activity)
        {
            activity?.AddTag("http.response.status_code", ((int)response.StatusCode));
            return response;
        }
   }

   public class SplunkTelemetryConsoleFormatter : ConsoleFormatter
   {
       public SplunkTelemetryConsoleFormatter() : base("splunkLogsJson") { }

       public override void Write<TState>(in LogEntry<TState> logEntry, IExternalScopeProvider? scopeProvider, TextWriter textWriter)
       {
           var serviceName = Environment.GetEnvironmentVariable("OTEL_SERVICE_NAME") ?? "Unknown";
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