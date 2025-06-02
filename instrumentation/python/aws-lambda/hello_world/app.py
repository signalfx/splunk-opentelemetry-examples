import json
import requests
import logging
import sys
from opentelemetry.instrumentation.logging import LoggingInstrumentor

LoggingInstrumentor().instrument(set_logging_format=True)
FORMAT = '%(asctime)s %(levelname)s [%(name)s] [%(filename)s:%(lineno)d] [trace_id=%(otelTraceID)s span_id=%(otelSpanID)s resource.service.name=%(otelServiceName)s d trace_sampled=%(otelTraceSampled)s] - %(message)s'

logger = logging.getLogger(__name__)
logger.setLevel("INFO")
h = logging.StreamHandler(sys.stdout)
h.setFormatter(logging.Formatter(FORMAT))
logger.addHandler(h)
logging.getLogger().setLevel(logging.INFO)

def lambda_handler(event, context):

    logger.info('In lambda_handler, about to get the IP address...')

    try:
        ip = requests.get("http://checkip.amazonaws.com/")
    except requests.RequestException as e:
        logger.error(e)
        raise e

    logger.info('Successfully got the IP address, returning a response.')

    return {
        "statusCode": 200,
        "body": json.dumps({
            "message": "hello world",
             "location": ip.text.replace("\n", "")
        }),
    }
