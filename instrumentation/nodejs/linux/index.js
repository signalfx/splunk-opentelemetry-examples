const express = require("express");
const app = express();
const logger = require('pino')()
const opentelemetry = require('@opentelemetry/api');

const PORT = process.env.PORT || "8080";
const tracer = opentelemetry.trace.getTracer('example-app', '0.1.0');

app.get("/hello", (_, res) => {
  hello(res);
});

app.listen(parseInt(PORT, 10), () => {
  logger.info(`Listening for requests on http://localhost:${PORT}`);
});

function hello(res) {
  logger.info('/hello endpoint invoked, sending response');
  slow_function()
  res.status(200).send("Hello, World!");
}

// include a slow function to demonstrate CPU call stacks
function slow_function() {

  let result = 0;
  for (let i = 0; i < 1000000000; i++) {
    result += i;
  }

  return result;
}
