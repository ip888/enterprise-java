#!/bin/bash

# Enterprise Java Project - Quick Start Script
# This script provides common shortcuts for the main project manager

PROJECT_ROOT="/workspaces/enterprise-java"
MANAGER_SCRIPT="$PROJECT_ROOT/project-manager.sh"

# Make sure the main script is executable
chmod +x "$MANAGER_SCRIPT"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

case "${1:-help}" in
    "up"|"start")
        echo -e "${GREEN}🚀 Starting Enterprise Java Project...${NC}"
        "$MANAGER_SCRIPT" start
        ;;
    "down"|"stop")
        echo -e "${YELLOW}🛑 Stopping Enterprise Java Project...${NC}"
        "$MANAGER_SCRIPT" stop
        ;;
    "status"|"ps")
        "$MANAGER_SCRIPT" status
        ;;
    "logs"|"log")
        "$MANAGER_SCRIPT" logs "${2:-all}"
        ;;
    "restart"|"reload")
        echo -e "${BLUE}🔄 Restarting Enterprise Java Project...${NC}"
        "$MANAGER_SCRIPT" restart
        ;;
    "clean"|"reset")
        "$MANAGER_SCRIPT" clean
        ;;
    *)
        echo "Enterprise Java Project - Quick Commands"
        echo
        echo "Usage: $0 [COMMAND]"
        echo
        echo "Commands:"
        echo "  up|start        Start all services"
        echo "  down|stop       Stop all services"
        echo "  status|ps       Show service status"
        echo "  logs|log        View logs"
        echo "  restart|reload  Restart all services"
        echo "  clean|reset     Clean everything"
        echo
        echo "For more options, use: $MANAGER_SCRIPT help"
        ;;
esac