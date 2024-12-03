using Microsoft.Azure.Functions.Worker.Builder;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.DependencyInjection;

using OpenTelemetry;
using OpenTelemetry.Exporter;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using System.Diagnostics;

// Get environment variables from function configuration
// You need a valid Splunk Observability Cloud access token and realm
var serviceName = Environment.GetEnvironmentVariable("WEBSITE_SITE_NAME") ?? "Unknown";
var accessToken = Environment.GetEnvironmentVariable("SPLUNK_ACCESS_TOKEN")?.Trim();
var realm = Environment.GetEnvironmentVariable("SPLUNK_REALM")?.Trim();

ArgumentNullException.ThrowIfNull(accessToken, "SPLUNK_ACCESS_TOKEN");
ArgumentNullException.ThrowIfNull(realm, "SPLUNK_REALM");

var tp = Sdk.CreateTracerProviderBuilder()
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
      .AddAttributes(new[]
      {
         new KeyValuePair<string, object>("deployment.environment", "test")
      })
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
   .AddConsoleExporter()
   .Build();

var host = new HostBuilder()
   .ConfigureFunctionsWorkerDefaults()
   .ConfigureServices(services => services.AddSingleton(tp))
   .Build();

host.Run();

var builder = FunctionsApplication.CreateBuilder(args);

builder.ConfigureFunctionsWebApplication();

builder.Build().Run();
