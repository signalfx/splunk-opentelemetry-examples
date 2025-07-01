# LiteLLM Proxy Example with Splunk

[LiteLLM Proxy](https://docs.litellm.ai/) provides a centralized, unified interface to more than
100 different types of LLMs (including OpenAI, Anthropic, AWS Bedrock, Google Gemini, etc).
It includes the ability to track LLM usage and cost. 

This example demonstrates how the
[Splunk Distribution of OpenTelemetry Python](https://help.splunk.com/en/splunk-observability-cloud/manage-data/instrument-back-end-services/instrument-back-end-applications-to-send-spans-to-splunk-apm./instrument-a-python-application/about-splunk-otel-python)
can be used to capture metrics and traces from an application that utilizes the LiteLLM Proxy 
to connect to one or more LLMs. 

The metrics and traces are sent to an [OpenTelemetry Collector](https://help.splunk.com/en/splunk-observability-cloud/manage-data/splunk-distribution-of-the-opentelemetry-collector/get-started-with-the-splunk-distribution-of-the-opentelemetry-collector),
which exports the data to [Splunk Observability Cloud](https://www.splunk.com/en_us/products/observability-cloud.html).

This example uses [opentelemetry-instrumentation-openai-v2](https://pypi.org/project/opentelemetry-instrumentation-openai-v2/)
to instrument the application.

## Prerequisites

* Splunk distribution of OpenTelemetry collector running on the host where the example is deployed
* An OpenAI API key 
* Python 3.12
* Docker

## Deploy a Postgres Database 

LiteLLM Proxy uses a PostgreSQL database to store information such as 
Virtual Keys, Organizations, Teams, Users, Budgets, and Per request Usage Tracking. 

Further details can be found in [What is stored in the DB](https://docs.litellm.ai/docs/proxy/db_info). 

We'll use Docker to deploy a Postgres database for this example.  Add a password then run the 
command below to start the Postgres database: 

``` bash
docker run --name local-postgres \
    -p 54320:5432 \
    -e POSTGRES_PASSWORD=*** \
    -e SSL_MODE=disable \
    -d postgres
```

Configure the following environment variable so LiteLLM knows where to find 
this database (substitute the Postgres password before running the command): 

```bash
export DATABASE_URL="postgresql://postgres:***@localhost:54320/litellm"
```

## Install the LiteLLM Proxy

### Setup the Environment 

``` bash
# clone the repo if you haven't already
git clone https://github.com/signalfx/splunk-opentelemetry-examples.git

# navigate to the proxy directory
cd splunk-opentelemetry-examples/gen-ai/lite-llm-proxy/proxy

# create a virtual environment for the proxy 
python3 -m venv venv

# activate the virtual environment for the proxy
source venv/bin/activate

# install litellm proxy and prisma
pip install 'litellm[proxy]'
pip install prisma
```

### Set Additional Environment Variables

Set a few additional environment variables, filling in the desired values for 
`OPENAI_API_KEY`, `LITELLM_MASTER_KEY` and `UI_PASSWORD`:

``` bash
export OPENAI_API_KEY="REPLACE_WITH_YOUR_KEY_VALUE_HERE"

# this is your master key for using the proxy server
export LITELLM_MASTER_KEY=***

# username to sign in to the LiteLLM Admin UI
export UI_USERNAME=o11y

# password to sign in to the LiteLLM Admin UI
export UI_PASSWORD=***
```

### Configure LiteLLM Proxy to Emit OpenTelemetry

We can configure LiteLLM Proxy to emit OpenTelemetry spans by executing the following steps. 

First, install the OpenTelemetry SDK: 

```bash
pip install opentelemetry-api opentelemetry-sdk opentelemetry-exporter-otlp -U
```

Next, tell the SDK to send data to our collector using gRPC: 

```bash
export OTEL_SERVICE_NAME=litellm-proxy
export OTEL_RESOURCE_ATTRIBUTES='deployment.environment=test'
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
```

Note that we've also enabled OpenTelemetry in the [config.yaml](./proxy/config.yaml) file as follows: 

```yaml
litellm_settings:
  callbacks: ["otel"]
````

### Configure LiteLLM Proxy to Expose Prometheus Metrics (Optional)

LiteLLM Proxy can also be configured to expose a Prometheus `/metrics` endpoint. 
Note however that this functionality requires a LiteLLM Enterprise license, as described 
in the [documentation](https://docs.litellm.ai/docs/proxy/prometheus). 

We can enable this in the [config.yaml](./proxy/config.yaml) file as follows:

```yaml
litellm_settings:
  callbacks: ["otel","prometheus"]
````

Then we can add a Prometheus receiver to the OpenTelemetry collector config to scrape these metrics: 

```yaml
  prometheus_simple/litellm-proxy:
    collection_interval: 30s
    endpoint: "127.0.0.1:4000"
    metrics_path: /metrics/
````

### Start the LiteLLM Proxy

Start LiteLLM proxy with Docker using the following command: 

``` bash
litellm --config config.yaml
```

### Access the LiteLLM Admin UI

Navigate to `http://localhost:4000/ui/` to bring up the Admin UI:

![Example trace](./images/litellm-admin-login.png)

Enter the values for `UI_USERNAME` and `UI_PASSWORD` that you set earlier. 

Use the Admin UI to create a new Virtual Key: 

![Example trace](./images/litellm-virtual-keys.png)

Provide a name for the key, map it to the `gpt-3.5-turbo` model, then set a limit 
of 1 request per minute: 

![Example trace](./images/litellm-create-virtual-key.png)

Ensure that you copy the API key once it's provided, as you won't be able to view it again 
using LiteLLM. 

## Run the Application 

Next, we'll run a custom application that uses the LiteLLM proxy instead of 
calling OpenAI's API directly. 

### Setup the Environment

Open another terminal window in the same directory and run the following commands: 

``` bash
# navigate to the application directory
cd splunk-opentelemetry-examples/gen-ai/lite-llm-proxy/app

# create a virtual environment for the application
python3 -m venv venv

# activate the virtual environment for the application
source venv/bin/activate

# install required dependencies 
pip install -r requirements.txt 
```

### Set Environment Variables

Set the following environment variables.  Add the value for the `LITELLM_VIRTUAL_KEY` copied above 
before running the following commands: 

``` bash
export OTEL_SERVICE_NAME=litellm-proxy-app
export OTEL_RESOURCE_ATTRIBUTES='deployment.environment=test'
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT=true
export LITELLM_VIRTUAL_KEY=***
```

### Launch the Application

Finally, we can launch the application which uses the LiteLLM proxy to invoke OpenAI: 

``` bash
opentelemetry-instrument python3 app.py
```

You should see traces in Splunk Observability Cloud that look like the following:

![Example trace](./images/trace.png)

Prompt details are available on the AI Events tab on the right-hand side of the screen:

![Prompt details](./images/prompt-details.png)

We can also view any log entries related to this trace by clicking on the Logs button
at the bottom right of the trace:

![Related logs](./images/related-logs.png)

Virtual key usage can be tracked using the LiteLLM Admin UI:

![Related logs](./images/litellm-virtual-key-usage.png)

If Prometheus metrics are enabled, we can also track virtual key usage plus much 
more using Splunk Observability Cloud. 