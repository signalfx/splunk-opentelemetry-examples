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
* [istioctl](https://istio.io/latest/docs/ops/diagnostic-tools/istioctl/)

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

## Install Istio

Ensure Istio is installed in the Kubernetes cluster used for testing: 

``` bash
helm repo add istio https://istio-release.storage.googleapis.com/charts

helm repo update

helm install istio-base istio/base -n istio-system --set defaultRevision=default --create-namespace

helm install istiod istio/istiod -n istio-system --wait
```

## Enable Istio Injection on Service 1

Next, we'll enable Istio side-car injection on the service 1 namespace (only): 

``` bash
kubectl label namespace svc1 istio-injection=enabled --overwrite
```

Then restart the service 1 deployment to ensure the changes take effect: 

``` bash
kubectl rollout restart deploy/python-istio-example-svc1 -n svc1
```

## Update the Collector Configuration

Now that Istio is installed, we need to update the collector configuration 
to enable Istio auto-detection.  We can do this with the `helm upgrade` command, 
adding `--set=autodetect.istio=true` in addition to the parameters we used 
to install the collector originally: 

``` bash
helm upgrade splunk-otel-collector --set="autodetect.istio=true,splunkObservability.accessToken=<Access Token>,clusterName=<Cluster Name>,splunkObservability.realm=<Realm>,gateway.enabled=false,splunkPlatform.endpoint=https://<HEC URL>:443/services/collector/event,splunkPlatform.token=<HEC token>,splunkPlatform.index=<Index>,splunkObservability.profilingEnabled=true,environment=<Environment Name>" splunk-otel-collector-chart/splunk-otel-collector
```

See [Install and configure the Splunk OpenTelemetry Collector](https://docs.splunk.com/observability/en/gdi/get-data-in/application/istio/istio.html) 
for further details on this step. 

## Configure the Istio Operator

Then we configure the Istio operator to use the Zipkin tracer to send
data to the Splunk OpenTelemetry Collector running on the host.  We'll also 
set the `deployment.environment` attribute, to ensure traces are reported to the
appropriate environment in Splunk Observability Cloud: 

``` bash
istioctl install -f ./tracing.yaml
```
Enable tracing by applying the following configuration:

``` bash
kubectl apply -f - <<EOF
apiVersion: telemetry.istio.io/v1
kind: Telemetry
metadata:
name: mesh-default
namespace: istio-system
spec:
tracing:
- providers:
    - name: "zipkin"
      EOF
```
