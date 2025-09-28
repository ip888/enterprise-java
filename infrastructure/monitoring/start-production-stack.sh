#!/bin/bash

# Enterprise Java - Production Monitoring Stack Launcher
# Replaces custom dashboard with industry-standard Grafana + Prometheus

set -e

echo "🚀 Starting Enterprise Java Production Monitoring Stack..."

# Function to check if service is healthy
check_service() {
    local service_name=$1
    local health_url=$2
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$health_url" > /dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "❌ $service_name failed to start within $(($max_attempts * 2)) seconds"
    return 1
}

# Start monitoring stack
echo "📊 Starting Prometheus, Grafana, and AlertManager..."
docker-compose up -d --build

# Wait for services to be ready
check_service "Prometheus" "http://localhost:9090/-/healthy"
check_service "Grafana" "http://localhost:3000/api/health"

echo ""
echo "🎉 Production Monitoring Stack is Ready!"
echo ""
echo "📊 Prometheus:   http://localhost:9090"
echo "📈 Grafana:      http://localhost:3000 (admin/admin)"
echo "🚨 AlertManager: http://localhost:9093"
echo ""
echo "🔧 Next Steps:"
echo "1. Start your applications (they should expose /metrics endpoints)"
echo "2. Access Grafana to view pre-configured dashboards"
echo "3. Configure alerts in AlertManager"
echo ""
echo "🚀 To start streaming analytics with production metrics:"
echo "   cd ../streaming-analytics"
echo "   mvn exec:java -Dexec.mainClass=\"com.enterprise.java.streaming.server.ProductionMetricsServer\""
echo ""
echo "🏠 To start user service:"
echo "   cd ../microservices/user-service"
echo "   mvn spring-boot:run"
echo ""