# HR Management Microservice

A comprehensive HR Management microservice built with Spring Boot, following microservices architecture patterns and best practices.

## Features

- ✅ Employee Management (CRUD operations)
- ✅ Department Management
- ✅ Leave Management
- ✅ Attendance Tracking
- ✅ RESTful API with OpenAPI/Swagger documentation
- ✅ JWT-based Authentication & Authorization
- ✅ Role-based Access Control
- ✅ Database migration with Flyway
- ✅ Redis caching
- ✅ Kafka event publishing
- ✅ Service discovery with Eureka
- ✅ Distributed tracing with Zipkin
- ✅ Circuit breaker with Resilience4j
- ✅ Health checks and metrics
- ✅ Docker support

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Cache**: Redis
- **Message Broker**: Kafka
- **Service Discovery**: Eureka
- **API Documentation**: SpringDoc OpenAPI 3
- **Security**: Spring Security with JWT
- **Monitoring**: Spring Boot Actuator, Prometheus
- **Tracing**: Zipkin
- **Database Migration**: Flyway
- **Mapping**: MapStruct
- **Testing**: JUnit 5, Testcontainers

## Architecture

```
hr-management-service/
├── src/
│   ├── main/
│   │   ├── java/com/company/hr/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── event/           # Event publishers
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── mapper/          # MapStruct mappers
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── security/        # Security components
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── db/migration/    # Flyway migrations
│   │       └── application.yml  # Configuration
│   └── test/                    # Test classes
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Domain Model

### Entities

1. **Employee**: Core entity with personal info, employment details, and relationships
2. **Department**: Organizational units
3. **Leave**: Employee leave requests and approvals
4. **Attendance**: Daily attendance records
5. **Address**: Embedded address information

## API Endpoints

### Employee Management

- `POST /api/v1/employees` - Create employee
- `PUT /api/v1/employees/{id}` - Update employee
- `GET /api/v1/employees/{id}` - Get employee by ID
- `GET /api/v1/employees/employee-id/{employeeId}` - Get by employee ID
- `GET /api/v1/employees` - Get all employees (paginated)
- `GET /api/v1/employees/department/{departmentId}` - Get by department
- `GET /api/v1/employees/manager/{managerId}` - Get by manager
- `GET /api/v1/employees/status/{status}` - Get by status
- `GET /api/v1/employees/search?keyword={keyword}` - Search employees
- `DELETE /api/v1/employees/{id}` - Delete employee

Similar endpoints exist for Department, Leave, and Attendance management.

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose (for local development)
- PostgreSQL 16+ (if running without Docker)
- Redis (if running without Docker)
- Kafka (if running without Docker)

### Running with Docker Compose (Recommended)

1. Clone the repository
```bash
git clone <repository-url>
cd hr-management-service
```

2. Build and run all services
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Redis cache
- Kafka message broker
- Eureka service registry
- Zipkin distributed tracing
- HR Management Service

3. Access the application:
- API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- Eureka Dashboard: http://localhost:8761
- Zipkin: http://localhost:9411

### Running Locally

1. Start required services:
```bash
docker-compose up postgres redis kafka eureka-server zipkin -d
```

2. Update `application.yml` if needed

3. Run the application:
```bash
mvn spring-boot:run
```

## Configuration

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hr_db
    username: postgres
    password: postgres
  
  data:
    redis:
      host: localhost
      port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092

jwt:
  secret: <your-secret-key>
  expiration: 86400000
```

## Security

The service implements JWT-based authentication with role-based access control:

- **ADMIN**: Full access
- **HR_MANAGER**: Employee and HR operations
- **MANAGER**: View team members
- **EMPLOYEE**: View own information

## Testing

Run tests with:
```bash
mvn test
```

The project uses:
- JUnit 5 for unit testing
- Testcontainers for integration testing
- MockMVC for controller testing

## Monitoring

### Health Check
```bash
curl http://localhost:8081/actuator/health
```

### Metrics
```bash
curl http://localhost:8081/actuator/metrics
```

### Prometheus Endpoint
```bash
curl http://localhost:8081/actuator/prometheus
```

## Events

The service publishes events to Kafka topics:

- `employee-events`: Employee CRUD operations
- `leave-events`: Leave applications and approvals
- `attendance-events`: Attendance records

Event format:
```json
{
  "eventType": "EMPLOYEE_CREATED",
  "employeeId": 1,
  "employeeNumber": "EMP001",
  "email": "john.doe@company.com",
  "timestamp": "2024-01-01T10:00:00"
}
```

## Caching Strategy

Redis is used for caching frequently accessed data:
- Employee details (10 min TTL)
- Department information
- Cache eviction on updates

## Database Migration

Flyway manages database schema versions. Migrations are in `src/main/resources/db/migration/`.

To create a new migration:
1. Add file: `V{version}__{description}.sql`
2. Run the application (auto-migration enabled)

## API Documentation

Interactive API documentation is available at:
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

## Circuit Breaker

Resilience4j circuit breaker protects external service calls:
- Sliding window size: 10 requests
- Failure threshold: 50%
- Wait duration in open state: 10 seconds

## Best Practices Implemented

1. **Layered Architecture**: Controller → Service → Repository
2. **DTO Pattern**: Separation of domain and API models
3. **Exception Handling**: Global exception handler
4. **Validation**: Bean validation with annotations
5. **Logging**: Structured logging with SLF4J
6. **Transaction Management**: Declarative transactions
7. **Async Processing**: Event publishing
8. **Caching**: Redis for performance
9. **Security**: JWT authentication, role-based authorization
10. **API Versioning**: URL-based versioning
11. **Documentation**: OpenAPI/Swagger
12. **Testing**: Unit and integration tests
13. **Containerization**: Docker support

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the Apache License 2.0.

## Support

For issues and questions, please create an issue in the repository or contact hr@company.com.
