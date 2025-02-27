# NVIDIA GPU Metrics

The [NVIDIA GPU Operator](https://docs.nvidia.com/datacenter/cloud-native/gpu-operator/latest/index.html) 
is typically deployed in Kubernetes clusters with NVIDIA GPU hardware to simplify the process of making 
this specialized hardware available to pods that require it.

The operator includes a `/metrics` endpoint that we can scrape with the Prometheus receiver 
running in the OpenTelemetry Collector to capture metrics.

The Prometheus receiver can be added by using the values.yaml file like the one found [here](./values.yaml).

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

The resulting metrics provide a wealth of information about our GPU infrastructure and gives us insight into whether the hardware is being used efficiently, or if we’re at risk of running out of GPU capacity, so we can take action before end-users are negatively impacted:  

![NVIDIA GPU Dashboard](./images/GPU%20Dashboard.png)
