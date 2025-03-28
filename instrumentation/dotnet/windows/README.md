# Instrumenting a .NET Framework Application on Windows with OpenTelemetry (Work in Progress)

This example demonstrates how to instrument a .NET Framework Application on Windows using OpenTelemetry,
and export traces, metrics, and logs to a local collector, which will then
export that data to Splunk. We'll use .NET Framework 4.8 for this example. 

## Prerequisites

The following tools are required to build and execute the .NET Framework application:

* A Windows-compatible host (such as Windows Server 2022)
* Git command line tools 
* [Visual Studio 2022](https://visualstudio.microsoft.com/vs/community/)
* [.NET Framework 4.8](https://dotnet.microsoft.com/en-us/download/dotnet-framework/net48)
* [IIS with ASP.NET 4.8 Enabled](https://techcommunity.microsoft.com/blog/iis-support-blog/how-to-enable-iis-and-key-features-on-windows-server-a-step-by-step-guide/4229883)

## Deploy the Splunk OpenTelemetry Collector

This example requires the Splunk Distribution of the OpenTelemetry collector to
be running on the host and available via http://localhost:4318.  Follow the
instructions in [Install the Collector for Windows with the installer script](https://docs.splunk.com/observability/en/gdi/opentelemetry/collector-windows/install-windows.html#otel-install-windows)
to install the collector on your Windows host.

## Build and Execute the Application

For this example, we'll use the [ASP.NET Docker Sample](https://github.com/microsoft/dotnet-framework-docker/tree/main/samples/aspnetapp) application from Microsoft. 

Use git to clone the example source code and then build the application: 

``` bash
git clone https://github.com/microsoft/dotnet-framework-docker/
```

Open the project using Visual Studio.  Add the following to the web.config file, which 
will be used in the next step to specify which environment the OpenTelemetry data 
is associated with: 

````
  <appSettings>
    <add key="OTEL_RESOURCE_ATTRIBUTES" value="deployment.environment=test" />
  </appSettings>
````

Build it with `Build` -> `Rebuild Solution`. 

To publish it to IIS, right-click on the `aspnetapp` project in the Solution Explorer, 
then select `Publish`.  Enter `localhost` as the server name, and `Default Web Site/aspnetapp` as the site 
name, and `http://localhost/aspnetapp` as the destination URL.  Use the `Validate Connection` button 
and ensure you get a successful response. Click `Next` and keep the default settings, and then click `Save` to 
apply the changes.  Then click the `Publish` button to publish the ASP.NET application to IIS. 

Once complete, access the application using http://localhost/aspnetapp.

## Install the Splunk Distribution of OpenTelemetry .NET

Next, we'll install the Splunk Distribution of OpenTelemetry .NET.  Refer to the 
[product documentation](https://docs.splunk.com/observability/en/gdi/get-data-in/application/otel-dotnet/instrumentation/instrument-dotnet-application.html#install-the-splunk-distribution-of-opentelemetry-net-manually) for the latest deployment instructions. 

Execute the following commands in a PowerShell window: 

``` bash
# Download and import the PowerShell module
$module_url = "https://github.com/signalfx/splunk-otel-dotnet/releases/latest/download/Splunk.OTel.DotNet.psm1"
$download_path = Join-Path $env:temp "Splunk.OTel.DotNet.psm1"
Invoke-WebRequest -Uri $module_url -OutFile $download_path
Import-Module $download_path

# Install the Splunk distribution using the PowerShell module
Install-OpenTelemetryCore

# Set up IIS instrumentation
# IIS is restarted as a result
Register-OpenTelemetryForIIS
```

> Note: you may receive an error such as the following when accessing your application after the instrumentation
> is installed: 
> 
> `Loading this assembly would produce a different grant set from other instances. (Exception from HRESULT: 0x80131401)`
> 
> The workaround is to create a new registry DWORD value called `LoaderOptimization` 
> under `HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\.NETFramework` and set the value 1. Perform an `iisreset` and 
> test the application again. 