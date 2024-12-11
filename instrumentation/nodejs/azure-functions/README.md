# Instrumenting a Node.js Azure Function with OpenTelemetry

This example demonstrates how to instrument an serverless Azure function written in
Node.js using OpenTelemetry, and then export the data to Splunk Observability 
Cloud.  We'll use Node.js v20 for this example, but the steps for other Node.js versions are 
similar.   

## Prerequisites 

The following tools are required to deploy Node.js Azure functions: 

* An Azure account with permissions to create and execute Azure functions
* [Visual Studio Code](https://code.visualstudio.com/)
* Azure Functions extension for Visual Studio Code (installed using Visual Studio Code)

## Application Overview

If you just want to build and deploy the example, feel free to skip this section. 

The application used for this example is a simple Hello World application. 

We updated the [index.js](./src/index.js) file to include code that starts the instrumentation, 
which adds the Azure function OpenTelemetry instrumentation: 

````
const { start } = require('@splunk/otel');
const { getInstrumentations } = require('@splunk/otel/lib/instrumentations');
const { AzureFunctionsInstrumentation } = require('@azure/functions-opentelemetry-instrumentation');

start({
   tracing: {
      instrumentations: [
         ...getInstrumentations(), // Adds default instrumentations
         new AzureFunctionsInstrumentation()
      ],
   },
});
````

We also modified the 
[azure-function-nodejs-opentelemetry-example.js](./src/functions/azure-function-nodejs-opentelemetry-example.js) 
file to start a custom span and add a span attribute to it: 

````
const opentelemetry = require('@opentelemetry/api');

const tracer = opentelemetry.trace.getTracer('azure-function-nodejs-opentelemetry-example', '0.1.0');

app.http('azure-function-nodejs-opentelemetry-example', {
    methods: ['GET', 'POST'],
    authLevel: 'anonymous',
    handler: async (request, context) => {
        return tracer.startActiveSpan('nodejs-azure-http-trigger', (span) => {

            logger.info(`Http function processed request for url "${request.url}"`);

            const name = request.query.get('name') || 'world';
            
            span.setAttribute('app.name', name);
            span.end();

            return { body: `Hello, ${name}!` };
        });
    }
});
````

These code changes required the `@splunk/otel`, `@opentelemetry/api` and 
`@azure/functions-opentelemetry-instrumentation` packages to be installed with npm, which we can see
in the [package.json](./package.json) file: 

````
  "dependencies": {
    "@azure/functions": "^4.5.0",
    "@azure/functions-opentelemetry-instrumentation": "^0.1.0",
    "@opentelemetry/api": "^1.9.0",
    "@splunk/otel": "^2.15.0",
    "pino": "^9.5.0"
  },
````

The [local.settings.json](./local.settings.json) file was then updated to include the Splunk realm and access token which is
used for local testing: 

````
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "node",
    "OTEL_EXPORTER_OTLP_ENDPOINT": "https://ingest.<Realm>.signalfx.com/v2/trace/otlp", 
    "OTEL_EXPORTER_OTLP_HEADERS": "X-SF-TOKEN=<Access Token>", 
    "OTEL_SERVICE_NAME": "azure-function-nodejs-opentelemetry-example", 
    "OTEL_RESOURCE_ATTRIBUTES": "deployment.environment=test" 
  }
````

The [host.json](./host.json) file was also updated to set the `telemetryMode` to `openTelemetry`.  This 
enables OpenTelemetry output from the host where the function runs: 

````
{
  "version": "2.0",
  "logging": {
    "applicationInsights": {
      "samplingSettings": {
        "isEnabled": true,
        "excludedTypes": "Request"
      }
    }
  },
  "telemetryMode": "openTelemetry",
  "extensionBundle": {
    "id": "Microsoft.Azure.Functions.ExtensionBundle",
    "version": "[4.*, 5.0.0)"
  }
````

Note:  while the above setting should be optional, during testing it was observed that 
application traces don't get captured either if this setting is not included.  

## Build and Deploy

Open the following project using Visual Studio Code: 

````
splunk-opentelemetry-examples/instrumentation/nodejs/azure-functions
````

### Create a Function App in Azure 

Create a Function App in Azure if you don't already have one.  For my example, 
I used `opentelemetry-nodejs-examples` as the function name, and used the region of “West US 2” 
with Node.js v20 as the runtime. 

![Azure Function App](./images/azure-function-app.png)

### Create a Deployment Slot (Optional) 

By default, Azure will use a deployment slot named "Production" for an Azure Function App.  
In my example, I created a deployment slot named "test".

![Deployment Slot](./images/deployment-slot.png)

### Set Environment Variables 

To allow OpenTelemetry to send trace data to Splunk Observability Cloud, 
we need to set the `SPLUNK_REALM`, `SPLUNK_ACCESS_TOKEN`, `OTEL_EXPORTER_OTLP_ENDPOINT`, 
and `OTEL_EXPORTER_OTLP_HEADERS` environment variables 
for our Azure Function App: 

![Environment Variables](./images/env-vars.png)

### Build and Deploy the Azure Function

In the Azure section of Visual Studio Code, right click on the deployment slot of interest 
and select `Deploy to Slot`. 

<img src="./images/deploy.png" alt="Deploy" width="200"/>

It will ask you to confirm: 

<img src="./images/confirm-deploy.png" alt="Confirm Deploy" width="200"/>

### Test the Azure Function

Copy the function URL from the Azure function: 

<img src="./images/function-url.png" alt="Function URL" width="200"/>

Then point your browser to that URL, it should return: 

````
Hello, World! 
````

### View Traces in Splunk Observability Cloud

After a minute or so, you should start to see traces for the serverless function
appearing in Splunk Observability Cloud: 

![Trace](./images/trace.png)

Note that there are several traces captured at the function host level by Azure's Application Insights 
that are not directly related to HTTP function requests. 

And in the trace shown above, there are also .NET spans associated with the function host, which 
call into our Node.js function. 

### Add Trace Context to Logs

Logs generated by an Azure function get sent to Application Insights.
Various methods exist for ingesting logs into Splunk platform from Application Insights,
such as the 
[Splunk Add-on for Microsoft Cloud Services](https://splunkbase.splunk.com/app/3110).

Once the logs are in Splunk platform, they can be made available to
Splunk Observability Cloud using Log Observer Connect.

In the following example, 
we can see that the trace context was injected successfully into the logs 
since the Splunk distribution of OpenTelemetry JS automatically handles this when 
`pino` is used for logging: 

````
{
  "level": 30,
  "time": 1733937329972,
  "pid": 5364,
  "hostname": "10-30-16-44",
  "trace_id": "5d36a2495225ca7122a24206cf7261ce",
  "span_id": "24ab25749ad90eb4",
  "trace_flags": "01",
  "service.name": "azure-function-nodejs-opentelemetry-example",
  "service.environment": "test",
  "msg": "Http function processed request for url \"https://opentelemetry-nodejs-examples-test.azurewebsites.net/api/azure-function-nodejs-opentelemetry-example\""
}
````

This will ensure full correlation between traces generated by the OpenTelemetry instrumentation
with metrics and logs. 