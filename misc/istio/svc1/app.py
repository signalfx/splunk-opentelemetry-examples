from flask import Flask, request
import logging
import requests
import os

app = Flask(__name__)
logging.getLogger().setLevel(logging.INFO)
svc2endpoint = os.getenv("SVC2_ENDPOINT")

@app.route("/hello")
def hello_world():

    logging.getLogger().info("Handling the /hello request")

    response = requests.get(svc2endpoint)
    if response.status_code == 200:
        # combine the response from service 1 with service 2
        return "Hello from Service 1 and...{}".format(response.text)
    else:
        # return just the service 1 response
        logging.getLogger().error(f"Error: {response.status_code}")
        return "Hello from Service 1! (error encountered calling Service 2)"