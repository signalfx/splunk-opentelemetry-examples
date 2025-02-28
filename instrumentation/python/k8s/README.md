# Instrumenting a Python Application in Kubernetes with OpenTelemetry

This example uses the same sample application that is used in the
[python/linux](../linux) example.

## Prerequisites

The following tools are required to build and deploy the Python application and the
Splunk OpenTelemetry Collector:

* Docker
* Kubernetes
* Helm 3

## Deploy the Splunk OpenTelemetry Collector

This example requires the Splunk Distribution of the OpenTelemetry collector to
be running on the host and available within the Kubernetes cluster.  Follow the
instructions in [Install the Collector for Kubernetes using Helm](https://docs.splunk.com/observability/en/gdi/opentelemetry/collector-kubernetes/install-k8s.html)
to install the collector in your k8s cluster.

If you'd like to capture logs from
the Kubernetes cluster, ensure the HEC URL and HEC token are provided when the
collector is deployed.

Here's an example command that shows how to deploy the collector in Kubernetes using Helm:

````
helm install splunk-otel-collector --set="splunkObservability.accessToken=<Access Token>,clusterName=<Cluster Name>,splunkObservability.realm=<Realm>,gateway.enabled=false,splunkPlatform.endpoint=https://<HEC URL>:443/services/collector/event,splunkPlatform.token=<HEC token>,splunkPlatform.index=<Index>,splunkObservability.profilingEnabled=true,environment=<Environment Name>" splunk-otel-collector-chart/splunk-otel-collector
````

You'll need to substitute your access token, realm, and other information.

## Build and Execute the Application

Open a command line terminal and navigate to the root of the directory.  
For example:

````
cd ~/splunk-opentelemetry-examples/instrumentation/python/linux
````

### Build the Docker image (optional)

To run the application in K8s, we'll need a Docker image for the application.
We've already built one, so feel free to skip this section unless you want to use
your own image.

You can use the following command to build the Docker image:

````
docker build --platform="linux/amd64" -t helloworld-python:1.1 .
````

Ensure that you use a machine with a linux/amd64 architecture to build the image, as there are issues
with AlwaysOn profiling when the image is built with arm64 architecture.

Note that the Dockerfile references `opentelemetry-instrument` when launching the application:

````
CMD ["opentelemetry-instrument", "flask", "run", "--host", "0.0.0.0", "-p", "8080"]
````

It also includes the `--host` argument to ensure the flask application is visible
within the Kubernetes network (and not from just the container itself). 

If you'd like to test the Docker image locally you can use:

````
docker run -p 8080:8080 -e OTEL_SERVICE_NAME=helloworld-python -e SPLUNK_PROFILER_ENABLED=true helloworld-python:1.1
````

Then access the application by pointing your browser to `http://localhost:8080/hello`.

### Push the Docker image (optional)

We'll then need to push the Docker image to a repository that you have
access to, such as your Docker Hub account.  We've already done this for you,
so feel free to skip this step unless you'd like to use your own image.

Specifically, we've pushed the
image to GitHub's container repository using the following commands:

````
docker tag helloworld-python:1.1 ghcr.io/splunk/helloworld-python:1.1
docker push ghcr.io/splunk/helloworld-python:1.1
````

### Deploy to Kubernetes

Now that we have our Docker image, we can deploy the application to
our Kubernetes cluster.  We'll do this by using the following
kubectl command to deploy the helloworld.yaml manifest file:

````
kubectl apply -f ./helloworld.yaml
````

The helloworld.yaml manifest file adds to this
configuration by setting the following environment variables, to configure how the
Python instrumentation gathers and exports data to the collector running within the cluster:

````
  env:
    - name: PORT
      value: "8080"
    - name: NODE_IP
      valueFrom:
        fieldRef:
          fieldPath: status.hostIP
    - name: OTEL_EXPORTER_OTLP_ENDPOINT
      value: "http://$(NODE_IP):4317"
    - name: OTEL_SERVICE_NAME
      value: "helloworld-python"
    - name: OTEL_RESOURCE_ATTRIBUTES
      value: "deployment.environment=test"
    - name: OTEL_PYTHON_DISABLED_INSTRUMENTATIONS
      value: "click"
    - name: OTEL_LOGS_EXPORTER
      value: "otlp"
    - name: OTEL_PYTHON_LOGGING_AUTO_INSTRUMENTATION_ENABLED
      value: "true"
    - name: OTEL_PYTHON_LOG_LEVEL
      value: "info"
    - name: SPLUNK_PROFILER_ENABLED
      value: "true"
````

To test the application, we'll need to get the Cluster IP:

````
kubectl describe svc helloworld-python | grep IP:
````

Then we can access the application by pointing our browser to `http://<IP Address>:81/hello`.

If you're testing with minikube then use the following command to connect to the service:

````
minikube service helloworld-python
````

The application should return "Hello, World!".

### View Traces in Splunk Observability Cloud

After a minute or so, you should start to see traces for the Python application
appearing in Splunk Observability Cloud:

![Trace](./images/trace.png)

Note that the trace has been decorated with Kubernetes attributes, such as `k8s.pod.name`
and `k8s.pod.uid`.  This allows us to retain context when we navigate from APM to
infrastructure data within Splunk Observability Cloud.

### View AlwaysOn Profiling Data in Splunk Observability Cloud

You should also see profiling data appear:

![AlwaysOn Profiling Data](./images/profiling.png)

### View Metrics in Splunk Observability Cloud

Metrics are collected by the Splunk Distribution of OpenTelemetry Python automatically.  For example,
the `process.runtime.cpython.memory` metric shows us the amount of memory used by the
Python process:

![Python Runtime Metric Example](./images/metrics.png)

### View Logs with Trace Context

The Splunk Distribution of OpenTelemetry Python automatically adds trace context
to logs when the standard `logging` library is used.

Here's an example log entry, which includes the trace_id and span_id:

````
2024-11-21 18:07:38,201 INFO [app] [app.py:11] [trace_id=661c69dfc80b24ec393977ecae417a0b span_id=b93b64999af65757 resource.service.name=helloworld-python trace_sampled=True] - Handling the /hello request
````

The OpenTelemetry Collector can be configured to export log data to
Splunk platform using the Splunk HEC exporter.  The logs can then be made
available to Splunk Observability Cloud using Log Observer Connect.  This will
provide full correlation between spans generated by Python instrumentation
with metrics and logs.

![Trace Log Correlation Example](./images/trace_log_correlation.png)
