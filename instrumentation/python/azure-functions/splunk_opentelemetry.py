import os 
from splunk_otel.tracing import start_tracing
from splunk_otel.metrics import start_metrics

def init_opentelemetry(): 

    if 'OTEL_SERVICE_NAME' not in os.environ: 
        raise Exception('The OTEL_SERVICE_NAME environment variable must be set')

    if 'OTEL_EXPORTER_OTLP_ENDPOINT' not in os.environ: 
        raise Exception('The OTEL_EXPORTER_OTLP_ENDPOINT environment variable must be set')
    
    if 'OTEL_RESOURCE_ATTRIBUTES' not in os.environ: 
        raise Exception('The OTEL_RESOURCE_ATTRIBUTES environment variable must be set')

    start_tracing()
    start_metrics()
