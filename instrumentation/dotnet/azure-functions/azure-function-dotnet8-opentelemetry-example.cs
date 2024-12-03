using Microsoft.Azure.Functions.Worker;
using Microsoft.Extensions.Logging;

using System.Diagnostics;
using System.Net;
using Microsoft.Azure.Functions.Worker.Http;

namespace example
{
    public class azure_function_dotnet8_opentelemetry_example
    {
        private readonly ILogger<azure_function_dotnet8_opentelemetry_example> _logger;

        public azure_function_dotnet8_opentelemetry_example(ILogger<azure_function_dotnet8_opentelemetry_example> logger)
        {
            _logger = logger;
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

        [Function("azure_function_dotnet8_opentelemetry_example")]
        public HttpResponseData Run([HttpTrigger(AuthorizationLevel.Anonymous, "get", "post")] HttpRequestData req, FunctionContext fc)
        {
            using (var activity = StartActivity(req, fc))
            {
                _logger.LogInformation("C# HTTP trigger function processed a request.");
                var response = req.CreateResponse(HttpStatusCode.OK);
               response.Headers.Add("Content-Type", "text/plain; charset=utf-8");
               response.WriteString("Hello, World!");
               return FinishActivity(response, activity);
            }
        }
    }
}
