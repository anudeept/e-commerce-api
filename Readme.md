# E-Commerce Application

A Spring Boot based e-commerce application with MongoDB backend.

## Development Setup

### Prerequisites

- Java 17
- Gradle 8.x
- MongoDB 6.x
- Docker

### Local Development Environment Setup

1. Install Java 17

2. Install Gradle

3. Install MongoDB

4. Setup Docker Network
   ```bash
   # Create a network for the application
   docker network create ecommerce-network

   # Run MongoDB in Docker
   docker run --name mongodb --network ecommerce-network -p 27017:27017 -d mongo
   ```
4. Format Code
```bash
./gradlew spotlessApply
```
5. Build Docker Image
```bash
./gradlew jibDockerBuild
```
6. Run Docker Container
```bash
docker run --network ecommerce-network -p 8080:8080 ecommerce:0.0.1-SNAPSHOT
```

## API Documentation

The API documentation is available through Swagger UI:

- **URL**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

![Swagger UI](Swagger-doc.png)


## Authentication and Authorization

Login with credentials and get the JWT token. Use the JWT token to access the protected APIs.

The application supports role-based access control with three user roles:

- **ADMIN**: Full access to all APIs
  - Only one ADMIN user allowed in the system
  - Can manage products, users, and view all orders
- **STAFF**: Limited administrative access
  - Can manage products and view orders
- **CUSTOMER**: Basic access
  - Can view products and manage their own orders
  - Cannot access product management APIs

### User Registration

The application provides a user registration endpoint for new users to create an account.

#### Register a New User

```bash
curl -X POST http://localhost:8080/api/v1/ecommerce/auth/register \
     -H "Content-Type: application/json" \
     -d '{
           "username": "newuser",
           "email": "newuser@example.com",
           "password": "password123",
           "role": "ROLE_ADMIN"
     }'
```

### Login to the Application

```bash
curl -X POST http://localhost:8080/api/v1/ecommerce/auth/login \
     -H "Content-Type: application/json" \
     -d '{
           "username": "newuser",
           "password": "password123"
     }'
```
Above command will return a JWT token. Use the JWT token to access the protected APIs.
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2N2M2YWFiNmQxYWM3ZTM3MWI2NWVjNDgiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJlbWFpbCI6ImFudWRlZXBAZXhhbXBsZS5jb20iLCJpYXQiOjE3NDEwNzMwODQsImV4cCI6MTc0MTE1OTQ4NH0.oeB23VtMw6d9udcDFZ3lDv1crY-OJuzXdj-_fE9DL_g",
    "tokenType": "Bearer"
}
```

### Main API Endpoints
- **Auth**: `/api/v1/ecommerce/auth/*`
  - Registration, authentication, and Login
- **Users**: `/api/v1/ecommerce/users/*`
  - User CRUD operations (requires ADMIN role)
- **Products**: `/api/v1/ecommerce/products/*`
  - Product CRUD operations (requires ADMIN or STAFF role)
- **Cart**: `/api/v1/ecommerce/shopping-cart/*`
  - Shopping cart management
- **Orders**: `/api/v1/ecommerce/orders/*`
  - Order processing and management

## Integration Testing

The application includes integration tests for the main functionality.

### Run Integration Tests

```bash
./gradlew test
```

## Code Coverage Report

![Code Coverage Report](code-coverage.png)
