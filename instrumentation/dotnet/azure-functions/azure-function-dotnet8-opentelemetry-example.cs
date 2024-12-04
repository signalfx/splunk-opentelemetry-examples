using Microsoft.Azure.Functions.Worker;
using Microsoft.Extensions.Logging;

using SplunkTelemetry; 
using System.Net;
using Microsoft.Azure.Functions.Worker.Http;

namespace example
{
    public class azure_function_dotnet8_opentelemetry_example
    {
        private readonly ILogger<azure_function_dotnet8_opentelemetry_example> _logger;

        public azure_function_dotnet8_opentelemetry_example(ILogger<azure_function_dotnet8_opentelemetry_example> logger)
        {
            _logger = SplunkTelemetryConfigurator.ConfigureLogger<azure_function_dotnet8_opentelemetry_example>();
        }

        [Function("azure_function_dotnet8_opentelemetry_example")]
        public HttpResponseData Run([HttpTrigger(AuthorizationLevel.Anonymous, "get", "post")] HttpRequestData req, FunctionContext fc)
        {
            using (var activity = SplunkTelemetryConfigurator.StartActivity(req, fc))
            {
                _logger.LogInformation("C# HTTP trigger function processed a request.");
                var response = req.CreateResponse(HttpStatusCode.OK);
                SplunkTelemetryConfigurator.AddSpanAttributes(req, fc);
                response.Headers.Add("Content-Type", "text/plain; charset=utf-8");
                response.WriteString("Hello, World!");
                return SplunkTelemetryConfigurator.FinishActivity(response, activity);
            }
        }
    }
}
