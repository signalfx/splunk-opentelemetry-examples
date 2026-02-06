# LangGraph Example with Splunk

This example demonstrates how the
[Splunk Distribution of OpenTelemetry Python](https://help.splunk.com/en/splunk-observability-cloud/manage-data/instrument-back-end-services/instrument-back-end-applications-to-send-spans-to-splunk-apm./instrument-a-python-application/about-splunk-otel-python)
can be used to capture metrics and traces from an application that utilizes 
[LangGraph](https://www.langchain.com/langgraph).

The metrics and traces are sent to an [OpenTelemetry Collector](https://help.splunk.com/en/splunk-observability-cloud/manage-data/splunk-distribution-of-the-opentelemetry-collector/get-started-with-the-splunk-distribution-of-the-opentelemetry-collector),
which exports the data to [Splunk Observability Cloud](https://www.splunk.com/en_us/products/observability-cloud.html).

It uses a sample application with three nodes:

* A teacher, who assigns math problems
* A student, who completes the math assignments
* A teaching assistant, who grades the math assignments

## Prerequisites

* Splunk distribution of OpenTelemetry collector running on the host where the example is deployed
* An OpenAI API key
* Python 3.12
* [uv Package Manager](https://docs.astral.sh/uv/guides/install-python/#installing-a-specific-version)

## Setup the Environment

``` bash
# clone the repo if you haven't already
git clone https://github.com/signalfx/splunk-opentelemetry-examples.git

# navigate to the directory repo
cd splunk-opentelemetry-examples/gen-ai/langgraph/math_problems

```

## Setup the New Project (Optional)

We first installed LangGraph with the following command:

``` bash
# install LangGraph and LangChain OpenAI
uv add langgraph
uv add langchain_openai
```

We then installed the Splunk Distribution of OpenTelemetry Python, along with OpenLit,
which enhances spans with additional details:

``` bash
uv add splunk-opentelemetry
uv add openlit
uv add splunk-otel-util-genai-translator-openlit
```

We also added the `splunk-otel-util-genai-translator-openlit` package, which translates
GenAI attributes from OpenLIT into OpenTelemetry GenAI semantic conventions.

Then we ran the following command to add additional instrumentation packages:

``` bash
uv run opentelemetry-bootstrap -a requirements | uv pip install --requirement -
```

Note that there's no need to run these commands a second time, as the code
has already been generated.

## Set Environment Variables

``` bash
export OPENAI_API_KEY="REPLACE_WITH_YOUR_KEY_VALUE_HERE"
export OTEL_SERVICE_NAME=langgraph-example
export OTEL_RESOURCE_ATTRIBUTES=deployment.environment=test
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_PYTHON_DISABLED_INSTRUMENTATIONS=click
export OTEL_LOGS_EXPORTER=otlp
export OTEL_PYTHON_LOG_LEVEL=info
export OTEL_PYTHON_LOGGING_AUTO_INSTRUMENTATION_ENABLED=true
```

## Run the Application

Execute the following command to run the application:

``` bash
uv run opentelemetry-instrument python src/app.py
```

You should see traces in Splunk Observability Cloud that look like the following:

![Example trace](./images/trace.png)

Prompt details are available on the right-hand side of the screen as Span Events:

![Prompt details](./images/prompt-details.png)
