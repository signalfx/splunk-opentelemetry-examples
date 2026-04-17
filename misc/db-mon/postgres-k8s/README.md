# Database Monitoring: PostgreSQL in Kubernetes 

This example shows how to configure the OpenTelemetry collector to monitor 
a PostgreSQL database running in Kubernetes, and report data to 
Splunk Observability Cloud. 

## Prerequisites

* A Kubernetes cluster 
* Helm
* Splunk Observability Cloud with Database Monitoring enabled 

## Deploy the PostgreSQL Database 

If you don't already have a database running in Kubernetes, follow 
these steps to deploy one. 

Step 1: Install Helm if you don’t have it. You can confirm with 
the following command: 

```bash
helm version
```

Step 2: Add a PostgreSQL chart repository

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

Step 3: Install PostgreSQL in your cluster

```bash
helm install my-postgres bitnami/postgresql
```

Step 4: Verify the deployment

```bash
kubectl get pods
kubectl get svc
```

Step 5: Get the generated password

```bash
kubectl get secret my-postgres-postgresql -o jsonpath="{.data.postgres-password}" | base64 --decode
```

Make a note of the password for future reference. 

## Prepare the PostgreSQL Database for Database Monitoring

A few steps are required to prepare the database for database monitoring. 
Specifically, we new to load `pg_stat_statements` 
at PostgreSQL startup using the `shared_preload_libraries` setting. 

We'll do this by updating the PostgreSQL Helm release using the configuration 
in [postgres-values.yaml](./postgres-values.yaml): 

```bash
helm upgrade my-postgres bitnami/postgresql -f postgres-values.yaml 
```

When the database pod has restarted, run the following command to create the 
`pg_stat_statements` extension: 

```bash
psql -U postgres -d postgres -c "CREATE EXTENSION IF NOT EXISTS pg_stat_statements; GRANT SELECT ON pg_stat_statements TO postgres;" 
```

Verify this with the following command (substitute your pod name before running it): 

```bash
kubectl exec -it <postgres-pod> -- psql -U postgres -c "SHOW shared_preload_libraries;" 
```

## Deploy the OpenTelemetry Collector 

Next, we'll deploy an OpenTelemetry collector in the environment, and configure it 
to monitor this database. 

First, update the [collector-values.yaml](./collector-values.yaml) and set the 
appropriate database password: 

```yaml
              username: "postgres"
              password: "<database password>"
```

> Note: for a real database, a Kubernetes secret or other secure mechanism should be used instead

Then, install the collector Helm chart: 

```bash
helm repo add splunk-otel-collector-chart https://signalfx.github.io/splunk-otel-collector-chart
helm repo update
```

Define environment variables: 

> Note: set your realm and access token before running the following commands

```bash
export REALM=your_realm 
export ACCESS_TOKEN=your_observability_cloud_access_token
````

Now we can install the collector: 

```bash
helm upgrade --install splunk-otel-collector --set="splunkObservability.realm=$REALM,splunkObservability.accessToken=$ACCESS_TOKEN,clusterName=postgres-test,environment=postgres-test" -f ./collector-values.yaml splunk-otel-collector-chart/splunk-otel-collector 
```

> Note: we've used a `receiver_creator` to add the `postgresql`, which prevents 
> the collector from duplicating data pulls. 

Refer to [Collect data from PostgreSQL](https://help.splunk.com/en/splunk-observability-cloud/monitor-databases/collect-data-from-your-database-platforms/collect-data-from-postgresql) 
for further information. 