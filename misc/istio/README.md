# Istio with OpenTelemetry 

This example shows traces can be captured from Istio, and how these traces interoperate 
with traces captured using OpenTelemetry. 

For this example, we'll use two simple Python services:  Service 1, which calls Service 2. 
Calls into and out of service 1 go through an Istio service mesh, while Service 2 
resides outside the service mesh.  Both services are instrumented with the Splunk
Distribution of OpenTelemetry Python. 

## Prerequisites

The following tools are required to run this example: 

* Docker
* Kubernetes
* Helm 3

## Deploy the Splunk OpenTelemetry Collector

This example requires the Splunk Distribution of the OpenTelemetry collector to
be running on the host and available within the Kubernetes cluster.  Follow the
instructions in [Install the Collector for Kubernetes using Helm](https://docs.splunk.com/observability/en/gdi/opentelemetry/collector-kubernetes/install-k8s.html)
to install the collector in your k8s cluster.


Here's an example command that shows how to deploy the collector in Kubernetes using Helm:

``` bash
helm install splunk-otel-collector --set="splunkObservability.accessToken=<Access Token>,clusterName=<Cluster Name>,splunkObservability.realm=<Realm>,gateway.enabled=false,splunkPlatform.endpoint=https://<HEC URL>:443/services/collector/event,splunkPlatform.token=<HEC token>,splunkPlatform.index=<Index>,splunkObservability.profilingEnabled=true,environment=<Environment Name>" splunk-otel-collector-chart/splunk-otel-collector
```

You'll need to substitute your access token, realm, and other information.

## Deploy the Services 

The services can be deployed using the following commands: 

``` bash
kubectl create ns svc1
kubectl create ns svc2
kubectl apply -f svc1.yaml
kubectl apply -f svc2.yaml
```

If running locally, we can use port forward to make service 1 available via localhost:

````
kubectl port-forward service/python-istio-example-svc1 8080:8080 -n svc1
````

Then access it using your browser: 

````
http://localhost:8080/hello
````