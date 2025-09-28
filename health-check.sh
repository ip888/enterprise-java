#!/bin/bash

# Enterprise Java Project - Health Check Script
# Performs comprehensive health checks on all services

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Health check results
TOTAL_CHECKS=0
PASSED_CHECKS=0

check_service() {
    local name=$1
    local host=${2:-localhost}
    local port=$3
    local endpoint=${4:-""}
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    printf "%-20s " "$name:"
    
    if ! nc -z "$host" "$port" 2>/dev/null; then
        echo -e "${RED}❌ Port $port not accessible${NC}"
        return 1
    fi
    
    if [ -n "$endpoint" ]; then
        if curl -s -f "http://$host:$port$endpoint" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ Healthy${NC}"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
            return 0
        else
            echo -e "${YELLOW}⚠️  Port open but endpoint unhealthy${NC}"
            return 1
        fi
    else
        echo -e "${GREEN}✅ Port accessible${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    fi
}

check_kafka_topics() {
    printf "%-20s " "Kafka Topics:"
    
    if ! nc -z localhost 9092 2>/dev/null; then
        echo -e "${RED}❌ Kafka not running${NC}"
        return 1
    fi
    
    # Try to list topics (requires kafka tools in container)
    if docker exec $(docker ps -q -f "name=kafka") kafka-topics --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Topics accessible${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${YELLOW}⚠️  Kafka running but topics not accessible${NC}"
    fi
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
}

check_database() {
    printf "%-20s " "PostgreSQL:"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if ! nc -z localhost 5432 2>/dev/null; then
        echo -e "${RED}❌ PostgreSQL not running${NC}"
        return 1
    fi
    
    # Try to connect to database
    if docker exec $(docker ps -q -f "name=postgres") pg_isready -U analytics_user -d analytics_db >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Database accessible${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${YELLOW}⚠️  PostgreSQL running but database not ready${NC}"
        return 1
    fi
}

check_java_application() {
    local name=$1
    local port=$2
    local expected_endpoints=("${@:3}")
    
    printf "%-20s " "$name:"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if ! nc -z localhost "$port" 2>/dev/null; then
        echo -e "${RED}❌ Not running${NC}"
        return 1
    fi
    
    local healthy_endpoints=0
    local total_endpoints=${#expected_endpoints[@]}
    
    for endpoint in "${expected_endpoints[@]}"; do
        if curl -s -f "http://localhost:$port$endpoint" >/dev/null 2>&1; then
            healthy_endpoints=$((healthy_endpoints + 1))
        fi
    done
    
    if [ $healthy_endpoints -eq $total_endpoints ]; then
        echo -e "${GREEN}✅ All endpoints healthy${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    elif [ $healthy_endpoints -gt 0 ]; then
        echo -e "${YELLOW}⚠️  Some endpoints healthy ($healthy_endpoints/$total_endpoints)${NC}"
        return 1
    else
        echo -e "${RED}❌ No endpoints responding${NC}"
        return 1
    fi
}

show_detailed_status() {
    echo
    echo -e "${BLUE}📊 Detailed Service Information:${NC}"
    echo
    
    # Prometheus targets
    if nc -z localhost 9090 2>/dev/null; then
        echo -e "${BLUE}Prometheus Targets:${NC}"
        curl -s "http://localhost:9090/api/v1/targets" | jq -r '.data.activeTargets[] | "\(.labels.job): \(.health)"' 2>/dev/null || echo "  Unable to fetch targets"
        echo
    fi
    
    # Kafka topics
    if nc -z localhost 9092 2>/dev/null; then
        echo -e "${BLUE}Kafka Topics:${NC}"
        docker exec $(docker ps -q -f "name=kafka") kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null | sed 's/^/  /' || echo "  Unable to list topics"
        echo
    fi
    
    # Java application versions
    if nc -z localhost 8080 2>/dev/null; then
        echo -e "${BLUE}User Service Info:${NC}"
        curl -s "http://localhost:8080/actuator/info" | jq -r 'to_entries[] | "  \(.key): \(.value)"' 2>/dev/null || echo "  Unable to fetch info"
        echo
    fi
    
    if nc -z localhost 8081 2>/dev/null; then
        echo -e "${BLUE}Streaming Analytics Info:${NC}"
        curl -s "http://localhost:8081/actuator/info" | jq -r 'to_entries[] | "  \(.key): \(.value)"' 2>/dev/null || echo "  Unable to fetch info"
        echo
    fi
}

main() {
    echo -e "${BLUE}🏥 Enterprise Java Project Health Check${NC}"
    echo "=================================================="
    echo
    
    # Infrastructure Services
    echo -e "${BLUE}Infrastructure Services:${NC}"
    check_service "Kafka" localhost 9092
    check_kafka_topics
    check_database
    check_service "Redis" localhost 6379
    check_service "Kafka UI" localhost 8083 "/actuator/health"
    echo
    
    # Monitoring Services  
    echo -e "${BLUE}Monitoring Services:${NC}"
    check_service "Prometheus" localhost 9090 "/-/ready"
    check_service "Grafana" localhost 3000 "/api/health"
    check_service "AlertManager" localhost 9093 "/-/ready"
    echo
    
    # Java Applications
    echo -e "${BLUE}Java Applications:${NC}"
    check_java_application "User Service" 8080 "/actuator/health" "/actuator/prometheus"
    check_java_application "Analytics Service" 8081 "/actuator/health" "/api/metrics"
    echo
    
    # Summary
    echo "=================================================="
    if [ $PASSED_CHECKS -eq $TOTAL_CHECKS ]; then
        echo -e "${GREEN}✅ All health checks passed ($PASSED_CHECKS/$TOTAL_CHECKS)${NC}"
        EXIT_CODE=0
    else
        echo -e "${YELLOW}⚠️  Health checks: $PASSED_CHECKS/$TOTAL_CHECKS passed${NC}"
        EXIT_CODE=1
    fi
    
    # Show detailed information if requested
    if [ "${1:-}" = "--verbose" ] || [ "${1:-}" = "-v" ]; then
        show_detailed_status
    fi
    
    echo
    echo "Use --verbose for detailed service information"
    
    exit $EXIT_CODE
}

# Check if required tools are available
if ! command -v nc >/dev/null 2>&1; then
    echo -e "${RED}Error: netcat (nc) is required but not installed${NC}"
    exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
    echo -e "${RED}Error: curl is required but not installed${NC}"
    exit 1
fi

main "$@"