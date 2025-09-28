#!/bin/bash

# Local CI/CD Pipeline Test Script
# Mirrors the GitHub Actions workflow for local testing

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_ROOT="/workspaces/enterprise-java"
LOG_FILE="$PROJECT_ROOT/local-cicd-test.log"

# Function to log messages
log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] CI/CD:${NC} $1" | tee -a "$LOG_FILE"
}

warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$LOG_FILE"
    exit 1
}

info() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] INFO:${NC} $1" | tee -a "$LOG_FILE"
}

# Function to run a test step
run_step() {
    local step_name="$1"
    local command="$2"
    local allow_failure="${3:-false}"
    
    log "🔄 Running: $step_name"
    echo "Command: $command" >> "$LOG_FILE"
    
    if eval "$command" >> "$LOG_FILE" 2>&1; then
        log "✅ PASSED: $step_name"
        return 0
    else
        if [ "$allow_failure" = "true" ]; then
            warn "⚠️  FAILED (allowed): $step_name"
            return 0
        else
            error "❌ FAILED: $step_name"
            return 1
        fi
    fi
}

# Function to check prerequisites
check_prerequisites() {
    log "🔍 Checking Prerequisites"
    
    # Check Java
    if ! java -version >/dev/null 2>&1; then
        error "Java is not installed or not in PATH"
    fi
    
    # Check Maven
    if ! mvn -version >/dev/null 2>&1; then
        error "Maven is not installed or not in PATH"
    fi
    
    # Check Docker
    if ! docker --version >/dev/null 2>&1; then
        error "Docker is not installed or not in PATH"
    fi
    
    # Check Docker Compose
    if ! docker-compose --version >/dev/null 2>&1; then
        error "Docker Compose is not installed or not in PATH"
    fi
    
    log "✅ All prerequisites satisfied"
}

# Function to clean up any running services
cleanup_services() {
    log "🧹 Cleaning up existing services"
    
    # Kill Java processes
    pkill -f "spring-boot:run" 2>/dev/null || true
    
    # Stop Docker containers
    cd "$PROJECT_ROOT/infrastructure/monitoring"
    docker-compose down 2>/dev/null || true
    
    cd "$PROJECT_ROOT/streaming-analytics"
    docker-compose down 2>/dev/null || true
    
    sleep 3
    log "✅ Cleanup completed"
}

# Function to test Maven build
test_maven_build() {
    log "🏗️  Testing Maven Build Pipeline"
    
    cd "$PROJECT_ROOT"
    
    # Clean build
    run_step "Maven Clean" "mvn clean"
    
    # Compile
    run_step "Maven Compile" "mvn compile"
    
    # Run tests
    run_step "Maven Test" "mvn test"
    
    # Package
    run_step "Maven Package" "mvn package -DskipTests"
    
    # Verify
    run_step "Maven Verify" "mvn verify -DskipTests"
}

# Function to test User Service
test_user_service() {
    log "👤 Testing User Service"
    
    cd "$PROJECT_ROOT/microservices/user-service"
    
    # Build
    run_step "User Service - Clean Build" "mvn clean compile"
    
    # Test
    run_step "User Service - Unit Tests" "mvn test"
    
    # Package
    run_step "User Service - Package" "mvn package -DskipTests"
    
    # Verify JAR exists
    if [ -f "target/user-service-1.0.0.jar" ]; then
        log "✅ User Service JAR created successfully"
    else
        error "User Service JAR not found"
    fi
}

# Function to test Streaming Analytics
test_streaming_analytics() {
    log "📊 Testing Streaming Analytics"
    
    cd "$PROJECT_ROOT/streaming-analytics"
    
    # Build
    run_step "Streaming Analytics - Clean Build" "mvn clean compile"
    
    # Test
    run_step "Streaming Analytics - Unit Tests" "mvn test"
    
    # Package
    run_step "Streaming Analytics - Package" "mvn package -DskipTests"
    
    # Verify JAR exists
    if [ -f "target/streaming-analytics-1.0.0.jar" ]; then
        log "✅ Streaming Analytics JAR created successfully"
    else
        error "Streaming Analytics JAR not found"
    fi
}

# Function to test Docker builds
test_docker_builds() {
    log "🐳 Testing Docker Builds"
    
    # Test Streaming Analytics Docker build
    cd "$PROJECT_ROOT/streaming-analytics"
    if [ -f "Dockerfile" ]; then
        run_step "Docker Build - Streaming Analytics" "docker build -t streaming-analytics-test ." "true"
    else
        warn "No Dockerfile found for Streaming Analytics"
    fi
    
    # Test if monitoring stack can start
    cd "$PROJECT_ROOT/infrastructure/monitoring"
    run_step "Docker Compose - Monitoring Stack" "docker-compose config"
    
    cd "$PROJECT_ROOT/streaming-analytics"
    run_step "Docker Compose - Infrastructure" "docker-compose config"
}

# Function to test service startup
test_service_startup() {
    log "🚀 Testing Service Startup"
    
    cd "$PROJECT_ROOT"
    
    # Start infrastructure first
    cd "$PROJECT_ROOT/streaming-analytics"
    run_step "Start Infrastructure" "docker-compose up -d kafka postgres redis"
    
    # Wait a bit for services to be ready
    sleep 30
    
    # Check if Kafka is responding
    if nc -z localhost 9092 2>/dev/null; then
        log "✅ Kafka is responding"
    else
        warn "⚠️  Kafka not responding (may need more time)"
    fi
    
    # Check if PostgreSQL is responding
    if nc -z localhost 5432 2>/dev/null; then
        log "✅ PostgreSQL is responding"
    else
        warn "⚠️  PostgreSQL not responding (may need more time)"
    fi
    
    # Start monitoring stack
    cd "$PROJECT_ROOT/infrastructure/monitoring"
    run_step "Start Monitoring" "docker-compose up -d"
    
    sleep 10
    
    # Check monitoring services
    if nc -z localhost 9090 2>/dev/null; then
        log "✅ Prometheus is responding"
    else
        warn "⚠️  Prometheus not responding"
    fi
    
    if nc -z localhost 3000 2>/dev/null; then
        log "✅ Grafana is responding"
    else
        warn "⚠️  Grafana not responding"
    fi
}

# Function to test Java applications
test_java_applications() {
    log "☕ Testing Java Applications"
    
    # Test User Service startup (in background)
    cd "$PROJECT_ROOT/microservices/user-service"
    log "Starting User Service..."
    timeout 60 mvn spring-boot:run > "$PROJECT_ROOT/test-user-service.log" 2>&1 &
    USER_SERVICE_PID=$!
    
    # Wait for User Service to start
    local attempts=0
    local max_attempts=30
    while [ $attempts -lt $max_attempts ]; do
        if nc -z localhost 8080 2>/dev/null; then
            log "✅ User Service started successfully"
            break
        fi
        attempts=$((attempts + 1))
        sleep 2
    done
    
    if [ $attempts -eq $max_attempts ]; then
        warn "⚠️  User Service did not start within 60 seconds"
    else
        # Test health endpoint
        if curl -s -f "http://localhost:8080/actuator/health" >/dev/null 2>&1; then
            log "✅ User Service health endpoint responding"
        else
            warn "⚠️  User Service health endpoint not responding"
        fi
    fi
    
    # Clean up User Service
    kill $USER_SERVICE_PID 2>/dev/null || true
    
    # Test Streaming Analytics startup (in background)
    cd "$PROJECT_ROOT/streaming-analytics"
    log "Starting Streaming Analytics..."
    timeout 60 mvn spring-boot:run > "$PROJECT_ROOT/test-streaming.log" 2>&1 &
    STREAMING_PID=$!
    
    # Wait for Streaming Analytics to start
    attempts=0
    while [ $attempts -lt $max_attempts ]; do
        if nc -z localhost 8081 2>/dev/null; then
            log "✅ Streaming Analytics started successfully"
            break
        fi
        attempts=$((attempts + 1))
        sleep 2
    done
    
    if [ $attempts -eq $max_attempts ]; then
        warn "⚠️  Streaming Analytics did not start within 60 seconds"
    else
        # Test health endpoint
        if curl -s -f "http://localhost:8081/actuator/health" >/dev/null 2>&1; then
            log "✅ Streaming Analytics health endpoint responding"
        else
            warn "⚠️  Streaming Analytics health endpoint not responding"
        fi
    fi
    
    # Clean up Streaming Analytics
    kill $STREAMING_PID 2>/dev/null || true
}

# Function to run integration tests
test_integration() {
    log "🔗 Running Integration Tests"
    
    cd "$PROJECT_ROOT"
    
    # Run health check script
    if [ -x "./health-check.sh" ]; then
        run_step "Health Check Script" "./health-check.sh" "true"
    fi
    
    # Test API endpoints (if services are running)
    if nc -z localhost 8080 2>/dev/null; then
        run_step "API Test - User Service Health" "curl -s -f http://localhost:8080/actuator/health"
        run_step "API Test - User Service Info" "curl -s -f http://localhost:8080/actuator/info" "true"
    fi
    
    if nc -z localhost 8081 2>/dev/null; then
        run_step "API Test - Streaming Health" "curl -s -f http://localhost:8081/actuator/health"
    fi
    
    if nc -z localhost 9090 2>/dev/null; then
        run_step "API Test - Prometheus" "curl -s -f http://localhost:9090/-/ready"
    fi
}

# Function to test documentation and configuration
test_documentation() {
    log "📚 Testing Documentation and Configuration"
    
    cd "$PROJECT_ROOT"
    
    # Check if README exists and has content
    if [ -f "README.md" ] && [ -s "README.md" ]; then
        log "✅ README.md exists and has content"
    else
        warn "⚠️  README.md missing or empty"
    fi
    
    # Check if management scripts exist
    for script in "run.sh" "project-manager.sh" "health-check.sh"; do
        if [ -x "./$script" ]; then
            log "✅ $script is executable"
        else
            warn "⚠️  $script is not executable or missing"
        fi
    done
    
    # Test YAML configurations
    cd "$PROJECT_ROOT/infrastructure/monitoring"
    run_step "YAML Validation - Monitoring" "docker-compose config" "true"
    
    cd "$PROJECT_ROOT/streaming-analytics"
    run_step "YAML Validation - Infrastructure" "docker-compose config" "true"
}

# Function to cleanup after tests
final_cleanup() {
    log "🧹 Final Cleanup"
    
    # Stop all services
    pkill -f "spring-boot:run" 2>/dev/null || true
    
    cd "$PROJECT_ROOT/infrastructure/monitoring"
    docker-compose down 2>/dev/null || true
    
    cd "$PROJECT_ROOT/streaming-analytics"
    docker-compose down 2>/dev/null || true
    
    # Clean up test files
    rm -f "$PROJECT_ROOT/test-*.log"
    
    log "✅ Cleanup completed"
}

# Function to show test summary
show_summary() {
    echo
    log "📋 CI/CD Test Summary"
    echo "=========================="
    
    if [ -f "$LOG_FILE" ]; then
        local passed=$(grep -c "✅ PASSED:" "$LOG_FILE" || echo "0")
        local failed=$(grep -c "❌ FAILED:" "$LOG_FILE" || echo "0")
        local warnings=$(grep -c "⚠️.*FAILED (allowed):" "$LOG_FILE" || echo "0")
        local total_warnings=$(grep -c "WARNING:" "$LOG_FILE" || echo "0")
        
        echo -e "${GREEN}✅ Passed: $passed${NC}"
        echo -e "${RED}❌ Failed: $failed${NC}"
        echo -e "${YELLOW}⚠️  Warnings: $total_warnings (including $warnings allowed failures)${NC}"
        
        if [ $failed -eq 0 ]; then
            log "🎉 All CI/CD tests passed! Ready for GitHub push."
            return 0
        else
            error "💥 Some CI/CD tests failed. Fix issues before pushing to GitHub."
            return 1
        fi
    else
        error "No log file found"
        return 1
    fi
}

# Main execution
main() {
    log "🚀 Starting Local CI/CD Pipeline Test"
    log "Log file: $LOG_FILE"
    echo
    
    # Initialize log file
    > "$LOG_FILE"
    
    # Run test pipeline
    check_prerequisites
    cleanup_services
    test_maven_build
    test_user_service  
    test_streaming_analytics
    test_docker_builds
    test_service_startup
    test_java_applications
    test_integration
    test_documentation
    final_cleanup
    
    # Show summary
    show_summary
}

# Trap to ensure cleanup on exit
trap 'final_cleanup; exit 1' INT TERM

# Run main function
main "$@"