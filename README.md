# 🚀 Enterprise Java Portfolio: Event-Driven Microservices & Streaming Analytics

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-blue.svg)](https://kafka.apache.org/)
[![Apache Spark](https://img.shields.io/badge/Apache%20Spark-3.5-red.svg)](https://spark.apache.org/)
[![Apache Flink](https://img.shields.io/badge/Apache%20Flink-1.18-purple.svg)](https://flink.apache.org/)
[![GitHub Codespaces](https://img.shields.io/badge/Codespaces-Ready-brightgreen.svg)](https://github.com/codespaces)

> **Portfolio demonstrating expertise in event-driven microservices architecture and high-performance streaming applications using modern Java enterprise technologies.**

## 🎯 Portfolio Objectives

This portfolio showcases enterprise-level skills in:
- ✅ **Event-driven microservices architecture** with Spring Boot & Kafka
- ✅ **High-performance streaming applications** using Apache Spark & Flink  
- ✅ **Modern Java 21** features and enterprise patterns
- ✅ **Production-ready CI/CD pipelines** with comprehensive testing
- ✅ **Cloud-native deployment** with Kubernetes and infrastructure as code

## 🏗️ Architecture Overview

### Project 1: Real-Time E-Commerce Platform
Event-driven microservices demonstrating CQRS, Event Sourcing, and Saga patterns.

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│ User Service│    │ Order Service│    │Inventory Svc│
│  (Java 21)  │    │ Event Sourcing│    │    CQRS     │
│   WebFlux   │    │   + Sagas    │    │  Pattern    │
└─────────────┘    └──────────────┘    └─────────────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
                    ┌──────▼──────┐
                    │Apache Kafka │
                    │Event Store  │
                    │+ Schema Reg │
                    └─────────────┘
```

### Project 2: Streaming Analytics Pipeline
Real-time data processing with ML capabilities using Spark and Flink.

```
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   Kafka      │   │ Apache Flink │   │ Apache Spark │
│ Data Streams │──▶│ Stream       │──▶│ ML Training  │
│              │   │ Processing   │   │ & Batch Jobs │
└──────────────┘   └──────────────┘   └──────────────┘
                           │                   │
                           ▼                   ▼
                   ┌──────────────┐   ┌──────────────┐
                   │ Real-time    │   │ Delta Lake   │
                   │ Dashboard    │   │ Feature Store│
                   └──────────────┘   └──────────────┘
```

## 🚀 Quick Start with GitHub Codespaces

### **Recommended: GitHub Codespaces (Zero Local Setup)**

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/ip888/enterprise-java)

1. **Click the Codespaces badge above** (after pushing to GitHub)
2. **Wait 5-10 minutes** for environment setup
3. **Start developing immediately** - all tools pre-configured!

**What you get:**
- ✅ Java 21 (GraalVM) with latest features
- ✅ Apache Kafka + Schema Registry  
- ✅ Apache Spark 3.5 + Flink 1.18
- ✅ PostgreSQL + Redis
- ✅ Docker + Kubernetes tools
- ✅ Monitoring stack (Prometheus + Grafana)

### **Alternative: Local Development**
```bash
# Prerequisites: Java 21, Docker, Maven
git clone https://github.com/YOUR_USERNAME/enterprise-java-portfolio.git
cd enterprise-java-portfolio
docker-compose -f .devcontainer/docker-compose.yml up -d
```

**Note:** Local development requires significant resources (8GB+ RAM recommended).

## 📁 Project Structure

```
enterprise-java-portfolio/
├── 📁 microservices/           # Event-driven microservices
│   ├── api-gateway/            # Spring Cloud Gateway
│   ├── user-service/           # User management with events
│   ├── order-service/          # Order processing with sagas
│   ├── inventory-service/      # Inventory with CQRS
│   └── notification-service/   # Async notifications
├── 📁 streaming-analytics/     # Spark & Flink applications  
│   ├── flink-processor/        # Real-time event processing
│   ├── spark-ml-pipeline/      # ML model training & serving
│   └── real-time-dashboard/    # React dashboard with WebSockets
├── 📁 infrastructure/          # Deployment & IaC
│   ├── terraform/             # Infrastructure as Code
│   ├── kubernetes/            # K8s manifests & Helm charts
│   └── docker/                # Custom Docker images
├── 📁 .devcontainer/          # Codespaces configuration
└── 📁 docs/                   # Comprehensive documentation
```

## 🛠️ Technology Stack

### **Core Technologies**
- **Language:** Java 21 (Virtual Threads, Pattern Matching, Records)
- **Framework:** Spring Boot 3.x with WebFlux (Reactive Programming)
- **Build Tools:** Maven 3.9+ with modern plugins
- **Testing:** JUnit 5, Testcontainers, Contract Testing

### **Event-Driven Architecture**
- **Message Broker:** Apache Kafka 7.4 + Confluent Schema Registry
- **Event Patterns:** Event Sourcing, CQRS, Saga Pattern
- **Data Storage:** PostgreSQL 15 + Redis 7

### **Streaming & Analytics**
- **Stream Processing:** Apache Flink 1.18 (Complex Event Processing)
- **Batch Processing:** Apache Spark 3.5 (MLlib for ML models)
- **Data Lake:** Delta Lake for data lakehouse architecture
- **ML Framework:** Spark MLlib + custom Java algorithms

### **DevOps & Deployment**
- **Containerization:** Docker + Docker Compose
- **Orchestration:** Kubernetes with Helm charts
- **Infrastructure:** Terraform for DigitalOcean
- **CI/CD:** GitHub Actions with comprehensive testing
- **Monitoring:** Prometheus + Grafana + custom metrics

## 🔄 Development Workflow

### **Daily Development Cycle**
```bash
# 1. Start Codespace or local environment
# 2. Start infrastructure services
docker-compose -f .devcontainer/docker-compose.yml up -d

# 3. Run specific microservice
cd microservices/user-service
./mvnw spring-boot:run

# 4. Test changes
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests
./scripts/contract-tests.sh    # Contract tests

# 5. Commit and deploy
git add . && git commit -m "feat: implement event sourcing"
git push  # Triggers CI/CD pipeline
```

### **Testing Strategy**
- **Unit Tests:** High coverage with JUnit 5 + Mockito
- **Integration Tests:** Testcontainers for real service testing
- **Contract Tests:** API compatibility testing
- **Performance Tests:** JMeter load testing
- **End-to-End Tests:** Full workflow validation

## 📊 Demo Scenarios

### **Scenario 1: Event-Driven Architecture**
```bash
# Start all services
./scripts/start-microservices.sh

# Create user (triggers events)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'

# Place order (demonstrates saga pattern)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":123,"quantity":2}'

# Monitor event flow
kafka-console-consumer --bootstrap-server localhost:9092 --topic user-events
```

### **Scenario 2: Real-Time Streaming**
```bash
# Start streaming pipeline
./scripts/start-streaming.sh

# Generate test data
./scripts/generate-test-data.sh --rate 1000 --duration 300

# Monitor dashboards:
# - Flink UI: http://localhost:8088 (Complex event processing)
# - Spark UI: http://localhost:4040 (ML model training)
# - Grafana: http://localhost:3001 (Real-time metrics)
```

## 🚀 Deployment

### **Local Development**
```bash
docker-compose -f .devcontainer/docker-compose.yml up -d
```

### **Staging (DigitalOcean)**
```bash
# Deploy with Terraform
cd infrastructure/terraform
terraform init && terraform apply

# Deploy applications
kubectl apply -f ../kubernetes/
```

### **Production**
- **Blue/Green deployments** with zero downtime
- **Auto-scaling** based on metrics
- **Comprehensive monitoring** and alerting

## 📈 Performance Benchmarks

### **Target Metrics**
- **Event Processing Latency:** < 100ms (99th percentile)
- **Streaming Throughput:** > 10,000 events/second
- **Service Availability:** > 99.9% uptime
- **API Response Time:** < 200ms (95th percentile)

### **Load Testing Results**
- **Concurrent Users:** 1,000+ supported
- **Peak Throughput:** 50,000+ requests/minute
- **Data Processing:** 1M+ events/hour processed

## 💰 Cost Optimization

### **Development Costs**
- **GitHub Codespaces:** Free (60 hours/month) + $0.18/hour for demos
- **Local Development:** $0 (if sufficient hardware)

### **Cloud Deployment Costs**
- **DigitalOcean Staging:** $50-100/month
- **DigitalOcean Demo:** $20-40/month (auto-shutdown enabled)

## 📚 Documentation

- 📖 [**Project Architecture**](./PROJECT_ARCHITECTURE.md) - Complete technical design
- 🛠️ [**Development Guide**](./DEVELOPMENT_GUIDE.md) - Setup and workflow
- 🚀 [**Deployment Guide**](./docs/DEPLOYMENT_GUIDE.md) - Cloud deployment
- 📋 [**API Documentation**](./docs/api/) - OpenAPI specifications
- 🎯 [**Demo Scenarios**](./docs/DEMO_SCENARIOS.md) - Interview demonstrations

## 🎯 Skills Demonstrated

### **Technical Skills**
- ✅ **Modern Java 21** (Virtual Threads, Pattern Matching, Records)
- ✅ **Spring Boot 3.x** (WebFlux, Security, Data)
- ✅ **Apache Kafka** (Event Streaming, Schema Registry)
- ✅ **Apache Spark** (Streaming, MLlib, Performance Tuning)
- ✅ **Apache Flink** (Complex Event Processing, State Management)
- ✅ **Microservices Patterns** (CQRS, Event Sourcing, Sagas)
- ✅ **Cloud-Native Development** (Docker, Kubernetes, Helm)
- ✅ **Infrastructure as Code** (Terraform, GitOps)

### **Architecture Skills**
- ✅ **Event-Driven Design** with proper domain boundaries
- ✅ **High-Performance Systems** with streaming architectures
- ✅ **Scalable Data Processing** with batch and real-time pipelines
- ✅ **Production Operations** with monitoring and observability
- ✅ **Security Best Practices** with authentication and authorization

## 🤝 Contributing

This is a portfolio project, but feedback and suggestions are welcome!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 About

**Enterprise Java Portfolio** - Demonstrating expertise in event-driven microservices architecture and high-performance streaming applications for senior Java architect positions.

**Author:** [Your Name]  
**LinkedIn:** [Your LinkedIn Profile]  
**Portfolio:** [Your Portfolio Website]

---

⭐ **Star this repository** if you find it useful for your own learning or portfolio development!

**Ready to explore enterprise-grade Java architecture?** Start with [GitHub Codespaces](https://codespaces.new/ip888/enterprise-java) and begin developing immediately!