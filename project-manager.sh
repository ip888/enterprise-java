#!/bin/bash

# Enterprise Java Project - Service Management Script
# Author: GitHub Copilot
# Date: September 28, 2025

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project directories
PROJECT_ROOT="/workspaces/enterprise-java"
MONITORING_DIR="$PROJECT_ROOT/infrastructure/monitoring"
STREAMING_DIR="$PROJECT_ROOT/streaming-analytics"
USER_SERVICE_DIR="$PROJECT_ROOT/microservices/user-service"

# Log file
LOG_FILE="$PROJECT_ROOT/project-manager.log"

# PID file for tracking Java processes
PIDS_FILE="$PROJECT_ROOT/.service-pids"

# Function to log messages
log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$LOG_FILE"
}

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=60
    local attempt=0
    
    log "Waiting for $service_name to be ready on $host:$port..."
    
    while [ $attempt -lt $max_attempts ]; do
        if nc -z "$host" "$port" 2>/dev/null; then
            log "$service_name is ready!"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done
    
    error "$service_name failed to start within 2 minutes"
    return 1
}

# Function to wait for HTTP endpoint
wait_for_http() {
    local url=$1
    local service_name=$2
    local max_attempts=60
    local attempt=0
    
    log "Waiting for $service_name HTTP endpoint: $url"
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s -f "$url" >/dev/null 2>&1; then
            log "$service_name HTTP endpoint is ready!"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done
    
    error "$service_name HTTP endpoint failed to respond within 2 minutes"
    return 1
}

# Function to start infrastructure services
start_infrastructure() {
    log "🚀 Starting Infrastructure Services..."
    
    # Start streaming infrastructure (Kafka, PostgreSQL, Redis)
    log "Starting streaming infrastructure..."
    cd "$STREAMING_DIR"
    docker-compose up -d kafka postgres redis kafka-ui
    
    # Wait for Kafka
    wait_for_service localhost 9092 "Kafka"
    
    # Wait for PostgreSQL
    wait_for_service localhost 5432 "PostgreSQL"
    
    # Wait for Redis
    wait_for_service localhost 6379 "Redis"
    
    # Wait for Kafka UI
    wait_for_http "http://localhost:8083" "Kafka UI"
    
    log "✅ Infrastructure services are ready!"
}

# Function to start monitoring services
start_monitoring() {
    log "📊 Starting Monitoring Services..."
    
    cd "$MONITORING_DIR"
    docker-compose up -d
    
    # Wait for Prometheus
    wait_for_http "http://localhost:9090/-/ready" "Prometheus"
    
    # Wait for Grafana
    wait_for_http "http://localhost:3000/api/health" "Grafana"
    
    # Wait for AlertManager
    wait_for_http "http://localhost:9093/-/ready" "AlertManager"
    
    log "✅ Monitoring services are ready!"
}

# Function to start Java applications
start_applications() {
    log "☕ Starting Java Applications..."
    
    # Initialize PID tracking file
    > "$PIDS_FILE"
    
    # Start User Service
    log "Starting User Service..."
    cd "$USER_SERVICE_DIR"
    nohup mvn spring-boot:run > "$PROJECT_ROOT/user-service.log" 2>&1 &
    USER_SERVICE_PID=$!
    echo "user-service:$USER_SERVICE_PID" >> "$PIDS_FILE"
    
    # Wait for User Service to be ready
    wait_for_http "http://localhost:8080/actuator/health" "User Service"
    
    # Start Streaming Analytics
    log "Starting Streaming Analytics..."
    cd "$STREAMING_DIR"
    nohup mvn spring-boot:run > "$PROJECT_ROOT/streaming-analytics.log" 2>&1 &
    STREAMING_PID=$!
    echo "streaming-analytics:$STREAMING_PID" >> "$PIDS_FILE"
    
    # Wait for Streaming Analytics to be ready
    wait_for_http "http://localhost:8081/actuator/health" "Streaming Analytics"
    
    log "✅ Java applications are ready!"
}

# Function to show service status
show_status() {
    log "📋 Service Status Report"
    echo
    
    echo -e "${BLUE}Infrastructure Services:${NC}"
    echo "  Kafka:          $(check_port 9092 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo "  PostgreSQL:     $(check_port 5432 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo "  Redis:          $(check_port 6379 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo "  Kafka UI:       $(check_port 8083 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo
    
    echo -e "${BLUE}Monitoring Services:${NC}"
    echo "  Prometheus:     $(check_port 9090 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo "  Grafana:        $(check_port 3000 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo "  AlertManager:   $(check_port 9093 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo
    
    echo -e "${BLUE}Java Applications:${NC}"
    echo "  User Service:   $(check_port 8080 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo "  Analytics:      $(check_port 8081 && echo -e "${GREEN}✅ Running${NC}" || echo -e "${RED}❌ Stopped${NC}")"
    echo
    
    echo -e "${BLUE}Service URLs:${NC}"
    echo "  User Service:      http://localhost:8080"
    echo "  Streaming Analytics: http://localhost:8081"
    echo "  Kafka UI:          http://localhost:8083"
    echo "  Prometheus:        http://localhost:9090"
    echo "  Grafana:           http://localhost:3000 (admin/admin)"
    echo "  AlertManager:      http://localhost:9093"
    echo
}

# Function to stop Java applications gracefully
stop_applications() {
    log "🛑 Stopping Java Applications..."
    
    if [ -f "$PIDS_FILE" ]; then
        while IFS=':' read -r service_name pid; do
            if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
                log "Stopping $service_name (PID: $pid)..."
                kill -TERM "$pid"
                
                # Wait for graceful shutdown (up to 30 seconds)
                local count=0
                while [ $count -lt 30 ] && kill -0 "$pid" 2>/dev/null; do
                    sleep 1
                    count=$((count + 1))
                done
                
                # Force kill if still running
                if kill -0 "$pid" 2>/dev/null; then
                    warn "Force killing $service_name..."
                    kill -KILL "$pid"
                fi
                
                log "✅ $service_name stopped"
            fi
        done < "$PIDS_FILE"
        
        rm -f "$PIDS_FILE"
    else
        warn "No PID file found, attempting to kill Java processes..."
        pkill -f "spring-boot:run" || true
    fi
}

# Function to stop all services
stop_all() {
    log "🛑 Stopping All Services..."
    
    # Stop Java applications first
    stop_applications
    
    # Stop Docker containers
    log "Stopping monitoring services..."
    cd "$MONITORING_DIR"
    docker-compose down
    
    log "Stopping infrastructure services..."
    cd "$STREAMING_DIR"
    docker-compose down
    
    log "✅ All services stopped"
}

# Function to restart all services
restart_all() {
    log "🔄 Restarting All Services..."
    stop_all
    sleep 5
    start_all
}

# Function to start all services in order
start_all() {
    log "🚀 Starting Enterprise Java Project..."
    echo
    
    # Check if any services are already running
    local running_services=0
    check_port 9092 && running_services=$((running_services + 1))
    check_port 5432 && running_services=$((running_services + 1))
    check_port 9090 && running_services=$((running_services + 1))
    
    if [ $running_services -gt 0 ]; then
        warn "Some services are already running. Use 'restart' to restart all services."
        show_status
        return 1
    fi
    
    # Start services in dependency order
    start_infrastructure
    start_monitoring
    start_applications
    
    echo
    log "🎉 All services started successfully!"
    show_status
}

# Function to view logs
view_logs() {
    local service=$1
    case $service in
        "user-service"|"user")
            if [ -f "$PROJECT_ROOT/user-service.log" ]; then
                tail -f "$PROJECT_ROOT/user-service.log"
            else
                error "User service log not found"
            fi
            ;;
        "streaming"|"analytics")
            if [ -f "$PROJECT_ROOT/streaming-analytics.log" ]; then
                tail -f "$PROJECT_ROOT/streaming-analytics.log"
            else
                error "Streaming analytics log not found"
            fi
            ;;
        "kafka")
            cd "$STREAMING_DIR" && docker-compose logs -f kafka
            ;;
        "postgres")
            cd "$STREAMING_DIR" && docker-compose logs -f postgres
            ;;
        "prometheus")
            cd "$MONITORING_DIR" && docker-compose logs -f prometheus
            ;;
        "grafana")
            cd "$MONITORING_DIR" && docker-compose logs -f grafana
            ;;
        "all"|"")
            tail -f "$PROJECT_ROOT"/*.log "$LOG_FILE"
            ;;
        *)
            error "Unknown service: $service"
            echo "Available services: user-service, streaming, kafka, postgres, prometheus, grafana, all"
            ;;
    esac
}

# Function to clean up everything (careful!)
clean_all() {
    read -p "This will remove ALL Docker containers, volumes, and logs. Continue? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log "🧹 Cleaning up everything..."
        
        stop_all
        
        # Remove Docker volumes
        cd "$STREAMING_DIR" && docker-compose down -v
        cd "$MONITORING_DIR" && docker-compose down -v
        
        # Remove logs
        rm -f "$PROJECT_ROOT"/*.log
        rm -f "$LOG_FILE"
        rm -f "$PIDS_FILE"
        
        log "✅ Cleanup completed"
    else
        log "Cleanup cancelled"
    fi
}

# Function to show usage
show_usage() {
    echo "Enterprise Java Project Manager"
    echo
    echo "Usage: $0 [COMMAND]"
    echo
    echo "Commands:"
    echo "  start           Start all services in proper order"
    echo "  stop            Stop all services gracefully"
    echo "  restart         Restart all services"
    echo "  status          Show service status"
    echo "  logs [service]  View logs (service: user-service, streaming, kafka, postgres, prometheus, grafana, all)"
    echo "  clean           Clean up everything (removes volumes and logs)"
    echo "  help            Show this help message"
    echo
    echo "Examples:"
    echo "  $0 start                    # Start all services"
    echo "  $0 logs user-service        # View user service logs"
    echo "  $0 status                   # Show service status"
    echo
}

# Signal handlers for graceful shutdown
trap 'log "Received SIGTERM, stopping services..."; stop_all; exit 0' TERM
trap 'log "Received SIGINT, stopping services..."; stop_all; exit 0' INT

# Main execution
case "${1:-help}" in
    "start")
        start_all
        ;;
    "stop")
        stop_all
        ;;
    "restart")
        restart_all
        ;;
    "status")
        show_status
        ;;
    "logs")
        view_logs "${2:-all}"
        ;;
    "clean")
        clean_all
        ;;
    "help"|"-h"|"--help")
        show_usage
        ;;
    *)
        error "Unknown command: $1"
        show_usage
        exit 1
        ;;
esac