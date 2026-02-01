# HR Management Microservice - Architecture Documentation

## System Overview

The HR Management Microservice is a cloud-native, production-ready application built using Spring Boot and following microservices best practices.

## Architecture Patterns

### 1. Layered Architecture

```
┌─────────────────────────────────────────────┐
│         Presentation Layer                  │
│  (REST Controllers, DTOs, OpenAPI)          │
├─────────────────────────────────────────────┤
│         Service Layer                       │
│  (Business Logic, Transactions)             │
├─────────────────────────────────────────────┤
│         Data Access Layer                   │
│  (Repositories, JPA Entities)               │
├─────────────────────────────────────────────┤
│         Database                            │
│  (PostgreSQL)                               │
└─────────────────────────────────────────────┘
```

### 2. Microservices Patterns Implemented

#### Service Discovery
- **Pattern**: Client-side service discovery
- **Implementation**: Netflix Eureka
- **Purpose**: Dynamic service registration and discovery

#### API Gateway (Ready for integration)
- **Pattern**: API Gateway pattern
- **Purpose**: Single entry point, routing, authentication

#### Circuit Breaker
- **Pattern**: Circuit breaker
- **Implementation**: Resilience4j
- **Purpose**: Fault tolerance, graceful degradation

#### Event-Driven Architecture
- **Pattern**: Event sourcing / CQRS (simplified)
- **Implementation**: Apache Kafka
- **Purpose**: Asynchronous communication, event publishing

#### Caching
- **Pattern**: Cache-aside
- **Implementation**: Redis
- **Purpose**: Performance optimization

#### Database per Service
- **Pattern**: Database per service
- **Implementation**: Dedicated PostgreSQL instance
- **Purpose**: Data isolation, independent scaling

## Component Architecture

### Controllers Layer
```
EmployeeController
├── Handles HTTP requests
├── Validates input (Bean Validation)
├── Delegates to service layer
└── Returns standardized ApiResponse

Responsibilities:
- Request/Response transformation
- HTTP status code management
- Security annotations (@PreAuthorize)
- API documentation (Swagger annotations)
```

### Service Layer
```
EmployeeService (Interface)
└── EmployeeServiceImpl
    ├── Business logic
    ├── Transaction management (@Transactional)
    ├── Cache management
    ├── Event publishing
    └── Exception handling

Key Features:
- Business rule enforcement
- Data validation
- Cross-cutting concerns (caching, logging)
- Event-driven integration
```

### Repository Layer
```
EmployeeRepository
├── Extends JpaRepository
├── Custom query methods
├── Specifications for dynamic queries
└── Native queries where needed

Features:
- CRUD operations
- Custom finder methods
- Pagination support
- Sorting capabilities
```

### Entity Model

```
Employee (Aggregate Root)
├── Personal Information
│   ├── employeeId (unique identifier)
│   ├── firstName, lastName
│   ├── email (unique)
│   ├── phoneNumber
│   └── dateOfBirth
├── Employment Information
│   ├── hireDate
│   ├── status (enum)
│   ├── employmentType (enum)
│   ├── jobTitle
│   └── salary
├── Relationships
│   ├── Department (ManyToOne)
│   ├── Manager (ManyToOne - self-reference)
│   ├── Subordinates (OneToMany)
│   ├── Leaves (OneToMany)
│   └── Attendances (OneToMany)
├── Address (Embedded)
└── Audit Fields
    ├── createdAt, updatedAt
    ├── createdBy, lastModifiedBy
    └── version (optimistic locking)
```

## Security Architecture

### Authentication Flow

```
Client Request
    ↓
JWT Authentication Filter
    ↓
[Extract JWT from Authorization header]
    ↓
[Validate JWT signature & expiration]
    ↓
[Load UserDetails]
    ↓
[Set Authentication in SecurityContext]
    ↓
Controller (with @PreAuthorize check)
    ↓
Service Layer
    ↓
Response
```

### Authorization Model

```
Roles:
├── ADMIN
│   └── Full system access
├── HR_MANAGER
│   ├── Employee CRUD
│   ├── Leave management
│   └── Reports
├── MANAGER
│   ├── View team members
│   └── Approve leaves
└── EMPLOYEE
    ├── View own profile
    └── Submit leave requests
```

## Data Flow

### Create Employee Flow

```
1. Client → POST /api/v1/employees
2. EmployeeController
   ├── Validates JWT
   ├── Checks @PreAuthorize("hasRole('HR_MANAGER')")
   └── Validates DTO (@Valid)
3. EmployeeService
   ├── Checks duplicate email/employeeId
   ├── Maps DTO → Entity (MapStruct)
   ├── Enriches with relationships (Department, Manager)
   └── Saves to database
4. Repository
   ├── Persists to PostgreSQL
   └── Returns saved entity
5. EmployeeService
   ├── Publishes EMPLOYEE_CREATED event to Kafka
   ├── Invalidates cache (@CacheEvict)
   └── Maps Entity → DTO
6. EmployeeController
   └── Returns ApiResponse with HTTP 201
```

## Event-Driven Architecture

### Kafka Topics

```
employee-events
├── EMPLOYEE_CREATED
├── EMPLOYEE_UPDATED
└── EMPLOYEE_DELETED

leave-events
├── LEAVE_REQUESTED
├── LEAVE_APPROVED
└── LEAVE_REJECTED

attendance-events
├── ATTENDANCE_MARKED
└── ATTENDANCE_UPDATED
```

### Event Structure

```json
{
  "eventType": "EMPLOYEE_CREATED",
  "employeeId": 123,
  "employeeNumber": "EMP001",
  "email": "john.doe@company.com",
  "timestamp": "2024-01-01T10:00:00",
  "metadata": {
    "correlationId": "uuid",
    "source": "hr-management-service"
  }
}
```

## Caching Strategy

### Cache Configuration

```
Cache Type: Redis
TTL: 10 minutes
Eviction: On update/delete

Cached Entities:
├── Employee (by ID)
├── Employee (by employeeId)
└── Department (by ID)

Cache Keys:
employees::123
employees::EMP001
departments::5
```

### Cache Invalidation

```
@CacheEvict triggered on:
├── createEmployee()
├── updateEmployee()
└── deleteEmployee()

Strategy: Write-through cache
```

## Database Design

### Schema Overview

```sql
departments (1) ←─── (N) employees
                         ↓ (1)
                         ├──→ (N) leaves
                         ├──→ (N) attendances
                         └──→ (N) subordinates
```

### Indexing Strategy

```
Primary Indexes:
├── employees(id) - Primary Key
├── departments(id) - Primary Key
├── leaves(id) - Primary Key
└── attendances(id) - Primary Key

Secondary Indexes:
├── employees(email) - Unique, for login
├── employees(employee_id) - Unique, business key
├── employees(department_id) - Foreign key
├── employees(manager_id) - Self-reference
├── leaves(employee_id) - Lookups
├── leaves(status) - Filtering
├── attendances(employee_id) - Lookups
└── attendances(date) - Date-based queries
```

## Observability

### Logging

```
Framework: SLF4J + Logback
Format: JSON (via logstash-logback-encoder)

Log Levels:
├── ERROR: System failures, exceptions
├── WARN: Business rule violations
├── INFO: Important business events
└── DEBUG: Detailed execution flow

Structured Logging:
{
  "timestamp": "2024-01-01T10:00:00Z",
  "level": "INFO",
  "thread": "http-nio-8081-exec-1",
  "logger": "c.c.hr.service.EmployeeServiceImpl",
  "message": "Employee created successfully",
  "employeeId": 123,
  "correlationId": "uuid"
}
```

### Monitoring

```
Spring Boot Actuator Endpoints:
├── /actuator/health - Health checks
├── /actuator/metrics - Application metrics
├── /actuator/info - Application info
└── /actuator/prometheus - Prometheus metrics

Custom Metrics:
├── employee.created.count
├── leave.approved.count
├── attendance.marked.count
└── http.requests.duration
```

### Distributed Tracing

```
Framework: Micrometer + Zipkin
Trace Context Propagation: B3

Trace Structure:
Service A (hr-management)
  ├── Span: HTTP POST /employees
  ├── Span: Database INSERT
  ├── Span: Kafka PUBLISH
  └── Span: Cache EVICT
```

## Scalability Considerations

### Horizontal Scaling

```
Load Balancer
    ↓
┌─────────┬─────────┬─────────┐
│Service 1│Service 2│Service 3│ (Stateless)
└─────────┴─────────┴─────────┘
    ↓           ↓           ↓
┌─────────────────────────────┐
│    Shared PostgreSQL        │
└─────────────────────────────┘
┌─────────────────────────────┐
│    Shared Redis Cache       │
└─────────────────────────────┘
```

### Performance Optimization

1. **Connection Pooling**: HikariCP with optimal settings
2. **Query Optimization**: Indexed columns, proper joins
3. **Lazy Loading**: Fetch strategies for relationships
4. **Caching**: Redis for frequently accessed data
5. **Pagination**: Limit result set sizes
6. **Async Processing**: Event publishing via Kafka

## Resilience Patterns

### Circuit Breaker Configuration

```yaml
resilience4j.circuitbreaker:
  instances:
    default:
      sliding-window-size: 10
      failure-rate-threshold: 50%
      wait-duration-in-open-state: 10s
      
States:
CLOSED → [Failures > 50%] → OPEN
OPEN → [After 10s] → HALF_OPEN
HALF_OPEN → [3 successes] → CLOSED
```

### Retry Mechanism

```yaml
resilience4j.retry:
  instances:
    default:
      max-attempts: 3
      wait-duration: 1s
      
Retry on:
├── Transient failures
├── Timeout exceptions
└── Connection errors
```

## Deployment Architecture

### Container Strategy

```
Dockerfile (Multi-stage build)
├── Stage 1: Maven build
│   └── Compile and package application
└── Stage 2: Runtime
    └── JRE with application JAR

Resource Requirements:
├── CPU: 0.5-1 core
├── Memory: 512MB-1GB
└── Storage: Minimal (logs to stdout)
```

### Docker Compose Stack

```
Services:
├── PostgreSQL (Database)
├── Redis (Cache)
├── Kafka (Message Broker)
├── Eureka (Service Discovery)
├── Zipkin (Tracing)
└── HR Service (Application)

Networks:
└── hr-network (Bridge)

Volumes:
└── postgres_data (Persistent storage)
```

## API Design Principles

### RESTful Best Practices

1. **Resource-based URLs**: `/api/v1/employees/{id}`
2. **HTTP Methods**: GET, POST, PUT, DELETE
3. **Status Codes**: 200, 201, 400, 404, 500
4. **Versioning**: URI-based (`/v1/`)
5. **HATEOAS**: Ready for implementation
6. **Content Negotiation**: JSON (extensible to XML)

### Response Format

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-01T10:00:00Z"
}
```

## Security Best Practices

1. **Authentication**: JWT with secure secret
2. **Authorization**: Role-based access control
3. **HTTPS**: Required in production
4. **SQL Injection**: Prevented via JPA
5. **XSS**: Prevented via input validation
6. **CSRF**: Disabled for stateless API
7. **Rate Limiting**: Ready for implementation
8. **Secrets Management**: Externalized configuration

## Testing Strategy

```
Testing Pyramid:
├── Unit Tests (70%)
│   ├── Service layer logic
│   ├── Mapper conversions
│   └── Utility functions
├── Integration Tests (20%)
│   ├── Controller + Service + Repository
│   ├── Database integration
│   └── Testcontainers
└── E2E Tests (10%)
    └── Full workflow scenarios
```

## Future Enhancements

1. **GraphQL API**: Alternative to REST
2. **gRPC**: For inter-service communication
3. **Multi-tenancy**: Tenant isolation
4. **Advanced Search**: Elasticsearch integration
5. **File Storage**: Document management (S3)
6. **Notifications**: Email/SMS integration
7. **Analytics**: Reporting and dashboards
8. **Audit Trail**: Complete change history
9. **Workflow Engine**: Approval workflows
10. **Mobile API**: Optimized endpoints

## Conclusion

This architecture provides a solid foundation for a scalable, maintainable, and production-ready HR Management system. It follows industry best practices and can be extended to meet future requirements.
