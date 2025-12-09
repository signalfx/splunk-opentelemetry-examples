# OpenTelemetry Collector in K8s with Gateway (Draft)

This example shows how to deploy the Splunk distribution of the OpenTelemetry Collector 
in a Kubernetes cluster with a Gateway. Per the 
[documentation](https://help.splunk.com/en/splunk-observability-cloud/manage-data/splunk-distribution-of-the-opentelemetry-collector/get-started-with-the-splunk-distribution-of-the-opentelemetry-collector/get-started-understand-and-use-the-collector/deployment-modes):

"A Collector in gateway mode collects data both from Kubernetes and from one or more Collectors
running in standalone agent mode and sends it to Splunk Observability Cloud."

It also demonstrates how to configure the gateway collector to send the data to two 
Splunk instances at the same time. 

## Create a Namespace

Create a new Kubernetes namespace for the collector: 

``` bash
kubectl create ns splunk-otel
```

## Create a Secret

Next, create a Kubernetes secret for access and HEC tokens used to send data to primary Splunk instances: 

> Note: ensure you substitute your access token and HEC token before running the provided command 

``` bash
kubectl create secret generic splunk-secret -n splunk-otel --from-literal=splunk_observability_access_token=your_access_token --from-literal=splunk_platform_hec_token=your_hec_token
```

Then, create a Kubernetes secret for access and HEC tokens used to send data to *alternate* Splunk instances:

> Note: ensure you substitute your access token and HEC token before running the provided command

``` bash
kubectl create secret generic splunk-alternate-backend --from-literal=splunk_observability_access_token=your_access_token --from-literal=splunk_platform_hec_token=your_hec_token
```

## Configure the Collector

Update the [values.yaml](./values.yaml) file and add the desired Splunk Observability realm and
HEC URL.

You may also want to adjust the replica count and resource limits for the gateway collector. 
The default setting is a replica count of 3, a cpu setting of 4 and a memory setting of 8Gi. 
However, this example has been updated to use reduced values. 

## Install the Collector

Add the Splunk OpenTelemetry Collector for Kubernetes' Helm chart repository

``` bash
helm repo add splunk-otel-collector-chart https://signalfx.github.io/splunk-otel-collector-chart
```

Ensure the latest state of the repository:

``` bash
helm repo update
```

Install the Splunk OpenTelemetry Collector for Kubernetes with the desired configuration values: 

``` bash
helm upgrade --install splunk-otel-collector -n splunk-otel -f ./values.yaml splunk-otel-collector-chart/splunk-otel-collector
```