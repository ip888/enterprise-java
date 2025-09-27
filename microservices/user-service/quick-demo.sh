#!/bin/bash

echo "🎯 SIMPLE USER SERVICE DEMO"
echo "============================"
echo ""

# 1. Check if Docker containers are running
echo "1. 📊 Infrastructure Status:"
echo "Docker containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(postgres|kafka|redis)"
echo ""

# 2. Test Spring Boot application
echo "2. 🏥 Application Health:"
echo "Testing Spring Boot on port 8081..."
curl -s -f http://localhost:8081/actuator/health > /dev/null && echo "✅ Service is UP" || echo "❌ Service is DOWN"
echo ""

# 3. Test basic endpoints
echo "3. 🔍 Testing Core Features:"
echo ""

echo "📝 User Registration (POST /api/v1/users/register):"
curl -s -X POST http://localhost:8081/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo_user",
    "email": "demo@example.com", 
    "password": "SecurePass123!",
    "firstName": "Demo",
    "lastName": "User"
  }' || echo "❌ Registration failed (service may be down)"
echo -e "\n"

echo "🔐 User Login (POST /api/v1/auth/login):"
curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "password": "SecurePass123!"
  }' || echo "❌ Login failed"
echo -e "\n"

echo "📊 User Statistics (GET /api/v1/users/stats):"
curl -s http://localhost:8081/api/v1/users/stats || echo "❌ Stats failed"
echo -e "\n"

echo "🎉 Demo completed! Check above results."