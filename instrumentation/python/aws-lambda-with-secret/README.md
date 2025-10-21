# Instrumenting a Python AWS Lambda Function with OpenTelemetry and AWS Secrets Manager

This example builds on [Instrumenting a Python AWS Lambda Function with OpenTelemetry](../aws-lambda) 
and demonstrates how to utilize AWS Secrets Manager to store the `SPLUNK_ACCESS_TOKEN` value 
securely. 

## Prerequisites

The following tools are required to deploy Python functions into AWS Lambda using SAM:

* An AWS account with permissions to create and execute Lambda functions
* Python 3.12
* Download and install [AWS SAM](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)

## Build and Deploy

Open a command line terminal and navigate to the root of the directory.  
For example:

````
cd ~/splunk-opentelemetry-examples/instrumentation/python/aws-lambda-with-secret
````

### Provide your AWS credentials

````
export AWS_ACCESS_KEY_ID="<put the access key ID here>"
export AWS_SECRET_ACCESS_KEY="<put the secret access key here>"
export AWS_SESSION_TOKEN="<put the session token here>"
````

### Create a Secret

Next, let's create a secret to securely store the access token we'll be using 
to send data to Splunk Observability Cloud: 

> Note: substitute your Splunk access token and target AWS region before running the following command

````
aws secretsmanager create-secret \
  --name "SPLUNK_ACCESS_TOKEN" \
  --secret-string "<put the Splunk access token here>" \
  --region "<put your target AWS region here>"
````

### Define a Wrapper

Next, we'll create a wrapper that copies a [custom collector configuration](./layer/collector-config.yaml) 
to the lambda runtime. The only change in the configuration is to use the 
[Secrets Manager Provider](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/confmap/provider/secretsmanagerprovider/README.md)  
to fetch the `SPLUNK_ACCESS_TOKEN` value: 

``` yaml
    headers:
      "X-SF-TOKEN": "${secretsmanager:SPLUNK_ACCESS_TOKEN}"
```

The custom configuration file is referenced in the `template.yaml` file: 

``` yaml
      Environment: # More info about Env Vars: https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#environment-object
        Variables:
          ...
          OPENTELEMETRY_COLLECTOR_CONFIG_URI: /opt/collector-config.yaml
```

Let's create a ZIP for this layer: 

``` bash
cd layer; zip -r ../copy-custom-collector-config.zip .; cd -
```

Then publish the layer to AWS: 

> Note: substitute your target AWS region before running the following command

``` bash
aws lambda publish-layer-version \
  --layer-name copy-custom-collector-config \
  --zip-file fileb://copy-custom-collector-config.zip \
  --region <target AWS region>
```

### Add the Splunk Lambda layers

Let's first make a copy of the template.yaml.base file:

````
cp template.yaml.base template.yaml
````

#### Add the Splunk OpenTelemetry Lambda Python Layer

Lookup the ARN for your
region, language, and CPU architecture in Step 3 in [this document](https://docs.splunk.com/observability/en/gdi/get-data-in/serverless/aws/otel-lambda-layer/instrumentation/lambda-language-layers.html#install-the-aws-lambda-layer-for-your-language).

Add the ARN to the layers section of the template.yaml file.  For example, here's the
ARN for us-west-1 and Python x86_64:

````
      Layers:
        - arn:aws:lambda:us-west-1:254067382080:layer:splunk-apm-python:16
````

#### Add the Splunk OpenTelemetry Collector layer

Our example deploys the Splunk distribution of the OpenTelemetry collector
to a separate layer within the lambda function.  Lookup the ARN for your
region in Step 6 in [this document](https://docs.splunk.com/observability/en/gdi/get-data-in/serverless/aws/otel-lambda-layer/instrumentation/lambda-language-layers.html#install-the-aws-lambda-layer-for-your-language).

and add the ARN there.  For example,
here's the ARN for us-west-1:

````
      Layers:
        - arn:aws:lambda:us-west-1:254067382080:layer:splunk-apm-python:16
        - arn:aws:lambda:us-west-1:254067382080:layer:splunk-apm-collector:16
````

#### Add the Splunk Metrics Extension Layer

Optionally, we can also add the Splunk Metrics Extension Layer to the template.yaml file.
Lookup the ARN for your
region in Step 7 in [this document](https://docs.splunk.com/observability/en/gdi/get-data-in/serverless/aws/otel-lambda-layer/instrumentation/lambda-language-layers.html#install-the-aws-lambda-layer-for-your-language).

````
      Layers:
        - arn:aws:lambda:us-west-1:254067382080:layer:splunk-apm-python:16
        - arn:aws:lambda:us-west-1:254067382080:layer:splunk-apm-collector:16
        - arn:aws:lambda:us-west-1:254067382080:layer:splunk-lambda-metrics:16
````

### Add the Splunk Observability Cloud Access Token and Realm

We'll also need to specify the realm and `deployment.environment` value for the target
Splunk Observability Cloud environment.  This goes in the template.yaml
file as well:

````
  Environment: 
    Variables:
      SPLUNK_REALM: us1
      OTEL_RESOURCE_ATTRIBUTES: deployment.environment=test
````

### Build the SAM Function

Next, we'll build the function using SAM:

````
sam build
````
### Deploy the SAM Function

Then deploy it:

````
sam deploy --guided
````

You'll be asked a number of questions along the way.  Here are sample responses,
but you should provide the desired stack name and AWS region for your lambda
function.

````
Setting default arguments for 'sam deploy'
=========================================
Stack Name [sam-app]: aws-lambda-python-opentelemetry-example
AWS Region [us-west-1]: us-west-1
#Shows you resources changes to be deployed and require a 'Y' to initiate deploy
Confirm changes before deploy [y/N]: y
#SAM needs permission to be able to create roles to connect to the resources in your template
Allow SAM CLI IAM role creation [Y/n]: y
#Preserves the state of previously provisioned resources when an operation fails
Disable rollback [y/N]: n
HelloWorldFunction has no authentication. Is this okay? [y/N]: y
Save arguments to configuration file [Y/n]: y
SAM configuration file [samconfig.toml]: 
SAM configuration environment [default]: 
````

It will take a few moments for SAM to create all of the objects necessary to
support your lambda function.  Once it's ready, it will provide you with an API
Gateway Endpoint URL that uses the following format:

````
https://${ServerlessRestApi}.execute-api.${AWS::Region}.amazonaws.com/Prod/hello/
````

### Test the SAM Function

Use the API Gateway Endpoint URL provided in the previous step to test the SAM function.
You should see a response such as the following:

````
{"message": "hello world", "location": "54.219.240.80"}
````

### View Traces in Splunk Observability Cloud

After a minute or so, you should start to see traces for the lambda function
appearing in Splunk Observability Cloud:

![Trace](./images/trace.png)

### View Metrics in Splunk Observability Cloud

If you added the Splunk Metrics Extension Layer, you'll also see metrics for your
lambda function by navigating to Infrastructure -> Lambda functions (OTel) and
then selecting your lambda function:

![Trace](./images/lambda-dashboard.png)

### Add Trace Context to Logs

Logs generated by an AWS Lambda function get sent to AWS CloudWatch.
Various methods exist for ingesting logs into Splunk platform from AWS CloudWatch,
such as the solution described in
[Stream Amazon CloudWatch Logs to Splunk Using AWS Lambda](https://www.splunk.com/en_us/blog/platform/stream-amazon-cloudwatch-logs-to-splunk-using-aws-lambda.html).

Once the logs are in Splunk platform, they can be made available to
Splunk Observability Cloud using Log Observer Connect.

To ensure full correlation between traces generated by AWS Lambda instrumentation with metrics and logs,
the Splunk Distribution of OpenTelemetry Python automatically adds trace context to log entries,
such as in the following example:

````
2025-06-02 23:49:46,431 INFO [app] [app.py:27] [trace_id=2b5c8fcec6908f57a7dc747bd00f50f2 span_id=32d9cc8f90531cd4 resource.service.name=aws-lambda-python-opentelemetry-example d trace_sampled=True] - Successfully got the IP address, returning a response.
````

To ensure trace context is injected into log entries, we had to set the following
environment variable in the template.yaml file:

````
OTEL_PYTHON_LOG_CORRELATION: true
````

We also had to include the following package in the requirements.txt file:

````
opentelemetry.instrumentation.logging
````

And configure the `LoggingInstrumentor` in our Lambda function as follows:

```python
import sys
from opentelemetry.instrumentation.logging import LoggingInstrumentor

LoggingInstrumentor().instrument(set_logging_format=True)
FORMAT = '%(asctime)s %(levelname)s [%(name)s] [%(filename)s:%(lineno)d] [trace_id=%(otelTraceID)s span_id=%(otelSpanID)s resource.service.name=%(otelServiceName)s d trace_sampled=%(otelTraceSampled)s] - %(message)s'

logger = logging.getLogger(__name__)
logger.setLevel("INFO")
h = logging.StreamHandler(sys.stdout)
h.setFormatter(logging.Formatter(FORMAT))
logger.addHandler(h)
logging.getLogger().setLevel(logging.INFO)
```