import os 
import logging
from opentelemetry import trace, _logs, metrics
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.sdk._logs import LoggerProvider, LoggingHandler
from opentelemetry.sdk._logs.export import BatchLogRecordProcessor
from opentelemetry.exporter.otlp.proto.http._log_exporter import OTLPLogExporter
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.exporter.otlp.proto.http.metric_exporter import OTLPMetricExporter
from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader

def init_opentelemetry(): 

    if 'OTEL_SERVICE_NAME' not in os.environ: 
        raise Exception('The OTEL_SERVICE_NAME environment variable must be set')

    if 'OTEL_EXPORTER_OTLP_ENDPOINT' not in os.environ: 
        raise Exception('The OTEL_EXPORTER_OTLP_ENDPOINT environment variable must be set')
    
    if 'OTEL_RESOURCE_ATTRIBUTES' not in os.environ: 
        raise Exception('The OTEL_RESOURCE_ATTRIBUTES environment variable must be set')

    traceProvider = TracerProvider()
    processor = BatchSpanProcessor(OTLPSpanExporter())
    traceProvider.add_span_processor(processor)
    trace.set_tracer_provider(traceProvider)

    reader = PeriodicExportingMetricReader(OTLPMetricExporter())
    meterProvider = MeterProvider(metric_readers=[reader])
    metrics.set_meter_provider(meterProvider)

    _logs.set_logger_provider(
        LoggerProvider()
    )
    logging.getLogger().addHandler(
        LoggingHandler(
            logger_provider=_logs.get_logger_provider().add_log_record_processor(
                BatchLogRecordProcessor(OTLPLogExporter())
            )
        )
    )