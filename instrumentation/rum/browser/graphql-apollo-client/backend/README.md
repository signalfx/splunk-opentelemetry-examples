# GraphQL Backend

This backend was created using the instructions found in 
[Get Started with Apollo Server](https://www.apollographql.com/docs/apollo-server/getting-started). 

We added OpenTelemetry by running the following commands: 

``` bash
npm install @splunk/otel
npm install @opentelemetry/instrumentation-http
npm install @opentelemetry/instrumentation-express
npm install @opentelemetry/instrumentation-graphql
export OTEL_SERVICE_NAME=apollo-server
export OTEL_RESOURCE_ATTRIBUTES='deployment.environment=apollo-test'
export SPLUNK_TRACE_RESPONSE_HEADER_ENABLED=true
```

Use the following command to start the server: 

``` bash
npm start
```

Access the sandbox application by navigating to [http://localhost:4000](http://localhost:4000). 

Example queries to use for testing: 

````
query GetBooks {
  books {
    title
    author
  }
}
````