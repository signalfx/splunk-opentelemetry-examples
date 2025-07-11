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

| Language / Environment | Java                                                  | .NET                                                                                                                                            | Node.js                                                                                                                 | Python                                                  | Go                                                  |
|------------------------|-------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------|
| Linux                  | [Link](./instrumentation/java/linux)                  | [Link](./instrumentation/dotnet/linux)                                                                                                          | [Link](./instrumentation/nodejs/linux)                                                                                  | [Link](./instrumentation/python/linux)                  | [Link](./instrumentation/go/linux)                  |
| Windows                |                                                       | [Link](./instrumentation/dotnet/windows)                                                                                                        |                                                                                                                         |                                                         |                                                     |
| Kubernetes             | [Link](./instrumentation/java/k8s)                    | [.NET Core/Linux](./instrumentation/dotnet/k8s)  <br>  [.NET Framework/Windows](./instrumentation/dotnet/k8s-windows)                           | [Link](./instrumentation/nodejs/k8s)                                                                                    | [Link](./instrumentation/python/k8s)                    | [Link](./instrumentation/go/k8s)                    |
| AWS ECS Fargate        | [Link](./instrumentation/java/aws-ecs)                | [Link](./instrumentation/dotnet/aws-ecs)                                                                                                        | [Link](./instrumentation/nodejs/aws-ecs)                                                                                | [Link](./instrumentation/python/aws-ecs)                | [Link](./instrumentation/go/aws-ecs)                |
| AWS EKS Fargate        | [Link](./instrumentation/java/aws-eks-fargate)        |                                                                                                                                                 |                                                                                                                         |                                                         |                                                     |
| AWS Lambda Function    | [Link](./instrumentation/java/aws-lambda)             | [Link](./instrumentation/dotnet/aws-lambda) <br> [Custom Layer](./instrumentation/dotnet/aws-lambda-with-custom-layer)                          | [Link](./instrumentation/nodejs/aws-lambda) <br> [Container Image](./instrumentation/nodejs/aws-lambda-container-image) | [Link](./instrumentation/python/aws-lambda)             | [Link](./instrumentation/go/aws-lambda)             |
| Azure Function         | [Link](./instrumentation/java/azure-functions)        | [Link](./instrumentation/dotnet/azure-functions)                                                                                                | [Link](./instrumentation/nodejs/azure-functions)                                                                        | [Link](./instrumentation/python/azure-functions)        | [Link](./instrumentation/go/azure-functions)        |
| Azure App Service      | [Link](./instrumentation/java/azure-app-service)      | [Link](./instrumentation/dotnet/azure-app-service)                                                                                              | [Link](./instrumentation/nodejs/azure-app-service)                                                                      |                                                         |                                                     |
| Azure Container App    |   | [Link](./instrumentation/dotnet/azure-container-apps) <br> [Without Collector](./instrumentation/dotnet/azure-container-apps-without-collector) |                                                                                                                         |                                                         |                                                     |
| Google Cloud Function  | [Link](./instrumentation/java/google-cloud-functions) | [Link](./instrumentation/dotnet/google-cloud-functions)                                                                                         | [Link](./instrumentation/nodejs/google-cloud-functions)                                                                 | [Link](./instrumentation/python/google-cloud-functions) | [Link](./instrumentation/go/google-cloud-functions) |

Examples for each combination will be added over time. 

## OpenTelemetry Collector Examples

This category will include examples that demonstrate how to deploy the collector 
in various environments, and how to utilize various features. 

# License

The examples in this repository are licensed under the terms of the Apache Software License version 2.0. For more details, see [the license file](./LICENSE).
