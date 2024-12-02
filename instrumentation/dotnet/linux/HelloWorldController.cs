using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;

public class HelloWorldController : Controller
{
    private readonly ILogger<HelloWorldController> _logger;

    public HelloWorldController(ILogger<HelloWorldController> logger)
    {
        _logger = logger;
    }

    //
    // GET: /HelloWorld/
    public string Index()
    {
        _logger.LogInformation("Processing a request...");
        return "Hello, World!";
    }
}
