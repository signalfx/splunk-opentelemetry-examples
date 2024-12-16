using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text.Json;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using OpenTelemetry;
using OpenTelemetry.Exporter;
using OpenTelemetry.Instrumentation.AWSLambda;
using OpenTelemetry.Instrumentation.AspNetCore;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;

namespace HelloWorld
{
    public class Startup
    {
        public void ConfigureServices(IServiceCollection services)
        {
            // Register IHttpClientFactory for dependency injection
            services.AddHttpClient();

            // Configure OpenTelemetry TracerProvider
            TracerProvider tracerProvider = ConfigureSplunkTelemetry();

            // Register TracerProvider as a singleton if needed elsewhere
            services.AddSingleton(tracerProvider);
        }

        public void Configure(IApplicationBuilder app, IWebHostEnvironment env, ILogger<Startup> logger)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }

            app.UseRouting();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapGet("/hello", async context =>
                {
                    logger.LogInformation("Received GET request at '/hello' endpoint.");

                    string location;
                    try
                    {
                        var httpClientFactory = context.RequestServices.GetRequiredService<IHttpClientFactory>();
                        var client = httpClientFactory.CreateClient();
                        client.DefaultRequestHeaders.Accept.Clear();
                        client.DefaultRequestHeaders.Add("User-Agent", "AWS Lambda .NET Client");

                        var msg = await client.GetStringAsync("http://checkip.amazonaws.com/").ConfigureAwait(false);
                        location = msg.Trim();
                    }
                    catch (HttpRequestException ex)
                    {
                        logger.LogError(ex, "Error fetching IP address.");
                        location = "Unable to retrieve IP";
                    }

                    var response = new Dictionary<string, string>
                    {
                        { "message", "Hello World" },
                        { "location", location }
                    };

                    logger.LogInformation("Sending response: {Response}", JsonSerializer.Serialize(response));

                    context.Response.ContentType = "application/json";
                    await context.Response.WriteAsync(JsonSerializer.Serialize(response));

                    logger.LogInformation("Response sent successfully.");
                });
            });
        }

        private TracerProvider ConfigureSplunkTelemetry()
        {
            var serviceName = Environment.GetEnvironmentVariable("AWS_LAMBDA_FUNCTION_NAME") ?? "Unknown";
            var accessToken = Environment.GetEnvironmentVariable("SPLUNK_ACCESS_TOKEN")?.Trim();
            var realm = Environment.GetEnvironmentVariable("SPLUNK_REALM")?.Trim();

            ArgumentNullException.ThrowIfNull(accessToken, "SPLUNK_ACCESS_TOKEN");
            ArgumentNullException.ThrowIfNull(realm, "SPLUNK_REALM");

            var builder = Sdk.CreateTracerProviderBuilder()
                // Instrumentations
                .AddHttpClientInstrumentation()
                .AddAspNetCoreInstrumentation()
                .AddAWSInstrumentation()
                // Disable AWS X-Ray context extraction to prevent conflicts
                .AddAWSLambdaConfigurations(opts => opts.DisableAwsXRayContextExtraction = true)
                // Sampling
                .SetSampler(new AlwaysOnSampler())
                // Resource Configuration
                .ConfigureResource(resourceBuilder =>
                {
                    resourceBuilder
                        .AddService(serviceName, serviceVersion: "1.0.0")
                        .AddAWSEBSDetector();
                })
                // Exporter Configuration
                .AddOtlpExporter();

            return builder.Build();
        }
    }
}
