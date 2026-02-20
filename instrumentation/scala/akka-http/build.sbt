ThisBuild / scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  "com.typesafe.akka" %% "akka-http"        % "10.5.3",
  "com.typesafe.akka" %% "akka-stream"      % "2.8.5"
)

enablePlugins(JavaAgent)

// Add the Splunk OTel Java Agent
javaAgents += "com.splunk" % "splunk-otel-javaagent" % "2.24.0" % "runtime"

// Set Splunk-specific default configurations
javaOptions ++= Seq(
  "-Dotel.service.name=akka-test-routes",
  "-Dsplunk.profiler.enabled=true",     // Enables AlwaysOn Profiling
  "-Dsplunk.metrics.enabled=true",       // Enables Splunk-specific metrics
  "-Dotel.traces.exporter=logging,otlp"  // Write traces to the console
)