# Building the upstream service (service 1)

This simple python service is used as part of an example that demonstrates
how to capture traces from an Istio environment and send them to an OpenTelemetry
collector.

## Prerequisites

The following tools are required to build and execute the Python service:

* Python 3.12+
* A Linux-compatible host (such as Ubuntu 24.04, or Mac OS)

## Deploy the Splunk OpenTelemetry Collector

This example requires the Splunk Distribution of the OpenTelemetry collector to
be running on the host and available via http://localhost:4317.  Follow the
instructions in [Install the Collector for Linux with the installer script](https://docs.splunk.com/observability/en/gdi/opentelemetry/collector-linux/install-linux.html#install-the-collector-using-the-installer-script)
to install the collector on your host.

## Instrument and Execute the Application

Open a command line terminal and navigate to the root of the directory,
then create a virtual environment and activate it:

````
cd ~/splunk-opentelemetry-examples/misc/istio/svc1
python3 -m venv venv
source ./venv/bin/activate
````

### Prep Package Installation and Instrumentation (Optional)

We installed the following packages:

````
pip3 install flask
pip3 install "splunk-opentelemetry" 
````

We then ran the following command to install instrumentation for packages
used by our application:

````
opentelemetry-bootstrap -a install
````

We then generated a requirements.txt file by executing the following command:

````
pip3 freeze > requirements.txt
````

There's no need to run these commands again as you can use the `requirements.txt` file that
was already created.

### Install Packages

Use the following command to install the required packages, which includes those
used for OpenTelemetry instrumentation:

````
pip3 install -r requirements.txt
````

### Set Environment Variables

To configure the instrumentation, we've set the following environment variables:

```` 
export OTEL_SERVICE_NAME=python-istio-svc2
export OTEL_RESOURCE_ATTRIBUTES='deployment.environment=test'
export OTEL_PYTHON_DISABLED_INSTRUMENTATIONS=click
export SVC2_ENDPOINT=http://localhost:8090/hello
````

### Test the application

Next, we'll execute the application with the `opentelemetry-instrument` binary as follows:

````
opentelemetry-instrument flask run -p 8080
````

Access the application by navigating your web browser to the following URL:

````
http://localhost:8080/hello
````

You should receive the following response:

````
Hello from Service 2!
````

### Build the Docker image

To run the application in K8s, we'll need a Docker image for the application.
We've already built one, so feel free to skip this section unless you want to use
your own image.

You can use the following command to build the Docker image:

````
docker build --platform="linux/amd64" -t python-istio-svc1:1.0 .
````

Ensure that you use a machine with a linux/amd64 architecture to build the image, as there are issues
with AlwaysOn profiling when the image is built with arm64 architecture.

Note that the Dockerfile references `opentelemetry-instrument` when launching the application:

````
CMD ["opentelemetry-instrument", "flask", "run", "--host", "0.0.0.0", "-p", "8080"]
````

It also includes the `--host` argument to ensure the flask application is visible
within the Kubernetes network (and not from just the container itself).

If you'd like to test the Docker image locally you can use:

````
docker run -p 8080:8080 \
-e OTEL_SERVICE_NAME=python-istio-svc1 \
-e OTEL_RESOURCE_ATTRIBUTES='deployment.environment=test' \
-e OTEL_PYTHON_DISABLED_INSTRUMENTATIONS=click \
-e SVC2_ENDPOINT=http://host.docker.internal:8090/hello \
python-istio-svc1:1.0
````

Then access the application by pointing your browser to `http://localhost:8080/hello`.

### Push the Docker image

We'll then need to push the Docker image to a repository that you have
access to, such as your Docker Hub account.  We've already done this for you,
so feel free to skip this step unless you'd like to use your own image.

Specifically, we've pushed the
image to GitHub's container repository using the following commands:

````
docker tag python-istio-svc1:1.0 ghcr.io/splunk/python-istio-example-svc1:1.0
docker push ghcr.io/splunk/python-istio-example-svc1:1.0
````