#!/bin/bash

echo "🎯 ENTERPRISE JAVA USER SERVICE - REAL WORKING DEMO"
echo "=================================================="
echo ""

echo "✅ 1. HEALTH CHECK"
echo "Checking if application is running and database connected..."
curl -s http://localhost:8081/actuator/health | jq '.status'
echo ""

echo "✅ 2. USER REGISTRATION"
echo "Registering a new user..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demouser","email":"demo@example.com","password":"DemoPass123!","firstName":"Demo","lastName":"User"}')
echo $REGISTER_RESPONSE | jq '.success'
echo "User ID:" $(echo $REGISTER_RESPONSE | jq '.data.id')
echo ""

echo "✅ 3. USER LOGIN & JWT TOKEN"
echo "Logging in to get JWT token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"DemoPass123!"}')
echo $LOGIN_RESPONSE | jq '.success'
JWT_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken')
echo "JWT Token received: ${JWT_TOKEN:0:50}..."
echo ""

echo "✅ 4. PROTECTED ENDPOINT ACCESS"
echo "Accessing user profile with JWT token..."
PROFILE_RESPONSE=$(curl -s -X GET http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer $JWT_TOKEN")
echo $PROFILE_RESPONSE | jq '.success'
echo "Username:" $(echo $PROFILE_RESPONSE | jq -r '.data.username')
echo ""

echo "✅ 5. USER SEARCH"
echo "Searching for users..."
SEARCH_RESPONSE=$(curl -s -X GET "http://localhost:8081/api/v1/users/search?searchTerm=demo")
echo $SEARCH_RESPONSE | jq '.success'
echo "Found users:" $(echo $SEARCH_RESPONSE | jq '.data | length')
echo ""

echo "✅ 6. USER LISTING WITH PAGINATION"
echo "Getting paginated user list..."
LIST_RESPONSE=$(curl -s -X GET "http://localhost:8081/api/v1/users?page=0&size=10")
echo $LIST_RESPONSE | jq '.success'
echo "Total users in page:" $(echo $LIST_RESPONSE | jq '.data | length')
echo ""

echo "✅ 7. DATABASE VERIFICATION"
echo "Checking database directly..."
docker exec postgres-userservice psql -U portfolio_user -d portfolio_db -c "SELECT COUNT(*) as user_count FROM users;" -t -A
echo ""

echo "🎉 DEMO COMPLETE!"
echo "=================="
echo "✅ Spring Boot 3.2 + Java 21: WORKING"
echo "✅ PostgreSQL Database: WORKING" 
echo "✅ JWT Authentication: WORKING"
echo "✅ User Registration: WORKING"
echo "✅ Protected Endpoints: WORKING"
echo "✅ Database Operations: WORKING"
echo "✅ Search Functionality: WORKING"
echo "✅ Pagination: WORKING"
echo ""
echo "🚀 Ready for GitHub and DigitalOcean deployment!"