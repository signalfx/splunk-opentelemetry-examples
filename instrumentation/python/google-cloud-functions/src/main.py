from splunk_otel import init_splunk_otel
import functions_framework
import logging
import flask
from opentelemetry.instrumentation.wsgi import OpenTelemetryMiddleware

init_splunk_otel()
logging.getLogger().setLevel(logging.INFO)

flask.current_app.wsgi_app = OpenTelemetryMiddleware(flask.current_app.wsgi_app)

@functions_framework.http
def hello_http(request):
    logging.getLogger().info("Handling the hello_http request")

    request_json = request.get_json(silent=True)
    request_args = request.args

    if request_json and 'name' in request_json:
        name = request_json['name']
    elif request_args and 'name' in request_args:
        name = request_args['name']
    else:
        name = 'World'
    return 'Hello {}!'.format(name)
