# AMD GPU Metrics (Work in Progress)

The [AMD Device Metrics Exporter](https://github.com/ROCm/device-metrics-exporter)
enables real-time collection of telemetry data in Prometheus format from AMD GPUs,
providing comprehensive metrics including temperature, utilization, memory usage, 
power consumption, and more. It's typically installed a part of the 
[AMD GPU Operator](https://instinct.docs.amd.com/projects/gpu-operator/en/latest/installation/kubernetes-helm.html). 

The exporter includes a `/metrics` endpoint that we can scrape with the Prometheus receiver
running in the OpenTelemetry Collector to capture metrics.

The Prometheus receiver can be added by using the values.yaml file like the one found [here](./values.yaml).

## Provision an Amazon EKS Cluster with AMD GPU Hardware

We'll use an EKS cluster with AMD GPU hardware for our example. If you already have a 
Kubernetes cluster with AMD GPUs, you can skip this section and proceed to install 
the OpenTelemetry collector. 

### Prerequisites

* Access to an AWS account with permissions to create an EKS cluster, VPC, etc.
* The AWS CLI
* Helm
* The eksctl command line utility needs to be installed on your workstation (see the [Installation guide](https://eksctl.io/installation/)).
* A key pair that you can use to connect to Amazon EC2 instances.

### Configure the AWS CLI

Use one of the methods described in [Configuration and credentials precedence](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html)
to authenticate with AWS. For example, you can do this by setting the following environment variables in your command-line
terminal, as described in [How to set environment variables](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-envvars.html):

``` bash
export AWS_ACCESS_KEY_ID=***
export AWS_SECRET_ACCESS_KEY=***
export AWS_DEFAULT_REGION=us-west-2
```

### Create an EKS Fargate Cluster

A key pair is required to SSH to nodes in the EKS cluster once it's provisioned.
Update the [eks-cluster-config.yaml](./eks-cluster-config.yaml) file with the location
of your public key:

``` yaml
    ssh:
      allow: true
      publicKeyPath: ~/.ssh/ec2_id_rsa.pub
```

If you need to get the public key portion of your key pair from AWS, you can use the following
command to do so:

``` bash
aws ec2 describe-key-pairs --key-names <key-pair-name> --include-public-key
```

Save the contents to a file with a `.pub` extension, and update the `publicKeyPath` in the
[eks-cluster-config.yaml](./eks-cluster-config.yaml) file to point to it.

We've created a single node cluster with 100 GB disk space, but this can be increased as needed by
updating the following settings:

``` yaml
nodeGroups:
  - name: eks-gpu-demo-workers
    instanceType: g4ad.xlarge
    ami: ami-096e26b9d25115941
    amiFamily: Ubuntu2204
    minSize: 1
    desiredCapacity: 1
    maxSize: 1
    volumeSize: 100
```

> Note:  at the time of writing, and single `g4ad.xlarge` instance will cost around $0.54117/hour
> to run in us-west-2.

Next, we can create the EKS cluster with the following command:

> Note: it will take at least 15-20 minutes to provision the cluster

``` bash
eksctl create cluster -f ./eks-cluster-config.yaml
```

`eksctl` adds the new cluster’s configuration to `~/.kube/config`, so once the provisioning is complete,
you should be able to use `kubectl` to connect to it:

``` bash
kubectl get nodes
````

### Lock Down Instances

By default, the cluster is configured to allow SSH access to all nodes from 0.0.0.0/0.  It also allows
access to the cluster API endpoint from 0.0.0.0/0.

Locking both of these down is desirable.

Starting with the security group for the EKS node group, update SSH access to only allow traffic
from your organization's CIDR block. Lock cluster endpoint access to a specific CIDR block as well, and 
change the cluster endpoint access from `Public` to `Public and private`.  Otherwise, the
cluster nodes wouldn't be able to access the cluster endpoint, as they're not part of the CIDR block.

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

## Update the Node Feature Rule

The AMD GPU Operator uses Node Feature Rules to determine which Kubernetes nodes have 
AMD GPUs, and then adds the appropriate labels to such nodes. 

The `g4ad.xlarge` instance type uses the AMD Radeon Pro V520, which isn't included 
by the default Node Feature Rule.  We can add it by editing the rule: 

``` bash
kubectl edit nodefeaturerule -n kube-amd-gpu
```

And adding a new match feature in the `amd-gpu` section:

``` yaml
      *** Other match features *** 
      - matchFeatures:
        - feature: pci.device
          matchExpressions:
            device:
              op: In
              value:
              - "7362" # <--- Add this device ID here
            vendor:
              op: In
              value:
              - "1002"
      name: amd-gpu
```

Once this change is applied, we should see the following label when describing 
our node: 

````
feature.node.kubernetes.io/amd-gpu=true
````

## Verify the Device Metrics Exporter

We can confirm whether the device metrics exporter is working by port forwarding the 
service with the following command: 

``` bash
kubectl port-forward svc/default-metrics-exporter -n kube-amd-gpu 5000:5000
```

And then using curl to access it: 

``` bash
curl http://localhost:5000/v1/metrics
```


## Install the OpenTelemetry Collector

Use the following command to install the Splunk distribution of the OpenTelemetry Collector,
using the [values.yaml](./values.yaml) file to configure the Prometheus receiver for scraping
AMD GPU metrics:

``` bash
helm install --upgrade splunk-otel-collector \
--set="splunkObservability.realm=us1" \
--set="splunkObservability.accessToken=your_splunk_access_token" \
--set="clusterName=eks-amd" \
--set="environment=eks-amd" \
-f ./values.yaml \
splunk-otel-collector-chart/splunk-otel-collector
```

## View Metrics in Splunk Observability Cloud

The resulting metrics provide a wealth of information about our GPU infrastructure and 
gives us insight into whether the hardware is being used efficiently, or if we’re at 
risk of running out of GPU capacity, so we can take action before end-users 
are negatively impacted. These metrics can be visualized on a dashboard in 
Splunk Observability Cloud: 

![AMD GPU Dashboard](./images/GPU%20Dashboard.png)

## Delete the EKS Cluster

If you need to delete the EKS cluster, you can do so with the following command:

``` bash
eksctl delete cluster --name eks-gpu-demo-cluster-g4ad --region us-west-2 --disable-nodegroup-eviction
```
