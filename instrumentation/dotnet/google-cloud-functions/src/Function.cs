using Google.Cloud.Functions.Framework;
using Google.Cloud.Functions.Hosting;

using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

using System.IO;
using System.Text.Json;
using System.Threading.Tasks;

using OpenTelemetry.Trace;

namespace HelloHttp
{
    public class Startup : FunctionsStartup
    {
        // Virtual methods in the base class are overridden
        // here to perform customization.
        public override void ConfigureServices(WebHostBuilderContext context, IServiceCollection services)
        {
            ILogger _logger = SplunkTelemetryConfigurator.ConfigureLogger<Function>();
            _logger.LogInformation("Configuring a tracer provider");
             TracerProvider tracerProvider =  SplunkTelemetryConfigurator.ConfigureSplunkTelemetry();

            // Add the tracer provider to the service collection.
            services.AddSingleton(tracerProvider);
        }
    }

    [FunctionsStartup(typeof(Startup))]
    public class Function : IHttpFunction
    {
        private readonly ILogger _logger = SplunkTelemetryConfigurator.ConfigureLogger<Function>();

        public async Task HandleAsync(HttpContext context)
        {
            HttpRequest request = context.Request;

            using (var activity = SplunkTelemetryConfigurator.StartActivity(request, context))
            {
                _logger.LogInformation("C# HTTP trigger function received a request.");

                // Check URL parameters for "name" field
                // "world" is the default value
                string name = ((string) request.Query["name"]) ?? "world";

                SplunkTelemetryConfigurator.AddSpanAttributes(request, context);

                var response = context.Response;
                response.StatusCode = 200;
                await response.WriteAsync($"Hello {name}!");
                SplunkTelemetryConfigurator.FinishActivity(response, activity);
            }
        }
    }
}

