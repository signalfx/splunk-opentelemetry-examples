# VAST Data Metrics (Work in Progress)

VAST Cluster provides a Prometheus exporter resource in the VMS REST API.
This example shows how we can use a Prometheus receiver running in the
OpenTelemetry Collector to capture metrics from this exporter and send the
metrics to Splunk Observability Cloud.

The example is based on 
[Exporting Metrics to Prometheus](https://support.vastdata.com/s/article/UUID-3678bd47-6b4d-934b-f0a2-2affe335bc52).

## Prerequisites

* VAST version >= 4.5

## Create a Secret

Use the following command to create a Kubernetes secret for your VMS Manager password (add 
the password to the command before running it): 

``` bash
kubectl create secret generic vast-secret --from-literal=vms_manager_password=***
```

## Configure the Prometheus Receiver

Edit the [values.yaml](./values.yaml) and set appropriate values for the following :

* `<EXPORTER_HOST>`: which is the IP that you use to browse to the VAST Web UI
* `<USER_NAME>`: which is the VMS manager user name 

## Install the OpenTelemetry Collector

Use the following command to install the Splunk distribution of the OpenTelemetry Collector,
using the [values.yaml](./values.yaml) file to configure the Prometheus receiver for scraping
VAST Data metrics:

``` bash
helm install --upgrade splunk-otel-collector \
--set="splunkObservability.realm=us1" \
--set="splunkObservability.accessToken=your_splunk_access_token" \
--set="clusterName=vast-cluster" \
--set="environment=vast-cluster" \
-f ./values.yaml \
splunk-otel-collector-chart/splunk-otel-collector
```
