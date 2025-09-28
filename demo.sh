#!/bin/bash

# Demo script to show the project management system

echo "🚀 Enterprise Java Project Management System"
echo "============================================="
echo

echo "📁 Management Scripts Created:"
echo "  ✅ project-manager.sh  - Full service management"  
echo "  ✅ run.sh             - Quick commands"
echo "  ✅ health-check.sh    - Health monitoring"
echo "  ✅ MANAGEMENT.md      - Complete documentation"
echo

echo "🎯 Quick Start Commands:"
echo "  ./run.sh start         # Start all services"
echo "  ./run.sh status        # Check service status"  
echo "  ./run.sh logs          # View logs"
echo "  ./run.sh stop          # Stop all services"
echo

echo "🔧 Full Management Commands:"
echo "  ./project-manager.sh start    # Start with dependency order"
echo "  ./project-manager.sh status   # Detailed status report"
echo "  ./project-manager.sh logs kafka    # View Kafka logs"
echo "  ./project-manager.sh clean    # Clean everything"
echo

echo "🏥 Health Monitoring:"
echo "  ./health-check.sh              # Basic health check"
echo "  ./health-check.sh --verbose    # Detailed diagnostics"
echo

echo "📋 Service Startup Order:"
echo "  1. Infrastructure: Kafka → PostgreSQL → Redis → Kafka UI"
echo "  2. Monitoring: Prometheus → Grafana → AlertManager"  
echo "  3. Applications: User Service → Streaming Analytics"
echo

echo "🌐 Service URLs (once running):"
echo "  User Service:        http://localhost:8080"
echo "  Streaming Analytics: http://localhost:8081"
echo "  Kafka UI:           http://localhost:8083"
echo "  Prometheus:         http://localhost:9090"
echo "  Grafana:            http://localhost:3000 (admin/admin)"
echo

echo "💡 To get started:"
echo "  1. chmod +x *.sh                # Make scripts executable"
echo "  2. ./run.sh start               # Start everything"
echo "  3. ./health-check.sh            # Verify all services"
echo "  4. Open Grafana at localhost:3000"
echo

echo "📖 For complete documentation, see: MANAGEMENT.md"