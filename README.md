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