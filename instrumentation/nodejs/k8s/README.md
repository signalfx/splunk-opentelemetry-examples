# Instrumenting a Node.js Application in Kubernetes with OpenTelemetry

This example uses the same sample application that is used in the
[nodejs/linux](../linux) example.

## Prerequisites

The following tools are required to build and deploy the Node.js application and the
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
cd ~/splunk-opentelemetry-examples/instrumentation/nodejs/linux
````

### Build the Docker image (optional)

To run the application in K8s, we'll need a Docker image for the application.
We've already built one, so feel free to skip this section unless you want to use
your own image.

You can use the following command to build the Docker image:

````
docker build --platform="linux/amd64" -t helloworld-nodejs:1.0 .
````

Ensure that you use a machine with a linux/amd64 architecture to build the image, as there are issues
with AlwaysOn profiling when the image is built with arm64 architecture. 

Note that the Dockerfile references the `@splunk/otel` package when launching the application: 

````
CMD [ "node", "-r", "@splunk/otel/instrument", "index.js" ]
````

If you'd like to test the Docker image locally you can use:

````
docker run -p 8080:8080 helloworld-nodejs:1.0 
````

Then access the application by pointing your browser to `http://localhost:8080/hello`.

### Push the Docker image (optional)

We'll then need to push the Docker image to a repository that you have
access to, such as your Docker Hub account.  We've already done this for you,
so feel free to skip this step unless you'd like to use your own image.

Specifically, we've pushed the
image to GitHub's container repository using the following commands:

````
docker tag helloworld-nodejs:1.0 ghcr.io/splunk/helloworld-nodejs:1.0
docker push ghcr.io/splunk/helloworld-nodejs:1.0
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
Node.js instrumentation gathers and exports data to the collector running within the cluster:

````
  env:
    - name: PORT
      value: "8080"
    - name: NODE_IP
      valueFrom:
        fieldRef:
          fieldPath: status.hostIP
    - name: OTEL_EXPORTER_OTLP_ENDPOINT
      value: "http://$(NODE_IP):4318"
    - name: OTEL_SERVICE_NAME
      value: "helloworld"
    - name: SPLUNK_METRICS_ENABLED
      value: "true"
    - name: SPLUNK_PROFILER_ENABLED
      value: "true"
    - name: SPLUNK_PROFILER_MEMORY_ENABLED
      value: "true"
````

To test the application, we'll need to get the Cluster IP:

````
kubectl describe svc helloworld | grep IP:
````

Then we can access the application by pointing our browser to `http://<IP Address>:81/hello`.

If you're testing with minikube then use the following command to connect to the service:

````
minikube service helloworld
````

The application should return "Hello, World!". 

### View Traces in Splunk Observability Cloud

After a minute or so, you should start to see traces for the Node.js application
appearing in Splunk Observability Cloud:

![Trace](./images/trace.png)

Note that the trace has been decorated with Kubernetes attributes, such as `k8s.pod.name`
and `k8s.pod.uid`.  This allows us to retain context when we navigate from APM to
infrastructure data within Splunk Observability Cloud.

### View AlwaysOn Profiling Data in Splunk Observability Cloud

You should also see profiling data appear:

![AlwaysOn Profiling Data](./images/profiling.png)

### View Metrics in Splunk Observability Cloud

Node.js runtime metrics are collected automatically.  For example,
the `process.runtime.nodejs.memory.heap.used` metric shows us the amount of heap memory used 
by the node.js process: 

![Node.js Metric Example](./images/metrics.png)

### View Logs with Trace Context

The Splunk Distribution of OpenTelemetry JS automatically adds trace context
to logs when one of the following logging frameworks is used:

* Bunyan
* Pino
* Winston

Here's an example log entry, which includes the trace_id and span_id:

````
{"level":30,"time":1737659257464,"pid":1,"hostname":"helloworld-6785547876-xjhtz","trace_id":"dd70deff50bfe7848e57a4076b871d6a","span_id":"ed19badc4fbbdb8e","trace_flags":"01","service.name":"helloworld","msg":"/hello endpoint invoked, sending response"}
````

The OpenTelemetry Collector can be configured to export log data to
Splunk platform using the Splunk HEC exporter.  The logs can then be made
available to Splunk Observability Cloud using Log Observer Connect.  This will
provide full correlation between spans generated by Node.js instrumentation
with metrics and logs: 

![Node.js Correlated Log Example](./images/logs.png)