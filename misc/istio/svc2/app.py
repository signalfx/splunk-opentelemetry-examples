from flask import Flask, request
import logging

app = Flask(__name__)
logging.getLogger().setLevel(logging.INFO)

@app.route("/hello")
def hello_world():

    print(request.headers)

    logging.getLogger().info("Handling the /hello request")
    return "Hello from Service 2!"