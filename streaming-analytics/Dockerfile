FROM openjdk:21-jre-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy the JAR file
COPY target/streaming-analytics-1.0.0-shaded.jar app.jar

# Create directories for Spark and Flink
RUN mkdir -p /tmp/spark-checkpoint /tmp/spark-warehouse \
    /tmp/flink-checkpoints /tmp/flink-savepoints

# Expose ports
EXPOSE 8082 8081 4040

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/api/health || exit 1

# Environment variables
ENV JAVA_OPTS="-Xmx2g -Xms1g"
ENV KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]