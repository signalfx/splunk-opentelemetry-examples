using SplunkTelemetry;
using Microsoft.Extensions.Logging.Console;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddControllersWithViews();

SplunkTelemetryConfigurator.ConfigureLogger(builder.Logging);

var app = builder.Build();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=HelloWorld}/{action=Index}");

app.Run();
