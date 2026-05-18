# Microsoft Foundry Agent Service with OpenTelemetry and Splunk (Work in Progress)

[Foundry Agent Service](https://learn.microsoft.com/en-us/azure/foundry/agents/overview) 
is a "fully managed platform for building, deploying, and scaling AI agents."

This example shows how we can instrument an agent built with **Foundry Agent Service** 
with **OpenTelemetry** and send the resulting data to **Splunk Observability Cloud**. 

## Prerequisites

* Active **Microsoft Azure** subscription
* Permissions to create agents in **Azure Foundry**

## Create an Agent

Create a simple prompt agent named `demo-agent` using Microsoft Foundry. For our example, we've included  
system instructions that state the following: 

````
You're a helpful assistant that only answers questions related to Observability.
````

![Create Agent](./images/demo-agent.png)

## Enable Tracing 

Navigate to the `Traces` tab and ensure tracing is enabled for your agent. 

## Test the Agent 

In this section, we'll use a Python application to connect to our agent as a client. 
Create a virtual environment and install the required packages: 

```bash
cd client
python3 -m venv venv
source ./venv/bin/activate
pip install azure-ai-projects
```

Set the Azure Foundry project endpoint: 

> Note: set your Azure Foundry DNS name and project name before running the command below

```bash
export AZURE_FOUNDRY_PROJECT_ENDPOINT="https://<DNS name>.services.ai.azure.com/api/projects/<project name>"
```

Use the following command to run the application: 

```bash
python app.py
```

## View Trace in Azure 

In Azure Foundry, navigate to the `Traces` tab for your agent. You should see a trace collected 
for your agent that looks like the following: 

![App Insights Trace](./images/app-insights-trace.png)

At this point, we've successfully collected a trace for our agent. Next, 
let's walk through the steps required to get the trace into Splunk 
Observability Cloud. 