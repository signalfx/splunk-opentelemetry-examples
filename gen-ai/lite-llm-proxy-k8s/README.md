# Deploy Lite LLM Proxy in Kubernetes with Splunk OpenTelemetry 

## Deploy the OpenTelemetry Collector

Add the access token to the command below before running it: 

``` bash
helm repo add splunk-otel-collector-chart https://signalfx.github.io/splunk-otel-collector-chart 

helm repo update 
 
kubectl create ns splunk 

helm install splunk-otel-collector --set="splunkObservability.accessToken=***,clusterName=lite-llm-proxy,splunkObservability.realm=us1,gateway.enabled=false,environment=lite-llm-proxy,agent.discovery.enabled=true" -n splunk splunk-otel-collector-chart/splunk-otel-collector 
````

## Deploy LiteLLM 

``` bash
git clone https://github.com/BerriAI/litellm.git 
 
cd litellm 

git checkout v1.77.7-stable
 
cd deploy/charts/litellm-helm
```

Update the values.yaml file to include the content from [values.yaml](./values.yaml). 
Do the same with [Chart.yaml](./Chart.yaml). 

Change the postgresql database image by navigating to: 

``` bash
cd charts
tar -xvf postgresql-14.3.1.tgz
rm postgresql-14.3.1.tgz
```

Then updating the `postgresql/values.yaml` file as follows: 

``` yaml
image:
  registry: registry-1.docker.io
  repository: bitnami/postgresql
  tag: latest
```

Then run the following command to install LiteLLM: 

``` bash
cd ~/litellm/deploy/charts/litellm-helm

kubectl create ns litellm

helm upgrade --install litellm . -f values.yaml -n litellm
```

## Test LiteLLM 

Expose LiteLLM service outside the Kubernetes cluster: 

``` bash
sudo kubectl --namespace default port-forward svc/litellm -n litellm 81:4000 --address 0.0.0.0
```

Try accessing the UI: 

````
http://<IP address>:81/ui
````

Try sending a chat completion request: 

``` bash
curl --location 'http://<IP Address>:81/openai/deployments/gpt-3.5-turbo/chat/completions' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer sk-12345' \
--data '{
    "model": "gpt-3.5-turbo",
    "messages": [
        {"role": "user", "content": "Tell me a short story."}
    ],
    "max_tokens": 100
}'
```