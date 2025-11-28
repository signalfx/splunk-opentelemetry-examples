# Options for Installing OpenTelemetry in a Kubernetes Cluster

## Decision Flowchart

### Option 1: Customer doesn't care, just wants to get OTel Signals

**Recommendation:** Use the Splunk version of the OpenTelemetry Helm chart

This is the simplest and most straightforward approach. The Splunk distribution comes pre-configured with optimal settings for sending telemetry data to Splunk Observability Cloud.

**Installation:**
```bash
helm repo add splunk-otel-collector-chart https://signalfx.github.io/splunk-otel-collector-chart
helm repo update

helm install splunk-otel-collector \
  --set="splunkObservability.realm=<REALM>" \
  --set="splunkObservability.accessToken=<ACCESS_TOKEN>" \
  --set="clusterName=<CLUSTER_NAME>" \
  splunk-otel-collector-chart/splunk-otel-collector
```

---

### Option 2: Customer insists on using Contrib instead of the Splunk Distribution

**Recommendation:** Use the Splunk Helm chart with a custom values.yaml to override the collector image

This approach gives you the upstream OpenTelemetry Collector Contrib distribution while still leveraging the Splunk Helm chart's configuration structure.

**Step 1:** Create a `values.yaml` file:
```yaml
# Minimal values.yaml for OpenTelemetry Collector Contrib 0.140.0
# This file contains only structural configuration
# Pass environment-specific values via command line (see examples below)

# Override image to use upstream contrib collector
image:
  otelcol:
    repository: otel/opentelemetry-collector-contrib
    tag: "0.140.0"

agent:
  service:
    enabled: true

featureGates:
  useMemoryLimitPercentage: true
```

**Step 2:** Install using the Splunk Helm chart with custom values:
```bash
helm repo add splunk-otel-collector-chart https://signalfx.github.io/splunk-otel-collector-chart
helm repo update

helm install splunk-otel-collector \
  --set="splunkObservability.realm=<REALM>" \
  --set="splunkObservability.accessToken=<ACCESS_TOKEN>" \
  --set="clusterName=<CLUSTER_NAME>" \
  -f values.yaml \
  splunk-otel-collector-chart/splunk-otel-collector
```

---

### Option 3: Customer has an existing OpenTelemetry Collector installation and wants to use Splunk as the endpoint

**Recommendation:** Modify existing configuration to add Splunk exporters

If the customer already has an OpenTelemetry Collector deployed and configured, you'll need to update their existing configuration to add Splunk as a destination.

**Step 1:** Use the reference templates

Use the files in `observability-workshop/workshop/otel-contrib-splunk-demo/k8s_manifests` as a template to identify the required Splunk-specific configurations.

**Step 2:** Identify the differences

Compare your existing collector configuration with the Splunk reference configuration. Key differences typically include:

- **Exporters:** Addition of Splunk-specific exporters (OTLP, SignalFx, etc.)
- **Processors:** Splunk-recommended processors (resource detection, batch processing, etc.)
- **Service pipelines:** Configuration of traces, metrics, and logs pipelines with Splunk exporters
- **Environment variables:** Splunk realm, access tokens, cluster name
- **Resource attributes:** Cluster and environment identification

**Step 3:** Apply the differences

Merge the Splunk-specific settings into your existing DaemonSet/Deployment configuration. Focus on:

1. **Environment Variables:**
   - `SPLUNK_OBSERVABILITY_ACCESS_TOKEN`
   - `SPLUNK_REALM`
   - `CLUSTER_NAME`

2. **ConfigMap Updates:**
   - Add Splunk exporters to the collector configuration
   - Update service pipelines to include Splunk exporters
   - Add necessary processors

3. **RBAC (if needed):**
   - Ensure service accounts have proper permissions for resource detection

**Step 4:** Apply the updated configuration
```bash
# Update ConfigMap with new collector configuration
kubectl apply -f collector-configmap.yaml

# Apply updated DaemonSet/Deployment
kubectl apply -f collector-daemonset.yaml

# Restart pods to pick up new configuration
kubectl rollout restart daemonset/otel-collector -n <namespace>
```

---

## Key Differences Between Installation Methods

| Aspect | Splunk Helm Chart | Contrib + Splunk Helm | Existing Collector |
|--------|-------------------|----------------------|-------------------|
| **Ease of Setup** | Easiest | Medium | Most Complex |
| **Collector Distribution** | Splunk-optimized | Upstream Contrib | Customer's choice |
| **Configuration Control** | Helm values | Helm + values.yaml | Full manual control |
| **Maintenance** | Helm managed | Helm managed | Manual |
| **Customization** | Limited to Helm values | Moderate | Complete |
| **Best For** | Quick deployment | Standards compliance | Existing infrastructure |

---

## Recommendations

- **For new deployments:** Use Option 1 (Splunk Helm chart)
- **For compliance requirements:** Use Option 2 (Contrib with Helm)
- **For existing deployments:** Use Option 3 (Modify existing config)

## Additional Resources

- [Splunk OpenTelemetry Collector Documentation](https://docs.splunk.com/Observability/gdi/opentelemetry/opentelemetry.html)
- [OpenTelemetry Collector Contrib Repository](https://github.com/open-telemetry/opentelemetry-collector-contrib)
- [Splunk OTel Collector Helm Chart](https://github.com/signalfx/splunk-otel-collector-chart)