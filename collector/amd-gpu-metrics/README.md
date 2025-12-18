# AMD GPU Metrics (Work in Progress)

The [AMD Device Metrics Exporter](https://github.com/ROCm/device-metrics-exporter)
enables real-time collection of telemetry data in Prometheus format from AMD GPUs,
providing comprehensive metrics including temperature, utilization, memory usage, 
power consumption, and more. It's typically installed a part of the 
[AMD GPU Operator](https://instinct.docs.amd.com/projects/gpu-operator/en/latest/installation/kubernetes-helm.html). 

The exporter includes a `/metrics` endpoint that we can scrape with the Prometheus receiver
running in the OpenTelemetry Collector to capture metrics.

The Prometheus receiver can be added by using the values.yaml file like the one found [here](./values.yaml).

## Prerequisites

* Kubernetes 1.29 - 1.33 running on Ubuntu 22.04 LTS or Ubuntu 24.04 LTS 
* Or RedHat OpenShift 4.16 - 4.19 running on Red Hat Core OS
* Helm v3.2.0+ 
* Nodes with AMD MI2xx or AMD MI3xx GPUs.  

## Install the AMD GPU Operator

The AMD GPU Operator simplifies the process of accessing GPU hardware within a Kubernetes cluster.
We'll use the steps from [AMD's Documentation](https://instinct.docs.amd.com/projects/gpu-operator/en/latest/installation/kubernetes-helm.html) 
to install the operator in our cluster using Helm.  

### Install Cert-Manager

The AMD GPU Operator requires cert-manager for TLS certificate management.

Add the cert-manager repository:

``` bash
helm repo add jetstack https://charts.jetstack.io --force-update
```

Install cert-manager:

``` bash
helm install cert-manager jetstack/cert-manager \
--namespace cert-manager \
--create-namespace \
--version v1.15.1 \
--set crds.enabled=true
```

### Installing Operator

#### Add the AMD Helm Repository

``` bash
helm repo add rocm https://rocm.github.io/gpu-operator
helm repo update
```

#### Install the Operator

To install the GPU Operator with the Metrics Exporter, run the following Helm install command:

``` bash
helm install amd-gpu-operator rocm/gpu-operator-charts \
--namespace kube-amd-gpu \
--create-namespace \
--version=v1.4.0 \
--set metricsExporter.enable=true
```

## Install the OpenTelemetry Collector

Use the following command to install the Splunk distribution of the OpenTelemetry Collector,
using the [values.yaml](./values.yaml) file to configure the Prometheus receiver for scraping
AMD GPU metrics:

``` bash
helm install --upgrade splunk-otel-collector \
--set="splunkObservability.realm=us1" \
--set="splunkObservability.accessToken=your_splunk_access_token" \
--set="clusterName=amd-cluster" \
--set="environment=amd-cluster" \
-f ./values.yaml \
splunk-otel-collector-chart/splunk-otel-collector
```
