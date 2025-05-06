# NVIDIA NIM Operator Metrics

The [NVIDIA NIM Operator](https://docs.nvidia.com/nim-operator/latest/index.html)
enables Kubernetes cluster administrators to operate the software components and 
services necessary to run NVIDIA NIMs. 

Per [NIM Operator Observability](https://docs.nvidia.com/nim-operator/latest/observability.html), 
the NVIDIA NIM operator includes a metrics service named `k8s-nim-operator-metrics-service` 
that exposes a `/metrics` endpoint that we can scrape with the Prometheus receiver
running in the OpenTelemetry Collector to capture metrics.

The Prometheus receiver can be added by using the values.yaml file like the one found [here](./values.yaml).

The endpoint of `k8s-nim-operator-metrics-service.nim-operator.svc.cluster.local:8080` should be updated 
if a different service name, namespace, or port is used by the target environment. 

Note that the configuration is applied to the `clusterReceiver` section of the `values.yaml` configuration. 
This ensures that `k8s-nim-operator-metrics-service` endpoint isn't scraped multiple times, which would 
result in duplicate data. 

Also note that the `k8s-nim-operator-metrics-service` service requires us to pass an authentication token, 
which is handled as follows: 

``` yaml
      authorization:
        type: Bearer
        credentials_file: '/var/run/secrets/kubernetes.io/serviceaccount/token'
```

The changes can then be applied with a command such as the following:

``` bash
helm upgrade splunk-otel-collector \
--set="splunkObservability.realm=$REALM" \
--set="splunkObservability.accessToken=$ACCESS_TOKEN" \
--set="splunkObservability.profilingEnabled=true" \
--set="clusterName=$CLUSTER_NAME" \
--set="environment=$ENVIRONMENT" \
--set="splunkPlatform.token=$HEC_TOKEN" \
--set="splunkPlatform.endpoint=$HEC_URL" \
--set="splunkPlatform.index=$INDEX" \
-f ./values.yaml \
splunk-otel-collector-chart/splunk-otel-collector
```

The list of metrics provided by this endpoint is documented 
[here](https://docs.nvidia.com/nim-operator/latest/observability.html#metrics). 

You can merge the `values.yaml` file in this example with the [nvidia-gpu-metrics](../nvidia-gpu-metrics/README.md) 
example, if the target environment also includes the Nvidia DCGM exporter. 