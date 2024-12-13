import azure.functions as func
import logging
from splunk_opentelemetry import init_opentelemetry
from opentelemetry import trace

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

init_opentelemetry()
tracer = trace.get_tracer("azure_function_python_opentelemetry_example")

app = func.FunctionApp(http_auth_level=func.AuthLevel.ANONYMOUS)

@app.route(route="azure_function_python_opentelemetry_example")
def azure_function_python_opentelemetry_example(req: func.HttpRequest) -> func.HttpResponse:

    with tracer.start_as_current_span("azure_function_python_opentelemetry_example") as span:
        
        logging.info('Python HTTP trigger function processed a request.')

        name = req.params.get('name')
        if not name:
            try:
                req_body = req.get_json()
            except ValueError:
                pass
            else:
                name = req_body.get('name')

        if name:
            span.set_attribute("app.name", name)
            return func.HttpResponse(f"Hello, {name}!")
        else:
            return func.HttpResponse(
                "Hello, World!",
                status_code=200
            )