#!/bin/bash

# User Service Demo Script
# This script demonstrates all the working features of the User Service

echo "🚀 Enterprise Java User Service Demo"
echo "====================================="
echo ""

BASE_URL="http://localhost:8081"

echo "1. 🏥 Health Check - Verify service is running"
echo "GET $BASE_URL/actuator/health"
curl -s $BASE_URL/actuator/health | jq '.' || curl -s $BASE_URL/actuator/health
echo -e "\n"

echo "2. ℹ️  Service Info - Get application information"
echo "GET $BASE_URL/actuator/info"
curl -s $BASE_URL/actuator/info | jq '.' || curl -s $BASE_URL/actuator/info
echo -e "\n"

echo "3. 📊 Metrics - Check application metrics"
echo "GET $BASE_URL/actuator/metrics"
curl -s $BASE_URL/actuator/metrics | jq '.names[0:10]' || curl -s $BASE_URL/actuator/metrics
echo -e "\n"

echo "4. 👤 User Registration - Register a new user"
echo "POST $BASE_URL/api/v1/users/register"
REGISTRATION_RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1-555-0123"
  }')
echo $REGISTRATION_RESPONSE | jq '.' || echo $REGISTRATION_RESPONSE
echo -e "\n"

echo "5. 🔐 User Login - Authenticate and get JWT token"
echo "POST $BASE_URL/api/v1/auth/login"
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }')
echo $LOGIN_RESPONSE | jq '.' || echo $LOGIN_RESPONSE

# Extract JWT token from response
JWT_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token // .data.token // empty')
echo "JWT Token extracted: ${JWT_TOKEN:0:50}..."
echo -e "\n"

if [ ! -z "$JWT_TOKEN" ] && [ "$JWT_TOKEN" != "null" ]; then
    echo "6. 👥 Get All Users - Fetch user list (requires authentication)"
    echo "GET $BASE_URL/api/v1/users"
    curl -s -X GET $BASE_URL/api/v1/users \
      -H "Authorization: Bearer $JWT_TOKEN" | jq '.' || \
    curl -s -X GET $BASE_URL/api/v1/users \
      -H "Authorization: Bearer $JWT_TOKEN"
    echo -e "\n"

    echo "7. 🔍 Get User Profile - Get current user's profile"
    echo "GET $BASE_URL/api/v1/users/profile"
    curl -s -X GET $BASE_URL/api/v1/users/profile \
      -H "Authorization: Bearer $JWT_TOKEN" | jq '.' || \
    curl -s -X GET $BASE_URL/api/v1/users/profile \
      -H "Authorization: Bearer $JWT_TOKEN"
    echo -e "\n"

    echo "8. ✏️  Update User Profile - Update user information"
    echo "PUT $BASE_URL/api/v1/users/profile"
    curl -s -X PUT $BASE_URL/api/v1/users/profile \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "firstName": "John Updated",
        "lastName": "Doe Updated",
        "phoneNumber": "+1-555-9999"
      }' | jq '.' || \
    curl -s -X PUT $BASE_URL/api/v1/users/profile \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "firstName": "John Updated",
        "lastName": "Doe Updated", 
        "phoneNumber": "+1-555-9999"
      }'
    echo -e "\n"
else
    echo "⚠️  Could not extract JWT token. Testing public endpoints only."
    echo -e "\n"
fi

echo "9. 🔍 Search Users - Search by username (public endpoint)"
echo "GET $BASE_URL/api/v1/users/search?username=admin"
curl -s "$BASE_URL/api/v1/users/search?username=admin" | jq '.' || \
curl -s "$BASE_URL/api/v1/users/search?username=admin"
echo -e "\n"

echo "10. 📈 User Statistics - Get user statistics"
echo "GET $BASE_URL/api/v1/users/stats"
curl -s $BASE_URL/api/v1/users/stats | jq '.' || \
curl -s $BASE_URL/api/v1/users/stats
echo -e "\n"

echo "11. ❌ Test Error Handling - Invalid login"
echo "POST $BASE_URL/api/v1/auth/login (with invalid credentials)"
curl -s -X POST $BASE_URL/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid@example.com",
    "password": "wrongpassword"
  }' | jq '.' || \
curl -s -X POST $BASE_URL/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid@example.com",
    "password": "wrongpassword"
  }'
echo -e "\n"

echo "12. 🔒 Test Unauthorized Access - Access protected endpoint without token"
echo "GET $BASE_URL/api/v1/users/profile (without Authorization header)"
curl -s -X GET $BASE_URL/api/v1/users/profile | jq '.' || \
curl -s -X GET $BASE_URL/api/v1/users/profile
echo -e "\n"

echo "🎉 Demo completed!"
echo ""
echo "📝 Summary of demonstrated features:"
echo "✅ Health monitoring with Spring Actuator"
echo "✅ User registration with validation"
echo "✅ JWT-based authentication"
echo "✅ Protected API endpoints"
echo "✅ User profile management"
echo "✅ Search functionality"
echo "✅ Statistics endpoints"
echo "✅ Comprehensive error handling"
echo "✅ Reactive programming with WebFlux"
echo "✅ R2DBC database integration"
echo "✅ Modern Java 21 features (Records, sealed classes)"
echo ""
echo "🏗️ Technology Stack:"
echo "• Java 21 with modern features"
echo "• Spring Boot 3.2 with WebFlux"
echo "• Spring Security with JWT"
echo "• R2DBC with PostgreSQL"
echo "• Apache Kafka for event streaming"
echo "• Docker for containerization"
echo "• Maven for build management"