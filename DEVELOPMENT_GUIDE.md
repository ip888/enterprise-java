# Development Guide: Enterprise Java Portfolio

## 🚀 Quick Start with GitHub Codespaces

### Option 1: GitHub Codespaces (Recommended)
```bash
# 1. Fork this repository to your GitHub account
# 2. Click "Code" → "Codespaces" → "Create codespace on main"
# 3. Wait for environment setup (5-10 minutes)
# 4. Start development immediately!
```

**Benefits:**
- ✅ Zero local setup required
- ✅ 4-core, 8GB RAM, 32GB storage
- ✅ Pre-configured Java 21, Kafka, Spark, Flink
- ✅ 60 free hours/month
- ✅ Professional development workflow

### Option 2: Local Development (MacBook M1 Alternative)
```bash
# Install prerequisites
brew install docker
brew install java-21-openjdk
brew install maven

# Clone and setup
git clone https://github.com/ip888/enterprise-java.git
cd enterprise-java
docker-compose -f .devcontainer/docker-compose.yml up -d
```

**Note:** Local development on MacBook M1/8GB/256GB will be resource-constrained for full stack.

---

## 🛠️ Development Environment

### Codespaces Environment Includes:
- **Java 21** (GraalVM with native compilation)
- **Apache Kafka** (Event streaming)
- **Apache Spark 3.5** (Batch & ML processing)
- **Apache Flink 1.18** (Stream processing)
- **PostgreSQL 15** (Primary database)
- **Redis 7** (Caching layer)
- **Docker & Kubernetes** (Containerization)
- **Prometheus & Grafana** (Monitoring)

### Port Mapping:
```
8080 → API Gateway
8081 → User Service
8082 → Order Service  
8083 → Inventory Service
8084 → Notification Service
9092 → Kafka Broker
5432 → PostgreSQL
6379 → Redis
8088 → Flink Web UI
4040 → Spark UI
3000 → React Frontend
9090 → Prometheus
3001 → Grafana
```

---

## 🏗️ Project Structure

```
enterprise-java-portfolio/
├── .devcontainer/              # Codespaces configuration
│   ├── devcontainer.json
│   ├── setup.sh
│   └── docker-compose.yml
├── microservices/              # Project 1: Event-driven services
│   ├── api-gateway/
│   ├── user-service/
│   ├── order-service/
│   ├── inventory-service/
│   └── notification-service/
├── streaming-analytics/        # Project 2: Spark/Flink applications
│   ├── flink-processor/
│   ├── spark-ml-pipeline/
│   └── real-time-dashboard/
├── infrastructure/             # Deployment & IaC
│   ├── terraform/
│   ├── kubernetes/
│   └── monitoring/             # Monitoring stack
├── docs/                       # Documentation
│   ├── api/
│   ├── architecture/
│   └── deployment/
└── streaming-analytics/        # Contains scripts in scripts/ folder
    ├── scripts/
    │   ├── init-db.sql
    │   └── start-pipeline.sh
    └── ...

---
```

---

## 🔄 Development Workflow

### Daily Development Cycle:
```bash
# 1. Start Codespace (if not running)
# 2. Start infrastructure services
docker-compose -f .devcontainer/docker-compose.yml up -d

# 3. Develop microservice or streaming app
cd microservices/user-service
./mvnw spring-boot:run

# 4. Test changes
./mvnw test
./mvnw verify  # Integration tests

# 5. Commit and push
git add .
git commit -m "feat: implement user service event publishing"
git push
```

### Testing Strategy:
```bash
# Unit Tests
./mvnw test

# Integration Tests (with Testcontainers)  
./mvnw verify

# Integration Tests with Maven
./mvnw verify

# Health Check Tests
./health-check.sh

# End-to-End Tests via CI/CD pipeline
# (See .github/workflows/ci-cd.yml for automated testing)

---
```

---

## 🚀 Running Individual Components

### Microservices (Project 1):
```bash
# Start infrastructure
docker-compose -f .devcontainer/docker-compose.yml up -d kafka postgres redis

# Start API Gateway
cd microservices/api-gateway
./mvnw spring-boot:run

# Start User Service  
cd microservices/user-service
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# Start Order Service
cd microservices/order-service  
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

### Streaming Analytics (Project 2):
```bash
# Start Flink cluster
docker-compose -f .devcontainer/docker-compose.yml up -d flink-jobmanager flink-taskmanager

# Submit Flink job
cd streaming-analytics/flink-processor
./mvnw clean package
flink run target/flink-processor-1.0.jar

# Start Spark application
cd streaming-analytics/spark-ml-pipeline
./mvnw clean package
spark-submit --class com.example.SparkMLPipeline target/spark-ml-pipeline-1.0.jar
```

---

## 📊 Monitoring & Observability

### Access Dashboards:
- **Flink Web UI:** http://localhost:8088
- **Spark UI:** http://localhost:4040  
- **Kafka UI:** Use Kafka Tool or kafkacat
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3001 (admin/admin)

### Key Metrics to Monitor:
- **Throughput:** Events/second processed
- **Latency:** 95th/99th percentile response times
- **Error Rate:** Failed requests percentage  
- **Resource Usage:** CPU, Memory, Disk I/O
- **Business Metrics:** Orders processed, recommendations served

---

## 🧪 Demo Scenarios

### Scenario 1: Event-Driven Architecture Demo
```bash
# 1. Start all microservices
./run.sh start

# 2. Create user and trigger events
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'

# 3. Place order (triggers saga)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":123,"quantity":2}'

# 4. Show event flow in Kafka
kafka-console-consumer --bootstrap-server localhost:9092 --topic user-events --from-beginning
```

### Scenario 2: Real-time Streaming Demo
```bash
# 1. Start streaming pipeline
./run.sh start

# 2. Check streaming analytics health
curl http://localhost:8081/actuator/health

# 3. View real-time processing
# - Streaming dashboard: http://localhost:8081
# - Spark UI: ML model training progress
# - Grafana: Real-time metrics
```

---

## 💰 Cost Optimization

### Codespaces Usage Tips:
- **Stop when not developing** (saves hours)
- **Use 2-core for light tasks** (default)
- **Upgrade to 8-core only for demos** ($0.36/hour)
- **Precompile during setup** (faster startups)

### Expected Costs:
- **Codespaces:** $0-20/month (60 free hours)
- **DigitalOcean staging:** $50-100/month  
- **DigitalOcean demo:** $20-40/month (auto-shutdown)

---

## 🎯 Portfolio Outcomes

After completing this development guide, you'll have:

✅ **Production-ready microservices** with event-driven architecture  
✅ **High-performance streaming applications** using Spark & Flink  
✅ **Modern Java 21 implementation** with latest features  
✅ **Comprehensive testing strategy** (Unit → E2E)  
✅ **Cloud deployment pipeline** with infrastructure as code  
✅ **Live demonstration capabilities** for interviews  
✅ **Professional development workflow** using industry standards  

**Interview Readiness:** 90%+ technical confidence for senior Java architect roles.

---

## 📞 Next Steps

1. **Setup Codespace:** Fork repo → Create Codespace
2. **Start Development:** Follow microservices implementation guide  
3. **Build Streaming:** Implement Flink/Spark applications
4. **Deploy to Cloud:** Setup DigitalOcean infrastructure
5. **Practice Demos:** Rehearse interview scenarios

**Questions?** Check `/docs` folder or create GitHub issue.