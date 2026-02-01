# Microservices Ecosystem - HR & Payroll Management

A complete microservices architecture featuring HR Management and Payroll services with API Gateway, Service Discovery, and distributed tracing.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway (8080)                      │
│           - Authentication & Authorization                      │
│           - Load Balancing                                      │
│           - Circuit Breaker                                     │
│           - Rate Limiting                                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
        ┌───────────▼──────────┐  ┌────▼──────────────┐
        │ HR Management (8081) │  │ Payroll (8082)    │
        │ - Employees          │  │ - Payroll         │
        │ - Departments        │◄─┤ - Salary          │
        │ - Leaves             │  │ - Feign Client    │
        │ - Attendance         │  │                   │
        └──────────────────────┘  └───────────────────┘
                 │                         │
        ┌────────┴────────┐       ┌───────┴────────┐
        │ PostgreSQL      │       │ PostgreSQL     │
        │ (hr_db)         │       │ (payroll_db)   │
        └─────────────────┘       └────────────────┘
                              │
        ┌─────────────────────────────────────────┐
        │     Shared Infrastructure               │
        ├─────────────────────────────────────────┤
        │ • Eureka Server (8761)                  │
        │ • Redis Cache (6379)                    │
        │ • Kafka (9092)                          │
        │ • Zipkin (9411)                         │
        └─────────────────────────────────────────┘
```

## 📦 Services

### 1. **Eureka Server** (Port: 8761)
- Service Discovery and Registration
- Health Monitoring
- Load Balancing Support

### 2. **API Gateway** (Port: 8080)
- Single Entry Point for all services
- JWT Authentication
- Request Routing
- Circuit Breaker with Resilience4j
- Rate Limiting with Redis
- CORS Configuration

### 3. **HR Management Service** (Port: 8081)
- Employee Management
- Department Management
- Leave Management
- Attendance Tracking
- Kafka Event Publishing

### 4. **Payroll Service** (Port: 8082)
- Payroll Processing
- Salary Calculation
- Integration with HR Service via Feign Client
- Approval Workflow
- Payment Processing

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local development)
- Maven 3.9+ (for local development)

### Running with Docker (Recommended)

1. **Clone the repository and navigate to the ecosystem directory**
```bash
cd microservices-ecosystem
```

2. **Build and start all services**
```bash
docker-compose up --build -d
```

3. **Check service health**
```bash
docker-compose ps
```

4. **View logs**
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway
docker-compose logs -f hr-service
docker-compose logs -f payroll-service
```

### Service Startup Order

The docker-compose file ensures services start in the correct order:
1. Infrastructure (PostgreSQL, Redis, Kafka, Zipkin)
2. Eureka Server
3. API Gateway
4. HR Management Service
5. Payroll Service

## 🌐 Access Points

| Service | URL | Description |
|---------|-----|-------------|
| API Gateway | http://localhost:8080 | Main entry point |
| Eureka Dashboard | http://localhost:8761 | Service registry |
| HR Service | http://localhost:8081 | Direct access (dev only) |
| Payroll Service | http://localhost:8082 | Direct access (dev only) |
| Zipkin | http://localhost:9411 | Distributed tracing |
| HR Swagger UI | http://localhost:8081/swagger-ui.html | API docs |
| Payroll Swagger UI | http://localhost:8082/swagger-ui.html | API docs |

## 📡 API Gateway Routes

All requests should go through the API Gateway at `http://localhost:8080`

### HR Management Routes
```
POST   /api/v1/employees              - Create employee
GET    /api/v1/employees              - Get all employees
GET    /api/v1/employees/{id}         - Get employee by ID
PUT    /api/v1/employees/{id}         - Update employee
DELETE /api/v1/employees/{id}         - Delete employee

GET    /api/v1/departments            - Get all departments
POST   /api/v1/departments            - Create department

GET    /api/v1/leaves                 - Get all leaves
POST   /api/v1/leaves                 - Create leave request

GET    /api/v1/attendances            - Get all attendance
POST   /api/v1/attendances            - Mark attendance
```

### Payroll Routes
```
POST   /api/v1/payroll                - Create payroll
GET    /api/v1/payroll                - Get all payrolls
GET    /api/v1/payroll/{id}           - Get payroll by ID
GET    /api/v1/payroll/employee/{id}  - Get employee payrolls
PUT    /api/v1/payroll/{id}/approve   - Approve payroll
PUT    /api/v1/payroll/{id}/pay       - Process payment
```

## 🔐 Authentication

The API Gateway uses JWT authentication. All requests (except public endpoints) require a valid JWT token.

### Public Endpoints (No Authentication Required)
- `/api/v1/auth/**`
- `/actuator/**`
- `/swagger-ui/**`
- `/v3/api-docs/**`

### Protected Endpoints
Include JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## 📊 Example API Calls

### 1. Create Employee (via Gateway)
```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "employeeId": "EMP001",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com",
    "phoneNumber": "1234567890",
    "dateOfBirth": "1990-01-01",
    "hireDate": "2024-01-01",
    "status": "ACTIVE",
    "employmentType": "FULL_TIME",
    "jobTitle": "Software Engineer",
    "salary": 75000.00
  }'
```

### 2. Create Payroll (via Gateway)
```bash
curl -X POST http://localhost:8080/api/v1/payroll \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "employeeId": 1,
    "payPeriodStart": "2024-01-01",
    "payPeriodEnd": "2024-01-31",
    "allowances": 5000.00,
    "bonuses": 2000.00,
    "overtimePay": 1000.00,
    "deductions": 1500.00,
    "tax": 15000.00
  }'
```

### 3. Get Employee Payrolls
```bash
curl http://localhost:8080/api/v1/payroll/employee/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🔄 Service Communication

### HR Service → Payroll Service
The Payroll Service communicates with HR Service using:
- **Feign Client**: For synchronous REST calls
- **Circuit Breaker**: For fault tolerance
- **Service Discovery**: For dynamic service location

Example Feign Client usage in Payroll Service:
```java
@FeignClient(name = "hr-management-service")
public interface HrServiceClient {
    @GetMapping("/api/v1/employees/{id}")
    ApiResponse<EmployeeDTO> getEmployeeById(@PathVariable Long id);
}
```

## 🛡️ Resilience Patterns

### Circuit Breaker
- **Sliding Window Size**: 10 requests
- **Failure Threshold**: 50%
- **Wait Duration in Open State**: 10 seconds
- **Half-Open Calls**: 3

### Retry Policy
- **Max Attempts**: 3
- **Backoff**: Exponential

### Fallback Responses
When a service is unavailable, the API Gateway returns:
```json
{
  "success": false,
  "message": "Service is currently unavailable. Please try again later.",
  "timestamp": "2024-01-01T10:00:00",
  "service": "service-name"
}
```

## 📈 Monitoring & Observability

### Health Checks
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# HR Service
curl http://localhost:8081/actuator/health

# Payroll Service
curl http://localhost:8082/actuator/health

# Eureka Server
curl http://localhost:8761/actuator/health
```

### Distributed Tracing
Access Zipkin at http://localhost:9411 to view:
- Request traces across services
- Service dependencies
- Performance metrics
- Error tracking

### Service Registry
Access Eureka at http://localhost:8761 to view:
- Registered services
- Service instances
- Health status
- Metadata

## 🗄️ Database Schema

### HR Database (hr_db)
- `employees` - Employee information
- `departments` - Department details
- `leaves` - Leave requests and approvals
- `attendances` - Daily attendance records

### Payroll Database (payroll_db)
- `payrolls` - Payroll records with salary calculations

## 🧪 Testing

### Integration Testing
Each service includes integration tests using Testcontainers:
```bash
# HR Service
cd hr-management-service
mvn test

# Payroll Service
cd payroll-service
mvn test
```

### API Testing with Postman
Import the provided Postman collection to test all endpoints.

## 🔧 Configuration

### Environment Variables
Key configuration can be overridden via environment variables:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka:8761/eureka/

# Redis
SPRING_DATA_REDIS_HOST=redis-host

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

## 🐛 Troubleshooting

### Service Not Registering with Eureka
```bash
# Check Eureka server is running
curl http://localhost:8761/actuator/health

# Check service logs
docker-compose logs -f service-name

# Verify network connectivity
docker network inspect microservices-ecosystem_microservices-network
```

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker-compose ps postgres-hr postgres-payroll

# Test connection
docker exec -it postgres-hr psql -U postgres -d hr_db

# Check Flyway migrations
docker-compose logs hr-service | grep Flyway
```

### API Gateway Not Routing
```bash
# Check Gateway logs
docker-compose logs -f api-gateway

# Verify route configuration
curl http://localhost:8080/actuator/gateway/routes

# Check service registration
curl http://localhost:8761/eureka/apps
```

## 📚 Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2.0 |
| Language | Java 17 |
| Build Tool | Maven |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| HTTP Client | OpenFeign |
| Circuit Breaker | Resilience4j |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Message Broker | Apache Kafka |
| Tracing | Zipkin |
| Monitoring | Spring Actuator + Prometheus |
| API Documentation | SpringDoc OpenAPI |
| Migration | Flyway |
| Containerization | Docker |

## 📝 Development

### Running Locally

1. **Start infrastructure**
```bash
docker-compose up postgres-hr postgres-payroll redis kafka zipkin -d
```

2. **Start Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
```

3. **Start HR Service**
```bash
cd hr-management-service
mvn spring-boot:run
```

4. **Start Payroll Service**
```bash
cd payroll-service
mvn spring-boot:run
```

5. **Start API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
```

### Hot Reload
Use Spring Boot DevTools for automatic restart during development.

## 🎯 Best Practices Implemented

1. **Microservices Patterns**
   - Service Discovery
   - API Gateway
   - Circuit Breaker
   - Event-Driven Architecture

2. **Security**
   - JWT Authentication
   - Role-Based Access Control
   - HTTPS Ready

3. **Resilience**
   - Circuit Breaker
   - Retry Logic
   - Timeout Configuration
   - Fallback Mechanisms

4. **Observability**
   - Distributed Tracing
   - Centralized Logging
   - Health Checks
   - Metrics

5. **Data Management**
   - Database per Service
   - Migration Scripts
   - Caching Strategy

## 📄 License

This project is licensed under the Apache License 2.0.

## 🤝 Support

For issues and questions:
- Create an issue in the repository
- Contact: support@company.com

---

**Happy Coding! 🚀**
