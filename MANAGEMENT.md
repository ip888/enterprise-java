# Enterprise Java Project Management

This directory contains comprehensive management scripts for running the entire Enterprise Java project stack.

## 🚀 Quick Start

```bash
# Start all services
./run.sh start

# Check status
./run.sh status

# View logs
./run.sh logs

# Stop all services
./run.sh stop
```

## 📋 Management Scripts

### 1. `run.sh` - Quick Commands
Simple interface for common operations:
- `./run.sh start` - Start all services
- `./run.sh stop` - Stop all services  
- `./run.sh status` - Show service status
- `./run.sh logs` - View all logs
- `./run.sh restart` - Restart everything
- `./run.sh clean` - Clean up everything

### 2. `project-manager.sh` - Full Management
Comprehensive service management with proper startup order:

```bash
# Start all services in dependency order
./project-manager.sh start

# Stop all services gracefully
./project-manager.sh stop

# Restart all services
./project-manager.sh restart

# Show detailed status
./project-manager.sh status

# View specific service logs
./project-manager.sh logs user-service
./project-manager.sh logs streaming
./project-manager.sh logs kafka

# Clean up everything (removes volumes!)
./project-manager.sh clean
```

### 3. `health-check.sh` - Health Monitoring
Comprehensive health checks for all services:

```bash
# Basic health check
./health-check.sh

# Verbose health check with details
./health-check.sh --verbose
```

## 🏗️ Service Architecture & Dependencies

### Startup Order
1. **Infrastructure Services** (Docker containers)
   - Kafka (port 9092)
   - PostgreSQL (port 5432) 
   - Redis (port 6379)
   - Kafka UI (port 8083)

2. **Monitoring Services** (Docker containers)
   - Prometheus (port 9090)
   - Grafana (port 3000)
   - AlertManager (port 9093)

3. **Java Applications** (Maven processes)
   - User Service (port 8080)
   - Streaming Analytics (port 8081)

### Service Dependencies
- **User Service** requires: Kafka, PostgreSQL, Redis
- **Streaming Analytics** requires: Kafka, PostgreSQL 
- **Monitoring** scrapes metrics from: Java applications
- **Grafana** uses: Prometheus as data source

## 🔍 Service URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| User Service | http://localhost:8080 | - |
| Streaming Analytics | http://localhost:8081 | - |
| Kafka UI | http://localhost:8083 | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin/admin |
| AlertManager | http://localhost:9093 | - |

## 📊 Monitoring Endpoints

| Service | Health Check | Metrics |
|---------|--------------|---------|
| User Service | `/actuator/health` | `/actuator/prometheus` |
| Streaming Analytics | `/actuator/health` | `/api/metrics` |
| Prometheus | `/-/ready` | `/metrics` |
| Grafana | `/api/health` | - |

## 🗂️ Log Files

All logs are stored in the project root:
- `user-service.log` - User service application logs
- `streaming-analytics.log` - Streaming analytics logs
- `project-manager.log` - Management script logs

Docker container logs can be viewed with:
```bash
# View specific container logs
./project-manager.sh logs kafka
./project-manager.sh logs prometheus
./project-manager.sh logs grafana
```

## 🛠️ Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   ./project-manager.sh status  # Check what's running
   ./project-manager.sh stop    # Stop everything
   ./project-manager.sh start   # Restart
   ```

2. **Services Won't Start**
   ```bash
   ./health-check.sh --verbose  # Detailed diagnostics
   ./project-manager.sh logs    # Check logs
   ```

3. **Kafka Connection Issues**
   ```bash
   # Check if Kafka is ready
   nc -z localhost 9092
   
   # View Kafka logs
   ./project-manager.sh logs kafka
   ```

4. **Database Connection Issues**
   ```bash
   # Check PostgreSQL
   nc -z localhost 5432
   
   # View database logs
   ./project-manager.sh logs postgres
   ```

### Clean Start
If everything is broken:
```bash
./project-manager.sh clean  # Removes all data!
./project-manager.sh start  # Fresh start
```

## ⚙️ Configuration

### Environment Variables
The scripts automatically configure:
- Kafka bootstrap servers
- Database connection strings  
- Service discovery between components

### Docker Networks
- `streaming-network` - Infrastructure services
- `monitoring_monitoring` - Monitoring stack

### Data Persistence
Docker volumes are used for:
- Kafka data (`kafka-data`)
- PostgreSQL data (`postgres-data`) 
- Redis data (`redis-data`)
- Grafana data (`grafana_data`)

## 🚨 Signal Handling

The management scripts handle shutdown signals gracefully:
- `SIGTERM` / `SIGINT` - Triggers graceful shutdown
- Java processes get 30 seconds to shutdown before force kill
- Docker containers are stopped with proper cleanup

## 🔐 Security Notes

- Default Grafana credentials: `admin/admin`
- All services run on localhost (development setup)
- No external authentication configured (development only)
- Database passwords are in plain text (development only)

## 📈 Performance

### Resource Requirements
- **Memory**: ~4GB total (2GB for Java apps, 2GB for infrastructure)
- **CPU**: 2+ cores recommended
- **Disk**: 1GB for logs and data

### Scaling
For production, consider:
- External databases
- Load balancing
- Service mesh
- Container orchestration (Kubernetes)

## 🧪 Testing

```bash
# Start everything
./run.sh start

# Run health checks
./health-check.sh

# Test user service
curl http://localhost:8080/actuator/health

# Test streaming analytics  
curl http://localhost:8081/actuator/health

# View metrics in Grafana
open http://localhost:3000
```