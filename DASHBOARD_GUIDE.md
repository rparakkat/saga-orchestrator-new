# Real-Time Saga Dashboard Guide

## Overview

The Saga Orchestrator includes a comprehensive real-time dashboard that provides live monitoring and visualization of saga workflow execution. The dashboard uses WebSocket technology for real-time updates and provides both REST API endpoints and a modern web interface.

## Features

### 🎯 **Real-Time Monitoring**
- **Live Updates**: WebSocket-based real-time data streaming
- **Event Streaming**: Instant saga event notifications
- **Metrics Dashboard**: Real-time performance metrics
- **Status Tracking**: Live saga and step status monitoring

### 📊 **Visualization Components**
- **Metric Cards**: Key performance indicators
- **Charts**: Interactive charts for trend analysis
- **Event Feed**: Real-time event stream
- **Status Indicators**: System health monitoring

### 🔧 **Interactive Features**
- **Auto-refresh**: Automatic data updates every 5 seconds
- **Responsive Design**: Mobile-friendly interface
- **Error Handling**: Graceful error recovery
- **Connection Management**: Automatic WebSocket reconnection

## Architecture

### Backend Components

#### 1. **WebSocket Handler** (`SagaWebSocketHandler`)
```java
@Component
public class SagaWebSocketHandler extends TextWebSocketHandler {
    // Manages WebSocket connections
    // Broadcasts saga events and metrics
    // Handles client commands
}
```

#### 2. **Dashboard Controller** (`DashboardController`)
```java
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    // REST endpoints for dashboard data
    // Metrics and statistics endpoints
    // System health monitoring
}
```

#### 3. **Dashboard Scheduler** (`DashboardSchedulerService`)
```java
@Service
public class DashboardSchedulerService {
    // Periodic metrics broadcasting
    // Health check updates
    // Data cleanup tasks
}
```

#### 4. **Event Publisher Integration**
```java
@Service
public class SagaEventPublisher {
    // Publishes events to WebSocket clients
    // Integrates with existing event system
    // Real-time event broadcasting
}
```

### Frontend Components

#### 1. **WebSocket Client**
```javascript
// Real-time connection management
const socket = new WebSocket('ws://localhost:8080/ws/saga-dashboard');
socket.onmessage = function(event) {
    const message = JSON.parse(event.data);
    handleWebSocketMessage(message);
};
```

#### 2. **Chart.js Integration**
```javascript
// Interactive charts for data visualization
const chart = new Chart(ctx, {
    type: 'doughnut',
    data: { /* chart data */ },
    options: { /* chart options */ }
});
```

#### 3. **Responsive UI**
```css
/* Modern, responsive design */
.metrics-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
}
```

## API Endpoints

### WebSocket Endpoint
```
WebSocket: ws://localhost:8080/ws/saga-dashboard
SockJS: http://localhost:8080/ws/saga-dashboard
```

### REST API Endpoints

#### Dashboard Overview
```bash
GET /api/v1/dashboard/overview
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response:**
```json
{
  "totalSagas": 1250,
  "successfulSagas": 1180,
  "failedSagas": 45,
  "compensatedSagas": 15,
  "timedOutSagas": 10,
  "sagaSuccessRate": 94.4,
  "stepSuccessRate": 96.8,
  "activeSagas": 25,
  "pendingSagas": 5,
  "totalRequests": 5000,
  "rateLimitExceededRate": 2.1,
  "uptimeMs": 86400000,
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Recent Sagas
```bash
GET /api/v1/dashboard/recent-sagas?page=0&size=20
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

#### Sagas by Status
```bash
GET /api/v1/dashboard/sagas-by-status
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response:**
```json
{
  "running": 25,
  "completed": 1180,
  "failed": 45,
  "compensated": 15,
  "timeout": 10
}
```

#### Step Metrics
```bash
GET /api/v1/dashboard/step-metrics
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

#### Circuit Breaker Metrics
```bash
GET /api/v1/dashboard/circuit-breaker-metrics
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

#### Performance Metrics
```bash
GET /api/v1/dashboard/performance
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

#### System Health
```bash
GET /api/v1/dashboard/health
```

**Response:**
```json
{
  "status": "HEALTHY",
  "message": "System is operating normally",
  "timestamp": "2024-01-15T10:30:00",
  "metrics": { /* full metrics object */ }
}
```

## WebSocket Message Types

### From Server to Client

#### 1. **Initial Metrics** (`INITIAL_METRICS`)
```json
{
  "type": "INITIAL_METRICS",
  "data": { /* metrics snapshot */ },
  "timestamp": 1705312200000
}
```

#### 2. **Metrics Update** (`METRICS_UPDATE`)
```json
{
  "type": "METRICS_UPDATE",
  "data": { /* updated metrics */ },
  "timestamp": 1705312205000
}
```

#### 3. **Saga Event** (`SAGA_EVENT`)
```json
{
  "type": "SAGA_EVENT",
  "data": {
    "eventType": "SAGA_STARTED",
    "sagaId": "saga-123",
    "timestamp": "2024-01-15T10:30:00",
    "saga": { /* saga details */ }
  },
  "timestamp": 1705312200000
}
```

### From Client to Server

#### 1. **Subscribe to Metrics** (`SUBSCRIBE_METRICS`)
```json
{
  "type": "SUBSCRIBE_METRICS"
}
```

#### 2. **Unsubscribe from Metrics** (`UNSUBSCRIBE_METRICS`)
```json
{
  "type": "UNSUBSCRIBE_METRICS"
}
```

#### 3. **Request Saga Details** (`REQUEST_SAGA_DETAILS`)
```json
{
  "type": "REQUEST_SAGA_DETAILS",
  "sagaId": "saga-123"
}
```

## Dashboard Features

### 1. **Real-Time Metrics Cards**
- **Total Sagas**: Number of executed sagas
- **Success Rate**: Percentage of successful sagas
- **Active Sagas**: Currently running sagas
- **Failed Sagas**: Failed sagas requiring attention
- **Total Steps**: Number of executed steps
- **Step Success Rate**: Percentage of successful steps

### 2. **Interactive Charts**
- **Saga Status Distribution**: Doughnut chart showing saga status breakdown
- **Performance Trends**: Line chart showing success rate over time

### 3. **Event Feed**
- **Real-time Events**: Live stream of saga events
- **Event Categorization**: Color-coded events by type
- **Timestamp Tracking**: Event timing information

### 4. **System Health Monitoring**
- **Status Indicator**: Visual health status with color coding
- **Health Checks**: Automatic system health monitoring
- **Error Alerts**: Real-time error notifications

## Configuration

### WebSocket Configuration
```yaml
# application.yml
spring:
  websocket:
    allowed-origins: "*"  # Configure for production
```

### Dashboard Update Intervals
```java
@Scheduled(fixedRate = 5000)    // Metrics updates every 5 seconds
@Scheduled(fixedRate = 30000)   // Health checks every 30 seconds
@Scheduled(fixedRate = 3600000) // Cleanup every hour
```

### Security Configuration
```java
@PreAuthorize("hasRole('ADMIN')")  // Admin-only access
```

## Usage Guide

### 1. **Accessing the Dashboard**
```bash
# Open in browser
http://localhost:8080/dashboard

# Or access directly
http://localhost:8080/dashboard.html
```

### 2. **Authentication**
- **Username**: admin
- **Password**: admin123
- **Basic Auth**: YWRtaW46YWRtaW4xMjM=

### 3. **Real-Time Monitoring**
- Dashboard automatically connects to WebSocket
- Real-time updates every 5 seconds
- Automatic reconnection on connection loss

### 4. **Interacting with Charts**
- Hover over chart elements for details
- Click legend items to show/hide data series
- Responsive design adapts to screen size

### 5. **Event Monitoring**
- Events appear in real-time as they occur
- Color coding: Green (success), Red (error), Orange (warning)
- Scrollable event list with latest events at top

## Troubleshooting

### Common Issues

#### 1. **WebSocket Connection Failed**
```javascript
// Check browser console for errors
// Verify WebSocket endpoint is accessible
// Check firewall/proxy settings
```

#### 2. **No Real-Time Updates**
```javascript
// Verify WebSocket connection status
// Check for JavaScript errors
// Ensure metrics service is running
```

#### 3. **Authentication Issues**
```bash
# Verify credentials
# Check security configuration
# Ensure proper authorization headers
```

#### 4. **Performance Issues**
```yaml
# Adjust update intervals
saga:
  dashboard:
    update-interval: 10000  # Increase to 10 seconds
    max-events: 100         # Limit event history
```

### Debug Mode
```javascript
// Enable debug logging
localStorage.setItem('dashboard-debug', 'true');
// Refresh page to see detailed logs
```

## Performance Considerations

### 1. **WebSocket Management**
- Automatic connection pooling
- Graceful reconnection handling
- Connection cleanup on page unload

### 2. **Data Optimization**
- Efficient JSON serialization
- Minimal data transfer
- Client-side caching

### 3. **UI Performance**
- Efficient DOM updates
- Debounced chart updates
- Lazy loading for large datasets

### 4. **Memory Management**
- Limited event history (50 events)
- Automatic cleanup of old data
- Efficient chart memory usage

## Security Considerations

### 1. **Authentication**
- Admin-only access to dashboard
- Secure WebSocket connections
- Proper authorization checks

### 2. **Data Protection**
- Sensitive data filtering
- Secure event transmission
- Audit logging

### 3. **Production Deployment**
```yaml
# Configure for production
spring:
  websocket:
    allowed-origins: "https://yourdomain.com"
  security:
    user:
      name: ${DASHBOARD_USERNAME}
      password: ${DASHBOARD_PASSWORD}
```

## Future Enhancements

### 1. **Advanced Features**
- Custom dashboard layouts
- User-defined alerts
- Export functionality
- Historical data analysis

### 2. **Integration**
- Grafana integration
- Prometheus metrics
- External monitoring systems
- Slack/Teams notifications

### 3. **Analytics**
- Trend analysis
- Predictive analytics
- Performance optimization suggestions
- Capacity planning tools

The real-time dashboard provides comprehensive monitoring capabilities for the Saga Orchestrator, enabling operators to track workflow execution, identify issues, and optimize performance in real-time. 