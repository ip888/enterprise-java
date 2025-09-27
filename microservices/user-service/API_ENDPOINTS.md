# 🚀 Enterprise Java User Service - All Working Endpoints

**Base URL:** `http://localhost:8081`  
**Date Verified:** September 27, 2025  
**Status:** ✅ All endpoints tested and working  

---

## 🏥 Health & Monitoring Endpoints

### Health Check
```http
GET /actuator/health
```
**Response:** Application and database status
```json
{
  "status": "UP",
  "components": {
    "r2dbc": {"status": "UP", "details": {"database": "PostgreSQL"}}
  }
}
```

### Application Info
```http
GET /actuator/info
```
**Response:** Application metadata

### Metrics
```http
GET /actuator/metrics
```
**Response:** Available application metrics

### Specific Metric
```http
GET /actuator/metrics/{metric-name}
```
**Example:** `/actuator/metrics/http.server.requests`

---

## 🔐 Authentication Endpoints

### User Registration
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com", 
  "password": "SecurePass123!",
  "firstName": "Test",
  "lastName": "User"
}
```
**Response:** User created with profile data
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 2,
    "username": "testuser",
    "email": "test@example.com",
    "profileCompleteness": 80,
    "accountStatus": "PENDING_VERIFICATION"
  }
}
```

### User Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "SecurePass123!"
}
```
**Response:** JWT token and user data
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {...}
  }
}
```

---

## 👤 User Management Endpoints

### Get Current User Profile (Protected)
```http
GET /api/v1/users/me
Authorization: Bearer {jwt-token}
```
**Response:** Current user's profile data
```json
{
  "success": true,
  "data": {
    "id": 2,
    "username": "testuser",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "profileCompleteness": 80
  }
}
```

### List Users with Pagination
```http
GET /api/v1/users?page=0&size=10
```
**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `sort`: Sort field (optional)
- `direction`: Sort direction (ASC/DESC)

**Response:** Paginated user list
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@enterprise-portfolio.com",
      "accountStatus": "ACTIVE"
    },
    {
      "id": 2,
      "username": "testuser",
      "email": "test@example.com",
      "accountStatus": "PENDING_VERIFICATION"
    }
  ]
}
```

### Search Users
```http
GET /api/v1/users/search?searchTerm={term}
```
**Query Parameters:**
- `searchTerm`: Search term for username, email, or name

**Response:** Matching users
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@enterprise-portfolio.com",
      "displayName": "Admin User"
    }
  ]
}
```

---

## 🔧 Tested Endpoint Examples

### Quick Test Commands

#### 1. Health Check
```bash
curl -X GET http://localhost:8081/actuator/health
```

#### 2. Register New User
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"new@example.com","password":"Password123!","firstName":"New","lastName":"User"}'
```

#### 3. Login and Get Token
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"new@example.com","password":"Password123!"}'
```

#### 4. Access Protected Endpoint
```bash
# First get token from login, then:
curl -X GET http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### 5. Search Users
```bash
curl -X GET "http://localhost:8081/api/v1/users/search?searchTerm=admin"
```

#### 6. List Users with Pagination
```bash
curl -X GET "http://localhost:8081/api/v1/users?page=0&size=5"
```

---

## 🔒 Authentication Requirements

### Public Endpoints (No Authentication)
- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/users/search`
- `GET /api/v1/users` (public listing)

### Protected Endpoints (JWT Required)
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me` (profile updates)
- `DELETE /api/v1/users/{id}` (admin only)
- `GET /api/v1/admin/*` (admin endpoints)

### JWT Token Usage
```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciI...
```

---

## 📊 Response Format

All API endpoints follow this consistent response format:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {...},
  "timestamp": "2025-09-27T16:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2025-09-27T16:30:00"
}
```

---

## 🛠️ Development & Testing

### Current Database Users
1. **Admin User**
   - Email: `admin@enterprise-portfolio.com`
   - Roles: ADMIN, USER
   - Status: ACTIVE

2. **Test Users** (created during demo)
   - Multiple test users with USER role
   - Status: PENDING_VERIFICATION

### Application Configuration
- **Port:** 8081
- **Database:** PostgreSQL on port 5432
- **JWT Expiry:** 1 hour (3600 seconds)
- **Profile:** dev (development mode)

---

**✅ All endpoints verified working on September 27, 2025**  
**🚀 Ready for production deployment**