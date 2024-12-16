var builder = WebApplication.CreateBuilder(args);

// Note: Services are configured in Startup.cs

var app = builder.Build();

// Note: Middleware and endpoints are configured in Startup.cs

app.Run();
