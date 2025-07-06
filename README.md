# Saga Orchestrator

A production-ready task orchestration system using the Saga pattern for distributed transactions. This system provides a robust framework for managing complex workflows across multiple microservices with automatic compensation (rollback) capabilities.

## Features

### Core Features
- **Saga Pattern Implementation**: Complete implementation of the Saga pattern for distributed transactions
- **MongoDB Persistence**: Production-ready persistence layer with MongoDB
- **RESTful API**: Comprehensive REST API for saga management
- **Asynchronous Execution**: Non-blocking saga execution with configurable thread pools
- **Error Handling & Retry**: Sophisticated error handling with configurable retry policies
- **Compensation (Rollback)**: Automatic compensation of completed steps when failures occur
- **Monitoring & Observability**: Built-in metrics, health checks, and logging

### Step Types Supported
- **HTTP Calls**: REST API calls to external microservices
- **Database Operations**: SQL/NoSQL database operations
- **Business Logic**: Custom Java business logic execution
- **Message Queue**: Message queue operations (planned)
- **File Operations**: File system operations (planned)
- **Wait/Delay**: Configurable delays between steps
- **Conditional**: Conditional step execution (planned)
- **Parallel**: Parallel step execution (planned)
- **Sub-Sagas**: Nested saga execution (planned)

### Production Features
- **Security**: Basic authentication and authorization
- **Circuit Breaker**: Resilience4j integration for fault tolerance
- **Metrics**: Prometheus metrics integration
- **Health Checks**: Spring Boot Actuator health endpoints
- **Logging**: Structured logging with configurable levels
- **Configuration**: Environment-based configuration
- **Testing**: Comprehensive test coverage with TestContainers
- **Exception Handling**: Custom exception hierarchy with global error handling
- **Design Patterns**: Multiple design patterns for maintainability and scalability

## Architecture

### Domain Model
```
Saga
├── SagaStep (multiple)
│   ├── StepConfig
│   └── CompensationConfig
├── SagaStatus
└── Metadata
```

### Design Patterns Implemented

#### 1. **Strategy Pattern** - Step Execution
- `StepExecutor` interface with different implementations
- `HttpStepExecutor`, `DatabaseStepExecutor`, `BusinessLogicStepExecutor`
- Easy to add new step types without modifying existing code

#### 2. **Factory Pattern** - Step Executor Creation
- `StepExecutorFactory` for creating appropriate executors
- Automatic discovery of step executors via Spring dependency injection
- Type-safe executor selection

#### 3. **Template Method Pattern** - Saga Execution
- `SagaExecutionTemplate` defines the execution algorithm
- Hooks for customization at different execution points
- Consistent execution flow with extensibility

#### 4. **Observer Pattern** - Event Handling
- `SagaEvent` base class with specific event types
- `SagaEventPublisher` for publishing events
- Decoupled event handling for monitoring and integration

#### 5. **Chain of Responsibility** - Error Handling
- `ErrorHandler` chain for step failure handling
- `RetryErrorHandler`, `CompensationErrorHandler`, `ContinueErrorHandler`
- Flexible error handling strategy

#### 6. **Command Pattern** - Saga Operations
- `SagaCommand` interface for saga operations
- `ExecuteSagaCommand` for saga execution
- Support for undo operations and command history

#### 7. **Builder Pattern** - Complex Object Creation
- `SagaBuilder` for creating complex saga configurations
- Fluent API for building sagas with multiple steps
- Immutable configuration objects

### Service Layer
- **SagaOrchestratorService**: Core orchestration logic using Command pattern
- **SagaExecutionTemplate**: Template method for execution flow
- **StepExecutorFactory**: Factory for step executors
- **CompensationService**: Compensation (rollback) logic
- **Error Handlers**: Chain of responsibility for error handling

### Infrastructure Layer
- **SagaRepository**: MongoDB persistence with custom queries
- **SagaController**: REST API endpoints with global exception handling
- **GlobalExceptionHandler**: Centralized exception handling
- **EventPublisher**: Event publishing for monitoring

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB 4.4+

### Running the Application

1. **Start MongoDB**:
   ```bash
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   ```

2. **Build the application**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the API**:
   - Base URL: `http://localhost:8080`
   - API Documentation: `http://localhost:8080/api/v1/sagas`
   - Health Check: `http://localhost:8080/actuator/health`
   - Metrics: `http://localhost:8080/actuator/metrics`

### Configuration

The application can be configured using environment variables or `application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/saga_orchestrator
  
  security:
    user:
      name: admin
      password: admin123

saga:
  execution:
    max-retries: 3
    retry-delay-ms: 1000
    timeout-ms: 30000
```

## API Usage

### Creating a Saga

```bash
curl -X POST http://localhost:8080/api/v1/sagas \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{
    "name": "order-processing-saga",
    "correlationId": "order-123",
    "steps": [
      {
        "stepId": "validate-order",
        "name": "Validate Order",
        "order": 0,
        "type": "HTTP_CALL",
        "required": true,
        "config": {
          "url": "http://order-service/api/orders/validate",
          "httpMethod": "POST",
          "timeoutMs": 5000,
          "maxRetries": 3
        },
        "compensationConfig": {
          "type": "HTTP_CALL",
          "url": "http://order-service/api/orders/cancel",
          "httpMethod": "POST",
          "required": true
        }
      },
      {
        "stepId": "reserve-inventory",
        "name": "Reserve Inventory",
        "order": 1,
        "type": "HTTP_CALL",
        "required": true,
        "config": {
          "url": "http://inventory-service/api/inventory/reserve",
          "httpMethod": "POST",
          "timeoutMs": 5000,
          "maxRetries": 3
        },
        "compensationConfig": {
          "type": "HTTP_CALL",
          "url": "http://inventory-service/api/inventory/release",
          "httpMethod": "POST",
          "required": true
        }
      },
      {
        "stepId": "process-payment",
        "name": "Process Payment",
        "order": 2,
        "type": "HTTP_CALL",
        "required": true,
        "config": {
          "url": "http://payment-service/api/payments/process",
          "httpMethod": "POST",
          "timeoutMs": 10000,
          "maxRetries": 3
        },
        "compensationConfig": {
          "type": "HTTP_CALL",
          "url": "http://payment-service/api/payments/refund",
          "httpMethod": "POST",
          "required": true
        }
      }
    ],
    "inputData": {
      "orderId": "order-123",
      "customerId": "customer-456",
      "items": [
        {"productId": "prod-1", "quantity": 2},
        {"productId": "prod-2", "quantity": 1}
      ],
      "totalAmount": 150.00
    }
  }'
```

### Getting Saga Status

```bash
curl -X GET http://localhost:8080/api/v1/sagas/{sagaId} \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### Retrying a Failed Saga

```bash
curl -X POST http://localhost:8080/api/v1/sagas/{sagaId}/retry \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### Compensating a Saga

```bash
curl -X POST http://localhost:8080/api/v1/sagas/{sagaId}/compensate \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

## Example Use Cases

### 1. E-commerce Order Processing
- Validate order
- Reserve inventory
- Process payment
- Send confirmation
- Update inventory

### 2. Banking Transaction
- Validate account
- Check balance
- Transfer funds
- Update ledgers
- Send notifications

### 3. Travel Booking
- Search availability
- Reserve flights
- Reserve hotels
- Process payment
- Send confirmations

## Monitoring and Observability

### Health Checks
- Application health: `/actuator/health`
- MongoDB health: `/actuator/health/mongo`

### Metrics
- Saga execution metrics: `/actuator/metrics/saga.executions`
- Step execution metrics: `/actuator/metrics/saga.steps`
- Compensation metrics: `/actuator/metrics/saga.compensations`

### Logging
The application uses structured logging with different levels:
- `INFO`: General saga operations
- `DEBUG`: Detailed step execution
- `WARN`: Retry attempts and non-critical issues
- `ERROR`: Failures and compensation events

## Development

### Project Structure
```
src/main/java/com/saga/
├── domain/
│   └── model/          # Domain entities
├── application/
│   └── service/        # Business logic services
├── infrastructure/
│   ├── repository/     # Data access layer
│   ├── controller/     # REST controllers
│   ├── config/         # Configuration classes
│   └── dto/           # Data transfer objects
```

### Adding New Step Types

1. **Create Step Executor**:
   ```java
   @Service
   public class CustomStepExecutor {
       public StepExecutionResult execute(SagaStep step, Map<String, Object> input) {
           // Implementation
       }
   }
   ```

2. **Update StepExecutorService**:
   ```java
   case CUSTOM_TYPE -> customStepExecutor.execute(step, sagaInputData);
   ```

3. **Add to StepType enum**:
   ```java
   CUSTOM_TYPE
   ```

### Testing

Run tests with:
```bash
mvn test
```

The project includes:
- Unit tests for services
- Integration tests with TestContainers
- API tests for controllers

## Production Deployment

### Docker
```dockerfile
FROM openjdk:17-jre-slim
COPY target/saga-orchestrator-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: saga-orchestrator
spec:
  replicas: 3
  selector:
    matchLabels:
      app: saga-orchestrator
  template:
    metadata:
      labels:
        app: saga-orchestrator
    spec:
      containers:
      - name: saga-orchestrator
        image: saga-orchestrator:latest
        ports:
        - containerPort: 8080
        env:
        - name: MONGODB_URI
          value: "mongodb://mongodb:27017/saga_orchestrator"
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Scalability Features

The project includes comprehensive scalability features to handle high-throughput, distributed environments:

### 1. Caching Layer
- **Implementation**: `SagaCache` with Spring Cache abstraction
- **Features**: 
  - Saga caching by ID, correlation ID, and status
  - Configurable cache eviction policies
  - Caffeine cache implementation for high performance
- **Benefits**: Reduced database load and improved response times

### 2. Connection Pooling
- **Implementation**: `ConnectionPoolConfig` with MongoDB connection pool
- **Features**:
  - Configurable pool sizes (min/max connections)
  - Connection lifecycle management
  - Timeout and heartbeat configurations
- **Benefits**: Efficient resource utilization and connection reuse

### 3. Thread Pool Management
- **Implementation**: `AsyncConfig` with multiple dedicated thread pools
- **Features**:
  - Separate pools for saga execution, step execution, and compensation
  - Configurable pool sizes and queue capacities
  - Graceful shutdown handling
- **Benefits**: Better resource isolation and performance tuning

### 4. Bulk Operations
- **Implementation**: `BulkOperationsService` for database operations
- **Features**:
  - Bulk status updates
  - Bulk retry operations
  - Bulk cleanup operations
  - Asynchronous execution with CompletableFuture
- **Benefits**: Improved database performance for large-scale operations

### 5. Rate Limiting
- **Implementation**: `RateLimitingService` with multi-level rate limiting
- **Features**:
  - Per-minute, per-hour, and burst rate limits
  - Client-specific rate limiting
  - Configurable limits and time windows
- **Benefits**: API protection and fair resource distribution

### 6. Circuit Breaker Pattern
- **Implementation**: `CircuitBreakerService` for external service calls
- **Features**:
  - Automatic failure detection and circuit opening
  - Configurable failure thresholds and timeouts
  - Half-open state for gradual recovery
- **Benefits**: Improved system resilience and failure isolation

### 7. Monitoring and Metrics
- **Implementation**: `MetricsService` with comprehensive metrics collection
- **Features**:
  - Saga and step execution metrics
  - Performance tracking and success rates
  - Circuit breaker and rate limiting metrics
  - System health monitoring
- **Benefits**: Better observability and performance optimization

### 8. Scalability Configuration
- **Thread Pools**: Configurable core/max pool sizes and queue capacities
- **Database**: Connection pool settings and timeout configurations
- **Caching**: Cache sizes and eviction policies
- **Rate Limiting**: Request limits and burst sizes
- **Circuit Breaker**: Failure thresholds and timeout settings

### 9. REST API for Scalability Management
- **Implementation**: `ScalabilityController` with admin endpoints
- **Features**:
  - Bulk operations management
  - Metrics and health monitoring
  - Circuit breaker and rate limit management
  - System configuration endpoints
- **Benefits**: Operational control and monitoring capabilities

### Scalability API Endpoints

#### System Metrics
```bash
# Get comprehensive system metrics
curl -X GET http://localhost:8080/api/v1/scalability/metrics \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

#### Bulk Operations
```bash
# Bulk update saga statuses
curl -X POST "http://localhost:8080/api/v1/scalability/bulk/status?sagaIds=saga1,saga2&newStatus=COMPLETED" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="

# Bulk retry failed sagas
curl -X POST http://localhost:8080/api/v1/scalability/bulk/retry \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="

# Bulk timeout long-running sagas
curl -X POST "http://localhost:8080/api/v1/scalability/bulk/timeout?timeoutThreshold=2024-01-01T12:00:00" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

#### Circuit Breaker Management
```bash
# Get circuit breaker status
curl -X GET http://localhost:8080/api/v1/scalability/circuit-breaker/payment-service \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="

# Reset circuit breaker
curl -X POST http://localhost:8080/api/v1/scalability/circuit-breaker/payment-service/reset \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

#### Rate Limiting Management
```bash
# Get rate limit status
curl -X GET http://localhost:8080/api/v1/scalability/rate-limit/client-123 \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="

# Reset rate limit
curl -X POST http://localhost:8080/api/v1/scalability/rate-limit/client-123/reset \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### Performance Tuning

#### Thread Pool Configuration
```yaml
saga:
  execution:
    thread-pool:
      core-size: 20          # Core thread pool size
      max-size: 100          # Maximum thread pool size
      queue-capacity: 500    # Queue capacity for pending tasks
      keep-alive-seconds: 60 # Thread keep-alive time
```

#### Database Connection Pool
```yaml
saga:
  mongodb:
    connection-pool:
      max-size: 100                    # Maximum connections
      min-size: 5                      # Minimum connections
      max-wait-time: 30000             # Max wait time for connection
      max-connection-life-time: 300000 # Connection lifetime
      max-connection-idle-time: 60000  # Idle connection timeout
```

#### Caching Configuration
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=300s
```

#### Rate Limiting Configuration
```yaml
saga:
  rate-limit:
    requests-per-minute: 100  # Requests per minute per client
    requests-per-hour: 1000   # Requests per hour per client
    burst-size: 50           # Burst size for short-term spikes
```

#### Circuit Breaker Configuration
```yaml
saga:
  circuit-breaker:
    failure-threshold: 5     # Failures before opening circuit
    timeout-seconds: 30      # Time to wait before half-open
    success-threshold: 3     # Successes before closing circuit
```

### Horizontal Scaling

The application is designed for horizontal scaling with:

1. **Stateless Design**: No in-memory state, all data in MongoDB
2. **Connection Pooling**: Efficient database connection management
3. **Caching**: Distributed cache support (Redis can be configured)
4. **Load Balancing**: Multiple instances can be deployed behind a load balancer
5. **Health Checks**: Kubernetes-ready health endpoints
6. **Metrics**: Prometheus metrics for monitoring and auto-scaling

### Monitoring and Alerting

#### Key Metrics to Monitor
- Saga execution rate and success rate
- Step execution performance
- Database connection pool utilization
- Thread pool queue sizes
- Circuit breaker trip rates
- Rate limiting effectiveness
- Cache hit rates

#### Recommended Alerts
- Saga failure rate > 5%
- Step execution time > 30 seconds
- Database connection pool > 80% utilization
- Thread pool queue > 80% capacity
- Circuit breaker open for > 5 minutes
- High rate limiting rejections 