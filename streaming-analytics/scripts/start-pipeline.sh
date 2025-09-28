#!/bin/bash

# Enterprise Streaming Analytics - Start Script
# This script starts the complete streaming analytics pipeline

set -e

echo "🚀 Starting Enterprise Streaming Analytics Pipeline"
echo "=================================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker and Docker Compose are installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Create necessary directories
print_status "Creating data directories..."
mkdir -p data/spark-checkpoint
mkdir -p data/spark-warehouse  
mkdir -p data/flink-checkpoints
mkdir -p logs

# Check if Project 1 (User Service) is running
print_status "Checking Project 1 (User Service) status..."
if curl -f http://localhost:8080/actuator/health &> /dev/null; then
    print_success "Project 1 (User Service) is running and healthy"
else
    print_warning "Project 1 (User Service) is not accessible. Some integration features may not work."
    print_warning "Please ensure the User Service is running on port 8080"
fi

# Build the application
print_status "Building the streaming analytics application..."
if mvn clean package -DskipTests; then
    print_success "Application built successfully"
else
    print_error "Failed to build application"
    exit 1
fi

# Start infrastructure services
print_status "Starting infrastructure services (Kafka, PostgreSQL, Redis)..."
docker-compose up -d kafka postgres redis kafka-ui

# Wait for services to be ready
print_status "Waiting for services to be ready..."
sleep 30

# Check service health
print_status "Checking service health..."

# Check Kafka
if docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list &> /dev/null; then
    print_success "Kafka is ready"
    
    # Create required Kafka topics
    print_status "Creating Kafka topics..."
    docker-compose exec -T kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic user-events --partitions 3 --replication-factor 1
    docker-compose exec -T kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic security-alerts --partitions 2 --replication-factor 1
    docker-compose exec -T kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic real-time-analytics --partitions 2 --replication-factor 1
    docker-compose exec -T kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic analytics-insights --partitions 2 --replication-factor 1
    print_success "Kafka topics created"
else
    print_error "Kafka is not ready"
    exit 1
fi

# Check PostgreSQL
if docker-compose exec -T postgres pg_isready -U analytics_user -d analytics_db &> /dev/null; then
    print_success "PostgreSQL is ready"
else
    print_error "PostgreSQL is not ready"
    exit 1
fi

# Check Redis
if docker-compose exec -T redis redis-cli ping | grep -q PONG; then
    print_success "Redis is ready"
else
    print_warning "Redis is not responding, but will continue"
fi

# Start the streaming analytics application
print_status "Starting streaming analytics application..."
docker-compose up -d streaming-analytics

# Wait for application to start
sleep 15

# Check application health
print_status "Checking application health..."
if curl -f http://localhost:8082/api/health &> /dev/null; then
    print_success "Streaming Analytics application is healthy"
else
    print_warning "Application may still be starting up..."
fi

echo ""
print_success "🎉 Enterprise Streaming Analytics Pipeline started successfully!"
echo ""
echo "📊 Available Services:"
echo "  • Analytics Dashboard:    http://localhost:8082"
echo "  • Spark UI:              http://localhost:4040"
echo "  • Flink Web UI:          http://localhost:8081"
echo "  • Kafka UI:              http://localhost:8083"
echo "  • PostgreSQL:            localhost:5432"
echo "  • Redis:                 localhost:6379"
echo ""
echo "🔗 Integration:"
if curl -f http://localhost:8080/actuator/health &> /dev/null; then
    echo "  • User Service (Project 1): http://localhost:8080 ✅"
    echo "  • Monitoring Dashboard:     http://localhost:8080/dashboard ✅"
else
    echo "  • User Service (Project 1): Not running ⚠️"
fi
echo ""
echo "📋 Logs:"
echo "  • View application logs: docker-compose logs -f streaming-analytics"
echo "  • View all services:     docker-compose logs -f"
echo ""
echo "🛑 To stop:"
echo "  • Stop services:         docker-compose down"
echo "  • Stop with cleanup:     docker-compose down -v"
echo ""
print_status "Monitoring service startup..."
echo "Services will continue starting in the background."

# Show real-time logs for a few seconds
print_status "Showing recent logs (press Ctrl+C to exit log view)..."
timeout 30 docker-compose logs -f streaming-analytics || true

echo ""
print_success "✅ Setup complete! Visit http://localhost:8082 to view the Analytics Dashboard"