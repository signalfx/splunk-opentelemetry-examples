# Argo Rollouts Integration with Splunk Observability Cloud

"Argo Rollouts is a Kubernetes controller and set of CRDs which provide advanced deployment 
capabilities such as blue-green, canary, canary analysis, experimentation, and progressive 
delivery features to Kubernetes."

Source:  [Argo Rollouts - Kubernetes Progressive Delivery Controller](https://argoproj.github.io/argo-rollouts/)

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

## Argo Rollouts Controller Installation

To start, we'll need a Kubernetes cluster that has the Argo Rollouts controller 
installed. Run the following commands to perform a default install: 

``` bash
kubectl create namespace argo-rollouts
kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml
```

See [Controller Installation](https://argoproj.github.io/argo-rollouts/installation/#controller-installation) 
for customization options. 

## Argo Rollouts Kubectl Plugin Installation

Next, install the kubectl plugin using the following commands (for Linux-based systems):

``` bash
curl -LO https://github.com/argoproj/argo-rollouts/releases/latest/download/kubectl-argo-rollouts-linux-amd64
chmod +x ./kubectl-argo-rollouts-linux-amd64
sudo mv ./kubectl-argo-rollouts-linux-amd64 /usr/local/bin/kubectl-argo-rollouts
```

See [Kubectl Plugin Installation](https://argoproj.github.io/argo-rollouts/installation/#manual)
for additional installation options. 

## Deploy the Job

Argo Rollouts allows a Kubernetes Job to be utilized to as a test, to determine 
whether to continue with the rollout of a new version of software. 

To integrate Argos Rollout with Splunk Observability Cloud, we'll define a job 
that calls a shell script, which in turn uses curl to retrieve metrics from 
Splunk Observability Cloud. The script returns an exit code of zero if the 
metric meets the specified condition. 

### Create a Script to Retrieve Metrics from Splunk

The script can be found in [check_splunk_metric.sh](./check_splunk_metric.sh). 

We can run the script manually as follows: 

``` bash
./check_splunk_metric.sh -r $SPLUNK_REALM -t "$SPLUNK_API_TOKEN" -q 'sf_metric:demo.trans.latency' -o gt -v 200 --scope any
```

> Be sure to define the SPLUNK_REALM and SPLUNK_API_TOKEN environment variables before 
> running the command. 

It should respond with something like the following: 

````
Criteria met (10/18) for query 'sf_metric:demo.trans.latency' with gt 200.
````

### Create a Docker Image 

Next, we'll create a Docker image that will be used by the Job to execute 
the script we just created. You Dockerfile can be found in 
[Dockerfile](./Dockerfile). 

Build the Dockerfile and push it to your repository using the following commands: 

> Note: we're using a local repository for this example; update the below commands with
> your target repository name before running them.

``` bash
docker build -t localhost:9999/check-splunk-metrics:latest .
docker push localhost:9999/check-splunk-metrics:latest
```

### Create a Kubernetes Secret 

Let's create a Kubernetes secret to securely store the API token that 
the script will use to access Splunk Observability Cloud: 

``` bash
kubectl create secret generic splunk-token --from-literal=token=YOUR_SPLUNK_API_TOKEN
```

### Create the Job 

Now, we can create the job, which you can find in [analysis-template.yaml](./analysis-template.yaml). 

``` bash
kubectl apply -f ./analysis-template.yaml
```

### Create the Rollout 

With Argo Rollouts, a "rollout" is used in place of a usual Kubernetes Deployment 
to manage the deployment of an application. 

In this example, we'll deploy the [Door Game sample application](https://github.com/signalfx/splunk-opentelemetry-examples/tree/main/instrumentation/java/linux). 

The [rollout.yaml](./rollout.yaml) refers to the job we created 
earlier to ensure that the rollout doesn't proceed beyond 40% until 
the analysis is run: 

``` yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: splunk-argo-rollouts-example
spec:
  replicas: 3
  strategy:
    canary:
      analysis:
        templates:
          - templateName: check-splunk-metrics
        startingStep: 2 # delay starting analysis run until setWeight: 40%
```

Deploy the application using the rollout manifest as follows: 

``` bash
kubectl apply -f ./rollout.yaml
kubectl apply -f ./service.yaml
```

You can access your application by using your browser to navigate to: 

````
http://<IP address>:81
````

We can monitor progress of the rollout using the following command: 

``` bash
kubectl argo rollouts get rollout splunk-argo-rollouts-example --watch
```

The first time an application is deployed, Argo Rollouts deploys 100% of pods. 

Let's deploy a slightly updated version of our application, which will trigger 
Argo Rollouts to perform analysis using our job and shell script, before moving 
on to the next step of the rollout. 

``` bash
kubectl apply -f ./rollout-update.yaml
```

We can monitor progress of the rollout using the following command:

``` bash
kubectl argo rollouts get rollout splunk-argo-rollouts-example --watch
```

Argo Rollouts will re-deploy the first pod only, and then pause for one 
minute (per the specified configuration). Then, it will perform analysis
by invoking our job, which in turn invokes the script to fetch metrics 
from Splunk Observability Cloud. If the conditions are met, then the 
rollout will proceed to the second pod. 

When the analysis has completed successfully, and the second pod is deployed, 
the output will look something like the following: 

````
NAME                                                                    KIND         STATUS        AGE  INFO
⟳ splunk-argo-rollouts-example                                          Rollout      ॥ Paused      89s  
├──# revision:2                                                                                         
│  ├──⧉ splunk-argo-rollouts-example-6c65f677bc                         ReplicaSet   ✔ Healthy     80s  canary
│  │  └──□ splunk-argo-rollouts-example-6c65f677bc-vkkqz                Pod          ✔ Running     80s  ready:1/1
│  └──α splunk-argo-rollouts-example-6c65f677bc-2                       AnalysisRun  ✔ Successful  18s  ✔ 1
│     └──⊞ 633fc2f0-a6e0-4138-afe7-5042e75b4dc6.check-splunk-metrics.1  Job          ✔ Successful  18s  
└──# revision:1                                                                                         
   └──⧉ splunk-argo-rollouts-example-69b79fd67                          ReplicaSet   ✔ Healthy     89s  stable
      ├──□ splunk-argo-rollouts-example-69b79fd67-6hgtp                 Pod          ✔ Running     89s  ready:1/1
      └──□ splunk-argo-rollouts-example-69b79fd67-tcnb9                 Pod          ✔ Running     89s  ready:1/1
````