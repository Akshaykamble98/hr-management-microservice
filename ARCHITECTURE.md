# Microservices Architecture - Detailed Documentation

## System Architecture

### High-Level Architecture

```
                                    ┌──────────────┐
                                    │   Clients    │
                                    │ (Web/Mobile) │
                                    └──────┬───────┘
                                           │
                                    ┌──────▼───────────────┐
                                    │   API Gateway        │
                                    │   (Port: 8080)       │
                                    │                      │
                                    │ • JWT Auth           │
                                    │ • Load Balancing     │
                                    │ • Circuit Breaker    │
                                    │ • Rate Limiting      │
                                    └──────┬───────────────┘
                                           │
                        ┌──────────────────┴──────────────────┐
                        │                                     │
                ┌───────▼─────────┐                  ┌────────▼────────┐
                │ HR Management   │                  │ Payroll Service │
                │ Service         │                  │                 │
                │ (Port: 8081)    │◄─────Feign──────┤ (Port: 8082)    │
                │                 │                  │                 │
                │ • Employees     │                  │ • Payroll       │
                │ • Departments   │                  │ • Salary        │
                │ • Leaves        │                  │ • Payments      │
                │ • Attendance    │                  │                 │
                └────────┬────────┘                  └─────────┬───────┘
                         │                                     │
                    ┌────▼────┐                           ┌────▼────┐
                    │   DB    │                           │   DB    │
                    │ (hr_db) │                           │(payroll)│
                    └─────────┘                           └─────────┘
```

### Service Discovery Flow

```
1. Service Startup:
   ┌──────────┐
   │ Service  │ ───register───► ┌───────────┐
   │ Instance │                 │  Eureka   │
   └──────────┘                 │  Server   │
                                └───────────┘

2. Service Discovery:
   ┌──────────┐
   │  Client  │ ───query──────► ┌───────────┐
   │ (Gateway)│ ◄──instances─── │  Eureka   │
   └──────────┘                 │  Server   │
                                └───────────┘

3. Load Balancing:
   ┌──────────┐     ┌───────────────┐
   │ Gateway  │────►│  HR Service   │ Instance 1
   │          │     ├───────────────┤
   │          │────►│  HR Service   │ Instance 2
   └──────────┘     └───────────────┘
```

## Communication Patterns

### 1. Synchronous Communication (Feign Client)

```
Payroll Service needs Employee Data:

┌──────────────┐         ┌─────────────┐         ┌──────────────┐
│   Payroll    │  GET    │   Eureka    │  GET    │  HR Service  │
│   Service    ├────────►│   Server    ├────────►│              │
│              │ resolve │             │ invoke  │              │
│              │◄────────┤             │◄────────┤              │
└──────────────┘ address └─────────────┘ response└──────────────┘

Steps:
1. Payroll calls: hrServiceClient.getEmployeeById(1)
2. Feign queries Eureka for HR Service instances
3. Feign load-balances and calls actual instance
4. Circuit breaker monitors the call
5. Response returns or fallback triggers
```

### 2. Asynchronous Communication (Kafka Events)

```
Event Publishing Flow:

┌──────────────┐         ┌─────────────┐         ┌──────────────┐
│  HR Service  │ publish │    Kafka    │subscribe│   Payroll    │
│              ├────────►│   Broker    ├────────►│   Service    │
│ Employee     │ event   │             │ event   │              │
│ Created      │         │ Topic:      │         │ Update       │
└──────────────┘         │ employee-   │         │ Records      │
                         │ events      │         └──────────────┘
                         └─────────────┘
```

## API Gateway Routing

### Request Flow Through Gateway

```
Client Request ───► Authentication ───► Route Matching ───► Load Balance ───► Service
                         │                    │                   │
                         ▼                    ▼                   ▼
                    JWT Validate          Path: /api/v1/     Choose Instance
                    Extract User          employees/**       from Eureka
                         │                    │                   │
                         ▼                    ▼                   ▼
                    Add Headers          Route to HR        Circuit Breaker
                    X-User-Id           Service            Check Status
                         │                    │                   │
                         └────────────────────┴───────────────────┘
                                             │
                                             ▼
                                      Forward Request
```

### Route Configuration

```yaml
Routes Defined:
1. HR Service Routes:
   Path: /api/v1/employees/**, /api/v1/departments/**, etc.
   Destination: lb://hr-management-service
   Filters: Authentication, Circuit Breaker, Retry

2. Payroll Service Routes:
   Path: /api/v1/payroll/**, /api/v1/salary/**
   Destination: lb://payroll-service
   Filters: Authentication, Circuit Breaker, Retry

3. Public Routes:
   Path: /api/v1/auth/**
   Destination: lb://hr-management-service
   Filters: None (Public Access)
```

## Circuit Breaker Pattern

### States and Transitions

```
                    ┌──────────┐
                    │  CLOSED  │ (Normal Operation)
                    │          │
                    │ Success  │
                    │ Requests │
                    └────┬─────┘
                         │
                 Failures > 50%
                         │
                         ▼
                    ┌──────────┐
                    │   OPEN   │ (Fail Fast)
                    │          │
                    │ Return   │
                    │ Fallback │
                    └────┬─────┘
                         │
                 After 10 seconds
                         │
                         ▼
                    ┌──────────┐
                    │ HALF_OPEN│ (Test Recovery)
                    │          │
                    │ Allow 3  │
                    │ Requests │
                    └────┬─────┘
                         │
                    ┌────┴────┐
            Success │         │ Failure
                    ▼         ▼
               ┌─────────┐ ┌────────┐
               │ CLOSED  │ │  OPEN  │
               └─────────┘ └────────┘
```

### Fallback Response Example

```json
{
  "success": false,
  "message": "HR Service is currently unavailable. Please try again later.",
  "timestamp": "2024-01-01T10:00:00",
  "service": "hr-management-service"
}
```

## Security Architecture

### JWT Authentication Flow

```
1. Login Request:
   Client ───► API Gateway ───► HR Service /auth/login
                                      │
                                      ▼
                                 Validate Credentials
                                      │
                                      ▼
                                 Generate JWT Token
                                      │
                                      ▼
   Client ◄─── API Gateway ◄─── Return Token

2. Authenticated Request:
   Client ───────────────────────────────────► API Gateway
   Header: Authorization: Bearer <token>           │
                                                    ▼
                                            JWT Validation
                                                    │
                                              ┌─────┴──────┐
                                         Valid│            │Invalid
                                              ▼            ▼
                                         Route to      Return 401
                                         Service       Unauthorized
```

### JWT Token Structure

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user@example.com",
  "iat": 1704067200,
  "exp": 1704153600,
  "roles": ["ROLE_HR_MANAGER"]
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

## Data Flow Examples

### Example 1: Creating a Payroll

```
1. Client Request:
   POST http://localhost:8080/api/v1/payroll
   {
     "employeeId": 1,
     "payPeriodStart": "2024-01-01",
     "payPeriodEnd": "2024-01-31",
     ...
   }

2. API Gateway:
   • Validates JWT token
   • Routes to Payroll Service

3. Payroll Service:
   • Receives request
   • Calls HR Service via Feign:
     GET /api/v1/employees/1
   
4. HR Service:
   • Returns employee details
   • Includes salary information

5. Payroll Service:
   • Calculates gross/net salary
   • Saves payroll record
   • Publishes "PAYROLL_CREATED" event to Kafka
   • Returns response

6. Response to Client:
   {
     "success": true,
     "data": {
       "id": 1,
       "employeeName": "John Doe",
       "grossSalary": 83000.00,
       "netSalary": 66500.00,
       ...
     }
   }
```

### Example 2: Circuit Breaker in Action

```
Scenario: HR Service is down

1. Payroll Service calls HR Service:
   hrServiceClient.getEmployeeById(1)

2. Request Fails (Connection Timeout)

3. Circuit Breaker Records Failure
   Failures: 1/10 (10%)

4. More requests fail...
   Failures: 5/10 (50%) ← Threshold Reached

5. Circuit Opens:
   • All subsequent requests fail immediately
   • Fallback method triggered
   • Returns error without attempting call

6. After 10 seconds:
   • Circuit moves to HALF_OPEN
   • Allows 3 test requests

7. If test requests succeed:
   • Circuit CLOSES
   • Normal operation resumes
```

## Distributed Tracing

### Trace Example

```
TraceId: abc123xyz
SpanId: span001

┌─────────────────────────────────────────────────────────┐
│ API Gateway                         [span001] 250ms     │
│   ├── JWT Validation                 [span002] 10ms    │
│   ├── Route Lookup                   [span003] 5ms     │
│   └── Forward Request                                  │
│       └─────────────────────────────────────────────┐  │
│                                                      │  │
│       ┌─────────────────────────────────────────────┘  │
│       │ Payroll Service                [span004] 180ms │
│       │   ├── Process Request           [span005] 20ms│
│       │   ├── Feign Call to HR                        │
│       │   │   └────────────────────────────────────┐  │
│       │   │                                         │  │
│       │   │   ┌─────────────────────────────────────┘ │
│       │   │   │ HR Service            [span006] 100ms│
│       │   │   │   ├── DB Query         [span007] 50ms│
│       │   │   │   └── Return Data                    │
│       │   │   └─────────────────────────────────────┐ │
│       │   │                                         │  │
│       │   ├── Calculate Salary          [span008] 30ms│
│       │   ├── Save to DB                [span009] 25ms│
│       │   └── Return Response                        │
│       └─────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘

View in Zipkin: http://localhost:9411/zipkin/traces/abc123xyz
```

## Caching Strategy

### Cache Hierarchy

```
1. Gateway Level:
   ┌────────────┐
   │ API Gateway│
   │   Cache    │ ← JWT Validation Results
   └────────────┘

2. Service Level:
   ┌────────────┐
   │  Redis     │ ← Employee Details
   │  Cache     │ ← Department Info
   └────────────┘ ← Payroll Records

3. Database Level:
   ┌────────────┐
   │ PostgreSQL │ ← Query Cache
   │  Cache     │ ← Index Cache
   └────────────┘
```

### Cache Invalidation

```
Event: Employee Updated

HR Service:
├── Update Database
├── Evict Cache (@CacheEvict)
├── Publish "EMPLOYEE_UPDATED" Event
└── Return Response

Payroll Service (Subscriber):
├── Receive Event
├── Evict Related Payroll Caches
└── Log Event
```

## Scaling Strategy

### Horizontal Scaling

```
Load Balancer (API Gateway)
          │
    ┌─────┼─────┐
    │     │     │
    ▼     ▼     ▼
┌──────┬──────┬──────┐
│ HR-1 │ HR-2 │ HR-3 │ (Auto-scaled based on load)
└──────┴──────┴──────┘
    │     │     │
    └─────┼─────┘
          ▼
    ┌──────────┐
    │ Database │ (Read Replicas)
    │ Cluster  │
    └──────────┘
```

### Resource Allocation

```yaml
Service Resource Limits:
├── API Gateway
│   ├── CPU: 0.5 cores
│   └── Memory: 512MB
├── HR Service
│   ├── CPU: 1 core
│   └── Memory: 1GB
├── Payroll Service
│   ├── CPU: 1 core
│   └── Memory: 1GB
└── Eureka Server
    ├── CPU: 0.5 cores
    └── Memory: 512MB
```

## Deployment Strategy

### Blue-Green Deployment

```
Step 1: Current State (Blue)
┌─────────────────┐
│ Load Balancer   │
└────────┬────────┘
         │
    ┌────▼────┐
    │ Blue    │ ← 100% Traffic
    │ v1.0    │
    └─────────┘

Step 2: Deploy Green
┌─────────────────┐
│ Load Balancer   │
└────────┬────────┘
         │
    ┌────┼────┐
    │    │    │
    ▼    │    ▼
┌─────┐  │  ┌─────┐
│Blue │  │  │Green│ ← 0% Traffic
│v1.0 │  │  │v1.1 │
└─────┘  │  └─────┘
         │
    100% Traffic

Step 3: Switch Traffic
┌─────────────────┐
│ Load Balancer   │
└────────┬────────┘
         │
    ┌────┼────┐
    │    │    │
    ▼    │    ▼
┌─────┐  │  ┌─────┐
│Blue │  │  │Green│ ← 100% Traffic
│v1.0 │  │  │v1.1 │
└─────┘  │  └─────┘
         │
    0% Traffic

Step 4: Cleanup
┌─────────────────┐
│ Load Balancer   │
└────────┬────────┘
         │
    ┌────▼────┐
    │ Green   │ ← 100% Traffic
    │ v1.1    │
    └─────────┘
```

## Monitoring & Alerts

### Key Metrics

```
Application Metrics:
├── Request Rate (requests/second)
├── Error Rate (%)
├── Response Time (ms)
│   ├── p50: 50ms
│   ├── p95: 200ms
│   └── p99: 500ms
├── Circuit Breaker Status
├── Cache Hit Rate (%)
└── Active Connections

Infrastructure Metrics:
├── CPU Usage (%)
├── Memory Usage (%)
├── Disk I/O
├── Network I/O
└── Database Connections

Business Metrics:
├── Employees Created
├── Payrolls Processed
├── Payments Completed
└── Leave Requests
```

## Conclusion

This architecture provides:
✅ Scalability through horizontal scaling
✅ Resilience through circuit breakers and retries
✅ Observability through distributed tracing
✅ Security through JWT authentication
✅ Performance through caching
✅ Flexibility through service discovery
