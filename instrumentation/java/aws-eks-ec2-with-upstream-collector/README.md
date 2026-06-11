# Instrumenting a Java Application in Amazon EKS EC2 with the Upstream OpenTelemetry Collector

This example uses the same sample application that is used in the
[java/linux](../linux) example.

It also uses the Docker image that was built for this sample application
in the [java/k8s](../k8s) example.  If you'd like to build your own image,
then please follow the steps in that example to do so.

Otherwise, we'll demonstrate how to use the existing Docker image that's
hosted in GitHub's container repository to deploy a Java application in an 
Amazon EKS EC2 cluster.

## Prerequisites

The following tools are required to build and deploy the Java application and the
Splunk OpenTelemetry Collector:

* Kubernetes
* Helm
* An AWS account with an EKS EC2 cluster and appropriate permissions

## Introduction to Amazon EKS

Amazon Elastic Kubernetes Service (Amazon EKS) is a managed Kubernetes service
that allows you to deploy and scale containerized applications.

It comes in two flavors:

* EC2: containers are deployed onto EC2 instances that are provisioned for your EKS cluster
* Fargate: containers are deployed in a serverless manner

We'll demonstrate how to deploy the Java application and the upstream OpenTelemetry collector
using EKS EC2. Refer to [Instrumenting a Java Application in Amazon EKS Fargate with OpenTelemetry](../aws-eks-fargate) 
for an example using EKS Fargate. 

If you don't already have an EKS EC2 cluster provisioned, follow the instructions in 
[Get started with Amazon EKS – eksctl](https://docs.aws.amazon.com/eks/latest/userguide/getting-started-eksctl.html) 
to do so. 

In my case, I've used the following command to create the EKS cluster: 

```bash
eksctl create cluster -f ./eks-cluster.yaml
```

`eksctl` adds the new cluster’s configuration to `~/.kube/config`, so once the provisioning is complete, 
you should be able to use `kubectl` to connect to it:

```bash
kubectl get nodes
````

## Install the AWS Load Balancer Controller

This example assumes that the AWS Load Balancer Controller is installed in the EKS cluster. 

See 
[Install AWS Load Balancer Controller with Helm](https://docs.aws.amazon.com/eks/latest/userguide/lbc-helm.html) 
to install it, if required.

## Deploy the OpenTelemetry Collector

Next, let's deploy the OpenTelemetry Collector in the cluster. For this example, 
we'll use the [OpenTelemetry Collector Contrib Distro](https://github.com/open-telemetry/opentelemetry-collector-releases/tree/main/distributions/otelcol-contrib). 

We'll use the manifests located in the [collector_k8s_manifests](./collector_k8s_manifests) folder 
to deploy the collector, which have been configured with exporters to send data to Splunk. 

First, create a secret with your Splunk access and HEC tokens: 

> Note: substitute your Splunk Observability Access and HEC tokens before running the command

```bash
kubectl create secret generic otel-contrib-collector \
  --from-literal=splunk_observability_access_token=your_splunk_observability_access_token \
  --from-literal=splunk_hec_token=your_splunk_hec_token
```

Next, define an environment variable with the target Splunk Observability Cloud realm. For example: 

```bash
export SPLUNK_REALM=us1
````

Then, create a config map with other Splunk variables: 

> Note: substitute your target HEC URL, index, cluster name, and deployment environment before running the command

```bash
kubectl create configmap otel-contrib-configmap \
--from-literal=splunk_trace_url="https://ingest.$SPLUNK_REALM.signalfx.com/v2/trace/otlp" \
--from-literal=splunk_api_url="https://api.$SPLUNK_REALM.signalfx.com" \
--from-literal=splunk_ingest_url="https://ingest.$SPLUNK_REALM.signalfx.com" \
--from-literal=splunk_hec_url="[URL]:[PORT]/services/collector/event" \
--from-literal=splunk_index="[INDEX]" \
--from-literal=k8s_cluster_name="[CLUSTER_NAME]" \
--from-literal=deployment_environment="[DEPLOYMENT_ENVIRONMENT]"
````

Now we can deploy the collector in our EKS EC2 cluster with the following command: 

````
kubectl apply -f collector_k8s_manifests/
````

Ensure the collector pods are running: 

```bash
kubectl get pods -l app=otel-collector
```

You should see output such as the following: 

````
NAME                                                           READY   STATUS    RESTARTS   AGE
otel-contrib-collector-agent-qlpdv                             1/1     Running   0          100s
otel-contrib-collector-k8s-cluster-receiver-56d786db46-245kj   1/1     Running   0          100s
````

## Build and Execute the Application

Next, let's deploy the application to the EKS cluster. 

Open a command line terminal and navigate to the root of the directory.  
For example:

````
cd ~/splunk-opentelemetry-examples/instrumentation/java/aws-eks-ec2-with-upstream_collector
````

### Deploy to Kubernetes

Now that we have our Docker image, we can deploy the application to
our Kubernetes cluster.  We'll do this by using the following
kubectl command to deploy the doorgame-eks.yaml manifest file:

````
kubectl apply -f ./doorgame-eks.yaml
````

The Docker image already includes the `splunk-otel-javaagent.jar` file, and adds it
to the Java startup command.  The `doorgame-eks.yaml` manifest file adds to this
configuration by setting the following environment variables, to configure how the
Java agent gathers and exports data to the collector running within the cluster:

````
  env:
    - name: PORT
      value: "9090"
    - name: OTEL_EXPORTER_OTLP_ENDPOINT
      value: "http://otel-contrib-collector-agent:4318"
    - name: OTEL_SERVICE_NAME
      value: "doorgame"
    - name: OTEL_PROPAGATORS
      value: "tracecontext,baggage"
    - name: SPLUNK_PROFILER_ENABLED
      value: "true"
    - name: SPLUNK_PROFILER_MEMORY_ENABLED
      value: "true"
````

Note that we've used the collector service DNS name to populate `OTEL_EXPORTER_OTLP_ENDPOINT`. 

To test the application, we'll need to get the Load Balancer DNS name: 

````
kubectl get ingress   
````

It will return something like the following: 

````
NAME               CLASS   HOSTS   ADDRESS                                                                 PORTS   AGE
doorgame-ingress   alb     *       k8s-default-doorgame-4417858106-224637253.eu-west-1.elb.amazonaws.com   80      5m15s
````

In some cases, the output of the following command might instead look like the following: 

````
NAME               CLASS   HOSTS   ADDRESS   PORTS   AGE
doorgame-ingress   alb     *                 80      8m46s
````

You can use the following command to troubleshoot: 

````
kubectl describe ingress doorgame-ingress
````

Then we can access the application by pointing our browser to `http://k8s-default-doorgame-4417858106-224637253.eu-west-1.elb.amazonaws.com`.

Note that it will take a few minutes for the load balancer to be created in AWS. 

The application should look like the following:

![Door Game Entry Screen](../linux/images/door_game_choose_door.png)

### View Traces in Splunk Observability Cloud

After a minute or so, you should start to see traces for the Java application
appearing in Splunk Observability Cloud:

![Trace](./images/trace.png)

Note that the trace has been decorated with Kubernetes attributes, such as `k8s.pod.name`
and `k8s.pod.uid`.  This allows us to retain context when we navigate from APM to
infrastructure data within Splunk Observability Cloud.

### View Metrics in Splunk Observability Cloud

Metrics are collected by splunk-otel-javaagent.jar automatically.  For example,
the `jvm.memory.used` metric shows us the amount of memory used in the JVM
by type of memory:

![JVM Metric Example](./images/metrics.png)

### View Logs with Trace Context

The Splunk Distribution of OpenTelemetry Java automatically adds trace context
to logs.  

We configured the OpenTelemetry Collector to export log data to
Splunk platform using the Splunk HEC exporter.  The logs can then be made
available to Splunk Observability Cloud using Log Observer Connect.  This will
provide full correlation between spans generated by Java instrumentation
with metrics and logs.

Here's an example of what that looks like. We can see that the trace includes a
Related Content link at the bottom right:

![Trace with Related Content](./images/trace_with_related_content.png)

Clicking on this link brings us to Log Observer Connect, which filters on log entries
related to this specific trace:

![Log Observer Connect](./images/log_observer_connect.png)
