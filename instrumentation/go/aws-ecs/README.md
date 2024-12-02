# Instrumenting a Go Application in Amazon ECS with OpenTelemetry

This example uses the same sample application that is used in the
[go/linux](../linux) example.

It also uses the Docker image that was built for this sample application
in the [go/k8s](../k8s) example.  If you'd like to build your own image,
then please follow the steps in that example to do so.

Otherwise, we'll demonstrate how to use the existing Docker image that's
hosted in GitHub's container repository to deploy a Go application in
Amazon ECS.

## Prerequisites

The following tools are required to build and deploy the Go application and the
Splunk OpenTelemetry Collector:

* Docker
* Kubernetes
* Helm 3
* An AWS account with an ECS cluster and appropriate permissions

## Introduction to Amazon ECS

Amazon Elastic Container Service (Amazon ECS) is a managed orchestration service
that allows you to deploy and scale containerized applications.

It comes in two flavors:

* EC2: containers are deployed onto EC2 instances that are provisioned for your ECS cluster
* Fargate: containers are deployed in a serverless manner

We'll demonstrate how to deploy the Go application and OpenTelemetry collector
using ECS Fargate, however EC2 is similar.

## Add the Splunk Distribution of OpenTelemetry Go

To instrument a Go service running in ECS Fargate with OpenTelemetry, we need to add the 
`splunk-otel-go` module.  

We've already done this in the [go/k8s](../k8s) example, and the container image
is available with an image name of `ghcr.io/splunk/helloworld-go:1.0`.

## Update the ECS Task Definition

The next step is to update the ECS Task definition for our application.

For our application container, we first need to add several environment variables:

````
   "environment": [
       {
           "name": "OTEL_SERVICE_NAME",
           "value": "helloworld-go-ecs"
       },
       {
           "name": "OTEL_RESOURCE_ATTRIBUTES",
           "value": "deployment.environment=test,service.version=1.0"
       },
       {
           "name": "SPLUNK_PROFILER_ENABLED",
           "value": "true"
       },
   ],
````

We then need to add a second container to the ECS task definition for the
Splunk distribution of the OpenTelemetry Collector:

````
   "name": "splunk-otel-collector",
   "image": "quay.io/signalfx/splunk-otel-collector:latest",
   "cpu": 0,
   "portMappings": [],
   "essential": true,
   "environment": [
       {
           "name": "SPLUNK_CONFIG",
           "value": "/etc/otel/collector/fargate_config.yaml"
       },
       {
           "name": "SPLUNK_REALM",
           "value": "<Realm - us0, us1, etc>"
       },
       {
           "name": "SPLUNK_ACCESS_TOKEN",
           "value": "<Access Token>"
       },
       {
           "name": "ECS_METADATA_EXCLUDED_IMAGES",
           "value": "[\"quay.io/signalfx/splunk-otel-collector:latest\"]"
       }
````

We've prepared a [task-definition.json](./task-definition.json) file that you can
use as an example.  Open this file for editing, and replace the:

* \<Splunk Realm\>
* \<Access Token\>
* \<AWS Region\>
* \<AWS Account ID\>

placeholders with appropriate values for your environment.

## Deploy to Amazon ECS

We have what we need now to deploy our task definition to Amazon ECS.

So navigate to the AWS console and go to the Amazon Elastic Container Service page.  Assuming
that you've already got an ECS cluster setup, click on Task definitions and then
Create a new task definition from JSON.  Copy and paste your task-definition.json file as
in the following screenshot:

![Create Task Definition](./images/create-ecs-task-definition.png)

Once the task definition is created successfully, navigate to the ECS cluster
where you'd like to deploy the application, then create a new service:

Specify "FARGATE" as the launch type:

![Compute configuration](./images/compute-configuration.png)

Then configure the service deployment as follows:

![Deployment configuration](./images/deployment-configuration.png)

While this goes beyond the scope of this example, you may need to configure
the networking details for the service, such as the VPC and subnet it belongs to,
as well as the security group to allow traffic on port 8080.  We'll configure
the service to use a public IP address and put it in a public subnet for our testing,
though in production it would be better to put a load balancer in front of the service. Refer to
[Connect Amazon ECS applications to the internet](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/networking-outbound.html) for
further details.

It will take a few minutes to deploy the service.  But once it's up and running,
it should look like this in the AWS console:

![ECS Service](./images/ecs-service.png)

Let's get the IP address for the helloworld-Go container:

![ECS container details](./images/ecs-container-details.png)

If you're using a load balancer for your deployment, then use the load balancer IP instead.

Point your browser to http://\<ECS IP Address\>:8080/hello.

The application should return "Hello, World!".

### View Traces in Splunk Observability Cloud

After a minute or so, you should start to see traces for the Go application
appearing in Splunk Observability Cloud:

![Trace](./images/ecs-trace.png)

Note that the trace has been decorated with ECS attributes, such as `aws.ecs.cluster.arn`.  
This allows us to retain context when we navigate from APM to
infrastructure data within Splunk Observability Cloud.

### View Metrics in Splunk Observability Cloud

Metrics are collected by the Splunk Distribution of OpenTelemetry Go automatically.  For example,
the `runtime.go.mem.heap_alloc` metric shows us the bytes of heap objects allocated by the  
Go process:

![Go Runtime Metric Example](./images/metrics.png)

### View Logs with Trace Context

With the default ECS task configuration, any logs generated by an ECS task are sent to AWS CloudWatch.

Our application uses [zap](https://github.com/uber-go/zap) for logging, and trace context was added 
to the code as explained in [go/linux](../linux). 

Here's an example log entry, which includes the trace_id and span_id:

````
{
    "level": "info",
    "ts": 1733175861.1912444,
    "caller": "app/main.go:33",
    "msg": "In httpHandler()",
    "trace_id": "eb2c0895bb66e39cd0f45830b91044e8",
    "span_id": "ae3f72e53af29e24",
    "trace_flags": "01"
}
````

The logs can be ingested into Splunk platform from AWS CloudWatch, and then
made available to Splunk Observability Cloud using Log Observer Connect. 
