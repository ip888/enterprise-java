# 🚀 Project 2: Streaming Data Pipeline with ML Insights

[![Apache Spark](https://img.shields.io/badge/Apache%20Spark-3.5-red.svg)](https://spark.apache.org/)
[![Apache Flink](https://img.shields.io/badge/Apache%20Flink-1.18-purple.svg)](https://flink.apache.org/)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-blue.svg)](https://kafka.apache.org/)

> **High-performance streaming analytics platform demonstrating real-time data processing, machine learning insights, and complex event processing using Apache Spark and Flink.**

## 🎯 Project Overview

This project showcases enterprise-level streaming data processing capabilities with:

- ✅ **Apache Spark Streaming** for ML model training and batch analytics
- ✅ **Apache Flink** for real-time complex event processing and alerting
- ✅ **Machine Learning Pipeline** with user behavior analysis and anomaly detection
- ✅ **Real-time Analytics Dashboard** with live metrics and monitoring
- ✅ **Security Monitoring** with intelligent threat detection
- ✅ **Seamless Integration** with Project 1 (User Service) event streams

## 🏗️ Architecture

```text
┌─────────────────────────────────────────────────────────────────────┐
│                    PROJECT 1: USER SERVICE                         │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────────────┐    │
│  │ User Service│───▶│ Kafka Events │───▶│  Real-time Monitor  │    │
│  │ (Spring Boot)│    │ (3 Topics)   │    │     Dashboard       │    │
│  └─────────────┘    └──────────────┘    └─────────────────────┘    │
└─────────────────────────┬───────────────────────────────────────────┘
                          │ Event Streams
┌─────────────────────────▼───────────────────────────────────────────┐
│                    PROJECT 2: STREAMING ANALYTICS                  │
│                                                                     │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐            │
│  │ Apache Flink │   │ Apache Spark │   │   Analytics  │            │
│  │ Stream       │   │ ML Pipeline  │   │   Dashboard  │            │
│  │ Processing   │   │ & Batch Jobs │   │  (Live UI)   │            │
│  └──────────────┘   └──────────────┘   └──────────────┘            │
│         │                   │                   │                  │
│         ▼                   ▼                   ▼                  │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │           PostgreSQL Analytics Database                     │    │
│  │  • Security Alerts    • User Behavior Patterns            │    │
│  │  • ML Model Metrics   • Streaming Performance             │    │
│  └────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

## 📊 Key Features

### Real-time Stream Processing
- **Complex Event Processing (CEP)** with Apache Flink for pattern detection
- **Stateful stream processing** with checkpointing and fault tolerance
- **Windowed analytics** for time-based aggregations
- **Real-time alerting** for security threats and anomalies

### Machine Learning Pipeline
- **User behavior clustering** using K-Means algorithm
- **Anomaly detection** with isolation forest techniques
- **Real-time feature engineering** from streaming data
- **Model training and serving** with Apache Spark MLlib

### Advanced Analytics
- **User segmentation** based on behavior patterns
- **Security threat detection** with configurable thresholds
- **Performance monitoring** with real-time metrics
- **Interactive dashboards** with live data visualization

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.9+
- **Project 1 (User Service) running** on port 8080 (for full integration)

### 1. Clone and Navigate
```bash
cd /workspaces/enterprise-java/streaming-analytics
```

### 2. Start the Complete Pipeline
```bash
# Start all services with one command
./scripts/start-pipeline.sh
```

This script will:
- ✅ Build the streaming analytics application
- ✅ Start Kafka, PostgreSQL, Redis infrastructure
- ✅ Create required Kafka topics
- ✅ Initialize the analytics database
- ✅ Launch Spark and Flink streaming applications
- ✅ Start the real-time analytics dashboard

### 3. Verify Deployment
After startup (60-90 seconds), verify all services:

```bash
# Check service health
curl http://localhost:8082/api/health

# View analytics dashboard
open http://localhost:8082
```

## 📈 Available Services

| Service | URL | Description |
|---------|-----|-------------|
| **Analytics Dashboard** | http://localhost:8082 | Real-time streaming metrics and alerts |
| **Spark Web UI** | http://localhost:4040 | Spark jobs and streaming monitoring |
| **Flink Web UI** | http://localhost:8081 | Flink stream processing dashboard |
| **Kafka UI** | http://localhost:8083 | Kafka topics and message monitoring |
| **PostgreSQL** | localhost:5432 | Analytics database (user: analytics_user) |

## 🔧 Configuration

### Application Properties
```properties
# Kafka Topics Integration
kafka.topics.user-events=user-events          # From Project 1
kafka.topics.notifications=notifications       # From Project 1  
kafka.topics.security-alerts=security-alerts  # Generated alerts
kafka.topics.analytics-insights=analytics-insights

# Spark Streaming
spark.streaming.batch.interval=30s
spark.sql.adaptive.enabled=true

# Flink Processing
flink.parallelism=2
flink.checkpoint.interval=30000

# ML Configuration  
ml.clustering.k=5
ml.anomaly.threshold=0.8
```

### Environment Variables
```bash
# Kafka connection
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Database connection  
POSTGRES_URL=jdbc:postgresql://localhost:5432/analytics_db

# JVM tuning
JAVA_OPTS="-Xmx2g -Xms1g"
```

## 🧠 Machine Learning Components

### 1. User Behavior Clustering
```java
// K-Means clustering for user segmentation
KMeans kmeans = new KMeans()
    .setK(5)
    .setFeaturesCol("features")
    .setPredictionCol("userSegment");
```

### 2. Anomaly Detection  
```java  
// Isolation Forest for anomaly detection
// Detects unusual user activity patterns
// Configurable threshold and sensitivity
```

### 3. Real-time Feature Engineering
```java
// Extract features from streaming events
Dataset<Row> features = userEvents
    .groupBy("userId")  
    .agg(
        functions.count("*").alias("totalEvents"),
        functions.countDistinct("sessionId").alias("sessions"),
        functions.avg("hour").alias("avgActiveHour")
    );
```

## 🔒 Security Monitoring

### Threat Detection Patterns
- **Brute Force Detection**: Multiple failed login attempts
- **Rapid Activity**: Unusually high event rates  
- **Behavioral Anomalies**: ML-based pattern detection
- **Geographic Anomalies**: IP-based location analysis

### Alert Severity Levels
- 🔵 **LOW**: Minor behavioral changes
- 🟡 **MEDIUM**: Moderate risk patterns  
- 🔴 **HIGH**: Significant security concerns
- 🚨 **CRITICAL**: Immediate threat response required

## 📊 Integration with Project 1

### Event Stream Consumption
```java
// Consuming events from User Service (Project 1)
KafkaSource<String> source = KafkaSource.<String>builder()
    .setBootstrapServers("localhost:9092")
    .setTopics("user-events")
    .setGroupId("streaming-analytics")
    .build();
```

### Real-time Processing Pipeline
1. **User Service** generates events (registrations, logins, searches)
2. **Kafka** streams events to analytics pipeline  
3. **Flink** processes events for real-time alerts
4. **Spark** performs ML analysis and batch processing
5. **Dashboard** displays live insights and metrics

## 🧪 Testing & Development

### Run Tests
```bash
mvn test
```

### Development Mode
```bash  
# Start only infrastructure
docker-compose up -d kafka postgres redis

# Run application locally
mvn exec:java -Dexec.mainClass="com.enterprise.java.streaming.StreamingAnalyticsApplication"
```

### Generate Test Data
```bash
# If Project 1 is not running, use data generator
curl -X POST http://localhost:8082/api/generate-test-data
```

## 📈 Performance Monitoring

### Spark Metrics
- **Batch Processing Time**: Average time per micro-batch
- **Input/Output Rates**: Events processed per second
- **Memory Usage**: JVM heap and off-heap memory  
- **ML Model Performance**: Training time and accuracy

### Flink Metrics  
- **Throughput**: Events processed per second
- **Latency**: End-to-end processing time
- **Checkpoint Duration**: State persistence time
- **Backpressure**: Stream processing bottlenecks

## 🐳 Docker Deployment

### Production Deployment
```bash
# Build production image
docker build -t streaming-analytics:latest .

# Deploy with environment-specific config
docker-compose -f docker-compose.prod.yml up -d
```

### Scaling
```bash  
# Scale Flink task managers
docker-compose up -d --scale flink-taskmanager=3

# Increase Spark executors
export SPARK_EXECUTOR_INSTANCES=4
```

## 🛡️ Production Considerations

### High Availability
- **Kafka**: Multi-broker cluster with replication
- **Flink**: JobManager HA (KRaft mode, no ZooKeeper)
- **Spark**: Cluster mode with dynamic allocation
- **Database**: PostgreSQL with streaming replication

### Security  
- **TLS/SSL**: Encrypt all communication channels
- **Authentication**: SASL/SCRAM for Kafka access
- **Authorization**: Role-based access control
- **Audit Logging**: Comprehensive security event logging

### Monitoring
- **Metrics**: Prometheus + Grafana dashboards
- **Logging**: ELK stack for centralized logging  
- **Alerting**: PagerDuty integration for critical alerts
- **Health Checks**: Kubernetes readiness/liveness probes

## 📚 API Documentation

### Analytics Dashboard API
```bash
# Get real-time metrics
curl http://localhost:8082/api/metrics

# Get security alerts  
curl http://localhost:8082/api/alerts

# Health check
curl http://localhost:8082/api/health

# Spark streaming metrics
curl http://localhost:8082/api/spark-metrics
```

## 🤝 Integration Examples

### Consuming User Events
```java
// Process events from Project 1
userEventStream
    .keyBy(UserEvent::getUserId)
    .process(new SecurityAnalysisFunction())
    .addSink(new AlertSink());
```

### Publishing Analytics Results  
```java
// Send insights back to Project 1
analyticsResults
    .map(new InsightMapper())
    .addSink(kafkaProducer("analytics-insights"));
```

## 🛑 Shutdown

```bash
# Graceful shutdown
docker-compose down

# With data cleanup  
docker-compose down -v
```

## 📝 Project Logs

```bash
# View streaming analytics logs
docker-compose logs -f streaming-analytics

# View specific component logs
docker-compose logs -f kafka
docker-compose logs -f postgres  
```

## 🎯 Learning Outcomes

This project demonstrates:
- ✅ **Stream Processing Mastery**: Advanced Spark and Flink usage
- ✅ **ML in Production**: Real-time model training and serving  
- ✅ **Event-Driven Architecture**: Complex event processing patterns
- ✅ **Microservices Integration**: Seamless cross-project communication
- ✅ **Operational Excellence**: Monitoring, alerting, and observability

---

## 🔗 Related Projects

- **[Project 1: Real-Time E-Commerce Analytics](../microservices/user-service/)** - Event-driven microservices with Spring Boot
- **[Infrastructure](../infrastructure/)** - Kubernetes deployment and monitoring  
- **[Documentation](../docs/)** - Architecture guides and API documentation

---

**Built with ❤️ using Java 21, Apache Spark 3.5, Apache Flink 1.18, and modern streaming technologies.**