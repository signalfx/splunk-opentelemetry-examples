# Splunk OpenTelemetry Examples

This repository provides examples that demonstrate how to use OpenTelemetry 
with Splunk Observability Cloud. The examples are divided into the following 
two categories: 

1. OpenTelemetry Instrumentation Examples
2. OpenTelemetry Collector Examples 

> :warning: These examples are not intended for production usage. While no support is officially provided for them, you are welcome to submit an issue or a pull request. 

## OpenTelemetry Instrumentation Examples

This category includes examples that demonstrate how to instrument applications 
with OpenTelemetry that use combinations of the following languages and target
deployment environments: 

| Language / Environment | Java                                           | .NET                                             | Node.js                                     | Python                                      | Go                                      |
|------------------------|------------------------------------------------|--------------------------------------------------|---------------------------------------------|---------------------------------------------|-----------------------------------------|
| Linux                  | [Link](./instrumentation/java/linux)           | [Link](./instrumentation/dotnet/linux)           | [Link](./instrumentation/nodejs/linux)      | [Link](./instrumentation/python/linux)      | [Link](./instrumentation/go/linux)      |
| Windows                |                                                |                                                  |                                             |                                             |                                         |
| Kubernetes             | [Link](./instrumentation/java/k8s)             | [Link](./instrumentation/dotnet/k8s)             | [Link](./instrumentation/nodejs/k8s)        | [Link](./instrumentation/python/k8s)        | [Link](./instrumentation/go/k8s)        |
| AWS ECS Fargate        | [Link](./instrumentation/java/aws-ecs)         | [Link](./instrumentation/dotnet/aws-ecs)         | [Link](./instrumentation/nodejs/aws-ecs)    | [Link](./instrumentation/python/aws-ecs)    | [Link](./instrumentation/go/aws-ecs)    |
| AWS EKS Fargate        | [Link](./instrumentation/java/aws-eks-fargate) |                                                  |                                             |                                             |                                         |
| AWS Lambda Function    | [Link](./instrumentation/java/aws-lambda)      | [Link](./instrumentation/dotnet/aws-lambda)      | [Link](./instrumentation/nodejs/aws-lambda) | [Link](./instrumentation/python/aws-lambda) | [Link](./instrumentation/go/aws-lambda) |
| Azure Function         |                                                | [Link](./instrumentation/dotnet/azure-functions) |                                             |                                             |                                         |
| Google Cloud Function  |                                                |                                                  |                                             |                                             |                                         |
| Azure Web Apps         |                                                |                                                  |                                             |                                             |                                         |

Examples for each combination will be added over time. 

## OpenTelemetry Collector Examples

This category will include examples that demonstrate how to deploy the collector 
in various environments, and how to utilize various features. 

# License

The examples in this repository are licensed under the terms of the Apache Software License version 2.0. For more details, see [the license file](./LICENSE).
