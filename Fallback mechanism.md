# ⚡ Fallback & Circuit Breaker Mechanism

## 🤔 What is a Fallback?

A **fallback** is a backup response when a service call **fails** or is **unavailable**. Instead of showing an error, the system provides a graceful alternative response.

**Think of it like:**
- Your primary route is blocked → Take alternate route
- Restaurant is closed → Order from backup restaurant
- Service is down → Show cached data or friendly message

---

## 📍 Where Fallback is Used in Our Project

We have **2 levels** of fallback:

### 1️⃣ **API Gateway Level Fallback**
### 2️⃣ **Service-to-Service Fallback** (Payroll → HR)

---

## 🌐 1. API Gateway Level Fallback

### **Location**: `api-gateway/src/main/java/com/company/gateway/config/GatewayConfig.java`

```java
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // HR Service Route
                .route("hr-service", r -> r
                        .path("/api/v1/employees/**", "/api/v1/departments/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("hrServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/hr"))  // ← FALLBACK!
                                //                      ↑ If HR service fails, call this
                        )
                        .uri("lb://hr-management-service"))

                // Payroll Service Route
                .route("payroll-service", r -> r
                        .path("/api/v1/payroll/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("payrollServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payroll"))  // ← FALLBACK!
                        )
                        .uri("lb://payroll-service"))
                .build();
    }
}
```

### **Fallback Controller**: `api-gateway/src/main/java/com/company/gateway/config/FallbackController.java`

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/hr")
    public ResponseEntity<Map<String, Object>> hrServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "HR Management Service is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "hr-management-service");
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)  // HTTP 503
                .body(response);
    }

    @GetMapping("/payroll")
    public ResponseEntity<Map<String, Object>> payrollServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Payroll Service is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "payroll-service");
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
```

---

## 🔄 2. Service-to-Service Fallback (Payroll → HR)

### **Location**: `payroll-service/src/main/java/com/company/payroll/service/PayrollService.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final HrServiceClient hrServiceClient;

    @CircuitBreaker(name = "hrService", fallbackMethod = "createPayrollFallback")
    //                                   ↑ If HR service fails, call this method
    public PayrollDTO createPayroll(PayrollDTO payrollDTO) {
        log.info("Creating payroll for employee ID: {}", payrollDTO.getEmployeeId());

        // Call HR Service via Feign Client
        EmployeeDTO employee = hrServiceClient.getEmployeeById(payrollDTO.getEmployeeId()).getData();
        //                     ↑ This can FAIL if HR Service is down

        // Create payroll with employee data
        Payroll payroll = Payroll.builder()
                .employeeId(payrollDTO.getEmployeeId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .basicSalary(employee.getSalary())
                // ... rest of payroll creation
                .build();

        return mapToDTO(payrollRepository.save(payroll));
    }

    // ⚠️ FALLBACK METHOD - Called when hrServiceClient fails
    public PayrollDTO createPayrollFallback(PayrollDTO payrollDTO, Exception ex) {
        log.error("HR Service is unavailable. Using fallback method.", ex);
        
        // Instead of failing completely, throw a user-friendly error
        throw new RuntimeException("HR Service is currently unavailable. Please try again later.");
    }
}
```

---

## 🔁 How Circuit Breaker + Fallback Works

### **Circuit Breaker States:**

```
┌─────────────────────────────────────────────────────────────┐
│                    CIRCUIT BREAKER FLOW                     │
└─────────────────────────────────────────────────────────────┘

State 1: CLOSED (Normal Operation)
┌──────────────┐
│   Request    │
│   to HR      │
│   Service    │
└──────┬───────┘
       │
       ▼
  ┌─────────┐
  │ Success │  ← Works fine
  └─────────┘

State 2: FAILURES START
┌──────────────┐
│   Request    │
└──────┬───────┘
       │
       ▼
  ┌─────────┐
  │  FAIL   │  ← HR Service down
  └─────────┘
       │
       ▼
  ┌─────────────────────┐
  │ Circuit Breaker     │
  │ Counts Failures     │
  │ 1/10, 2/10, 3/10... │
  └─────────────────────┘

State 3: CIRCUIT OPENS (50% failure threshold reached)
┌──────────────┐
│   Request    │
└──────┬───────┘
       │
       ▼
  ┌─────────────────────┐
  │ Circuit is OPEN!    │
  │ DON'T call service  │
  │ IMMEDIATELY call    │
  │ FALLBACK!          │
  └──────┬──────────────┘
         │
         ▼
  ┌──────────────────┐
  │ Fallback Method  │
  │ Return friendly  │
  │ error message    │
  └──────────────────┘

State 4: AFTER 10 SECONDS - Circuit goes to HALF_OPEN
┌──────────────┐
│   Request    │
└──────┬───────┘
       │
       ▼
  ┌─────────────────────┐
  │ Allow 3 test        │
  │ requests            │
  └──────┬──────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
SUCCESS?   FAIL?
    │         │
    ▼         ▼
 CLOSE    STAY OPEN
CIRCUIT   (More fallbacks)
```

---

## 🎬 Real-World Scenario Examples

### **Scenario 1: HR Service is Down (Gateway Level)**

```
1️⃣ User Request:
   GET http://localhost:8080/api/v1/employees/1

2️⃣ API Gateway tries to route to HR Service:
   GET http://hr-management-service:8081/api/v1/employees/1

3️⃣ HR Service is DOWN! ❌
   Connection Refused / Timeout

4️⃣ Circuit Breaker detects failure
   Failures: 5/10 (50%) → CIRCUIT OPENS!

5️⃣ Fallback is triggered automatically:
   forward:/fallback/hr

6️⃣ FallbackController.hrServiceFallback() is called

7️⃣ User receives friendly response:
   {
     "success": false,
     "message": "HR Management Service is currently unavailable. Please try again later.",
     "timestamp": "2024-01-01T10:00:00",
     "service": "hr-management-service"
   }
   HTTP Status: 503 Service Unavailable

✅ USER DOESN'T SEE UGLY ERROR!
✅ SYSTEM REMAINS STABLE!
```

---

### **Scenario 2: Payroll Service Can't Reach HR Service**

```
1️⃣ User creates payroll:
   POST http://localhost:8080/api/v1/payroll
   Body: { "employeeId": 1, ... }

2️⃣ API Gateway routes to Payroll Service:
   POST http://payroll-service:8082/api/v1/payroll

3️⃣ Payroll Service tries to fetch employee from HR Service:
   hrServiceClient.getEmployeeById(1)
   ↓
   GET http://hr-management-service:8081/api/v1/employees/1

4️⃣ HR Service is DOWN! ❌

5️⃣ @CircuitBreaker annotation detects failure

6️⃣ Circuit Breaker state:
   Failures: 5/10 (50%) → CIRCUIT OPENS!

7️⃣ Fallback method is called:
   createPayrollFallback(payrollDTO, exception)

8️⃣ Fallback method throws user-friendly error:
   throw new RuntimeException("HR Service is currently unavailable...")

9️⃣ User receives:
   {
     "success": false,
     "message": "HR Service is currently unavailable. Please try again later.",
     "timestamp": "2024-01-01T10:00:00"
   }

✅ PAYROLL SERVICE DOESN'T CRASH!
✅ USER GETS CLEAR ERROR MESSAGE!
```

---

## ⚙️ Circuit Breaker Configuration

### **Location**: `application.yml` (API Gateway & Services)

```yaml
resilience4j:
  circuitbreaker:
    instances:
      hrServiceCircuitBreaker:
        sliding-window-size: 10                    # Track last 10 requests
        failure-rate-threshold: 50                 # Open circuit at 50% failures
        wait-duration-in-open-state: 10000        # Wait 10 seconds before retry
        permitted-number-of-calls-in-half-open-state: 3  # Allow 3 test calls
        automatic-transition-from-open-to-half-open-enabled: true

  timelimiter:
    instances:
      hrServiceCircuitBreaker:
        timeout-duration: 5s                       # Max wait time for response
```

### **What This Means:**

| Setting | Value | Explanation |
|---------|-------|-------------|
| **sliding-window-size** | 10 | Track last 10 requests |
| **failure-rate-threshold** | 50% | If 5 out of 10 fail → Open circuit |
| **wait-duration-in-open-state** | 10 seconds | Wait before testing again |
| **permitted-calls-in-half-open** | 3 | Allow 3 test requests |
| **timeout-duration** | 5 seconds | Max time to wait for response |

---

## 📊 Fallback Flow Diagram

### **Without Fallback (BAD):**
```
User Request
    ↓
API Gateway
    ↓
Service Call FAILS ❌
    ↓
500 Internal Server Error
    ↓
User sees: "An error occurred"
    ↓
😞 Bad User Experience
```

### **With Fallback (GOOD):**
```
User Request
    ↓
API Gateway
    ↓
Service Call FAILS ❌
    ↓
Circuit Breaker Detects Failure
    ↓
Fallback Method Called
    ↓
503 Service Unavailable
    ↓
User sees: "Service temporarily unavailable. Please try again later."
    ↓
😊 Good User Experience
✅ System remains stable
```

---

## 🧪 Testing Fallback Mechanism

### **Test 1: Simulate HR Service Down**

```bash
# Stop HR Service
docker-compose stop hr-service

# Try to get employees via Gateway
curl http://localhost:8080/api/v1/employees/1

# Expected Response (Fallback):
{
  "success": false,
  "message": "HR Management Service is currently unavailable. Please try again later.",
  "timestamp": "2024-01-01T10:00:00",
  "service": "hr-management-service"
}
```

### **Test 2: Simulate Payroll → HR Communication Failure**

```bash
# Stop HR Service
docker-compose stop hr-service

# Try to create payroll (needs HR service)
curl -X POST http://localhost:8080/api/v1/payroll \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT" \
  -d '{
    "employeeId": 1,
    "payPeriodStart": "2024-01-01",
    "payPeriodEnd": "2024-01-31"
  }'

# Expected Response (Fallback):
{
  "success": false,
  "message": "HR Service is currently unavailable. Please try again later."
}
```

### **Test 3: Monitor Circuit Breaker States**

```bash
# Check Circuit Breaker health
curl http://localhost:8080/actuator/health

# Response shows circuit breaker status:
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "hrServiceCircuitBreaker": {
          "status": "OPEN",          ← Circuit is OPEN!
          "failureRate": "60.0%",
          "slowCallRate": "0.0%"
        }
      }
    }
  }
}
```

---

## 🎯 Benefits of Fallback Mechanism

### **1. Graceful Degradation**
```
Instead of: "500 Internal Server Error"
You get:    "Service temporarily unavailable"
```

### **2. System Stability**
```
❌ Without Fallback:
   One service down → Entire system down

✅ With Fallback:
   One service down → Other services continue working
```

### **3. Better User Experience**
```
User sees clear message instead of cryptic error
```

### **4. Prevents Cascade Failures**
```
Service A calls Service B
Service B is slow (5 seconds)
Without circuit breaker: Service A waits → Timeout → Resources exhausted
With circuit breaker: After 50% failures → Stop calling → Fallback → System stable
```

### **5. Automatic Recovery**
```
After 10 seconds → Circuit goes to HALF_OPEN
Allows 3 test requests
If successful → Circuit CLOSES → Normal operation resumes
```

---

## 📈 Monitoring Circuit Breaker

### **View Circuit Breaker Metrics**

```bash
# Gateway metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls

# Response:
{
  "name": "resilience4j.circuitbreaker.calls",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 100.0        ← Total calls
    }
  ],
  "availableTags": [
    {
      "tag": "kind",
      "values": ["successful", "failed", "not_permitted"]
    }
  ]
}
```

---

## 🔧 Customizing Fallback Responses

### **Example: Return Cached Data**

```java
@CircuitBreaker(name = "hrService", fallbackMethod = "getEmployeeFallback")
public EmployeeDTO getEmployee(Long id) {
    return hrServiceClient.getEmployeeById(id).getData();
}

public EmployeeDTO getEmployeeFallback(Long id, Exception ex) {
    log.warn("HR Service unavailable, returning cached data for employee: {}", id);
    
    // Return cached data from Redis
    return redisTemplate.opsForValue().get("employee:" + id);
}
```

### **Example: Return Default Values**

```java
public PayrollDTO createPayrollFallback(PayrollDTO payrollDTO, Exception ex) {
    log.error("Cannot create payroll, using default values", ex);
    
    // Create payroll with default salary
    Payroll payroll = Payroll.builder()
            .employeeId(payrollDTO.getEmployeeId())
            .employeeName("Unknown")  // Default value
            .basicSalary(BigDecimal.ZERO)  // Default value
            .status(Payroll.PayrollStatus.PENDING)
            .build();
    
    return mapToDTO(payrollRepository.save(payroll));
}
```

---

## 🎓 Summary

### **Fallback Locations in Our Project:**

1. ✅ **API Gateway** → Service unavailable fallbacks
    - `/fallback/hr` - HR Service fallback
    - `/fallback/payroll` - Payroll Service fallback

2. ✅ **Payroll Service** → HR Service call fallback
    - `createPayrollFallback()` - When HR Service is unreachable

### **When Fallback Triggers:**

- ⚡ Service is down/unreachable
- ⏱️ Request timeout (>5 seconds)
- 🔥 Circuit breaker opens (50% failure rate)
- 💥 Exception in service call

### **What Fallback Provides:**

- 🛡️ **Protection**: System doesn't crash
- 👤 **UX**: Users get friendly messages
- ⚖️ **Stability**: Prevents cascade failures
- 🔄 **Recovery**: Auto-recovery after cooldown

---

**Fallback = Safety Net for Your Microservices! 🎯**

When one service fails, the whole system doesn't fall apart!