using System.Threading.Tasks;
using Amazon.Lambda.AspNetCoreServer;
using Amazon.Lambda.Core;
using Amazon.Lambda.APIGatewayEvents;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Logging;
using OpenTelemetry.Trace;
using OpenTelemetry.Instrumentation.AWSLambda;

namespace HelloWorld
{
    public class LambdaEntryPoint : APIGatewayHttpApiV2ProxyFunction
    {
        private TracerProvider _tracerProvider = null!;

        protected override void Init(IWebHostBuilder builder)
        {
            builder
                .UseStartup<Startup>()
                .ConfigureLogging(logging =>
                {
                    logging.AddConsole();
                    logging.SetMinimumLevel(Microsoft.Extensions.Logging.LogLevel.Debug);
                });
        }

        protected override void PostCreateWebHost(IWebHost webHost)
        {
            // Now that the host is fully built by the framework,
            // we can resolve the TracerProvider from DI.
            _tracerProvider = webHost.Services.GetRequiredService<TracerProvider>();
        }

        public override async Task<APIGatewayHttpApiV2ProxyResponse> FunctionHandlerAsync(
            APIGatewayHttpApiV2ProxyRequest request, ILambdaContext lambdaContext)
        {
            // Use AWSLambdaWrapper.TraceAsync now that we have _tracerProvider
            return await AWSLambdaWrapper.TraceAsync(_tracerProvider, base.FunctionHandlerAsync, request, lambdaContext);
        }
    }
}