# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Tools needed for agent download
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl unzip jq && \
    rm -rf /var/lib/apt/lists/*

# Cache Maven dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy source and build jar
COPY src ./src
RUN mvn -q -DskipTests clean package

# Download AppDynamics Java agent
RUN set -eux; \
    INSTALL_DIR="/build/agent"; \
    mkdir -p "${INSTALL_DIR}"; \
    FILE_PATH="$(curl -s https://download.appdynamics.com/download/downloadfilelatest/ | jq -r '.[].s3_path' | grep java-jdk8 | head -n 1)"; \
    DOWNLOAD_URL="https://download-files.appdynamics.com/${FILE_PATH}"; \
    echo "Downloading agent from: ${DOWNLOAD_URL}"; \
    curl -fL "${DOWNLOAD_URL}" -o /build/JavaAgent.zip; \
    unzip /build/JavaAgent.zip -d "${INSTALL_DIR}"; \
    rm -f /build/JavaAgent.zip

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy app jar and agent from builder
COPY --from=builder /build/target/*.jar /app/app.jar
COPY --from=builder /build/agent /app/agent

# Fix permissions for AppD dynamic service/log folders
RUN chown -R spring:spring /app && \
    chmod -R u+rwX,g+rwX /app/agent

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java \
  -javaagent:/app/agent/javaagent.jar \
  -Dappdynamics.controller.hostName=${APPD_CONTROLLER_HOST} \
  -Dappdynamics.controller.port=443 \
  -Dappdynamics.controller.ssl.enabled=true \
  -Dappdynamics.agent.applicationName=${APPD_APP_NAME} \
  -Dappdynamics.agent.tierName=${APPD_TIER_NAME} \
  -Dappdynamics.agent.nodeName=${APPD_NODE_NAME} \
  -Dappdynamics.agent.accountName=${APPD_ACCOUNT_NAME} \
  -Dappdynamics.agent.accountAccessKey=${APPD_ACCESS_KEY} \
  -Dagent.deployment.mode=dual \
  -Dotel.traces.exporter=otlp \
  -Dotel.exporter.otlp.endpoint=http://localhost:4318 \
  -Dotel.service.name=${OTEL_SERVICE_NAME} \
  -Dotel.resource.attributes=${OTEL_RESOURCE_ATTRIBUTES} \
  -jar /app/app.jar"]