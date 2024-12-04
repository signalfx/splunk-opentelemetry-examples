using Microsoft.Azure.Functions.Worker.Builder;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.DependencyInjection;

using OpenTelemetry.Trace;
using SplunkTelemetry;

var tracerProvider = SplunkTelemetryConfigurator.ConfigureSplunkTelemetry();

var host = new HostBuilder()
   .ConfigureFunctionsWorkerDefaults()
   .ConfigureServices(services => services.AddSingleton(tracerProvider))
   .Build();

host.Run();

var builder = FunctionsApplication.CreateBuilder(args);

builder.ConfigureFunctionsWebApplication();

builder.Build().Run();
