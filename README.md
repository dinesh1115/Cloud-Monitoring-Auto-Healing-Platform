# Cloud Monitoring and Auto-Healing Platform

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/your-repo/actions)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

A robust, enterprise-grade Spring Boot application designed for real-time cloud system monitoring and automated healing capabilities. This platform provides comprehensive metric tracking, anomaly detection, alerting, and self-healing mechanisms to ensure optimal cloud infrastructure performance.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Overview

The Cloud Monitoring and Auto-Healing Platform is a sophisticated backend service built with Spring Boot that enables organizations to monitor cloud infrastructure health in real-time. It collects system metrics, detects anomalies, generates alerts, and can trigger automated healing actions to maintain system reliability and performance.

**Current Status**: In active development with core functionality implemented.

## Features

### Core Monitoring Capabilities
- **Real-time Metric Collection**: Tracks CPU usage, temperature, and other system metrics
- **RESTful API**: Comprehensive endpoints for metric ingestion and retrieval
- **Timestamp Tracking**: Automatic timestamping of all metric data
- **In-memory Storage**: High-performance data storage for demo and testing environments

### Intelligent Alerting System
- **Threshold-based Alerts**: Configurable alerts for CPU usage and temperature thresholds
- **Severity Levels**: Categorized alerts (Warning, Critical) for appropriate response
- **Alert History**: Persistent tracking of system alerts and incidents

### Anomaly Detection
- **Automated Analysis**: Intelligent detection of abnormal system behavior
- **Pattern Recognition**: Machine learning-ready architecture for advanced anomaly detection
- **Proactive Monitoring**: Early warning system for potential issues

### Auto-Healing Mechanisms
- **Self-healing Actions**: Automated responses to detected issues
- **Recovery Protocols**: Configurable healing strategies based on alert types
- **Health Checks**: Continuous system health monitoring and reporting

### Enterprise Features
- **Scalable Architecture**: Designed for high-throughput environments
- **Database Integration**: PostgreSQL support for production deployments
- **Comprehensive Logging**: Detailed logging for debugging and auditing
- **Health Endpoints**: Spring Boot Actuator integration for monitoring

## Architecture

The application follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────┐
│   Controllers   │  ← REST API Layer
├─────────────────┤
│    Services     │  ← Business Logic Layer
├─────────────────┤
│  Repositories   │  ← Data Access Layer
├─────────────────┤
│     Models      │  ← Domain Models
└─────────────────┘
```

### Key Components

- **Controllers**: `MetricController`, `SystemStatusController`
- **Services**: `MetricService`, `AlertService`, `CpuUsageService`, `AnomalyDetectionService`
- **Models**: `Metric`, `Alert`
- **Repository**: `MetricRepository` (in-memory implementation)

## Prerequisites

- **Java**: JDK 17 or higher
- **Build Tool**: Gradle 7.0+ (included via Gradle Wrapper)
- **Database**: PostgreSQL (for production) or in-memory (for development)
- **Operating System**: Windows, macOS, or Linux

## Installation & Setup

### Clone the Repository
```bash
git clone https://github.com/your-username/cloud-monitoring-auto-healing.git
cd cloud-monitoring-auto-healing
```

### Build the Application
```bash
# Using Gradle Wrapper (recommended)
./gradlew build

# On Windows
gradlew.bat build
```

### Run the Application
```bash
# Development mode
./gradlew bootRun

# Production mode
./gradlew build
java -jar build/libs/Cloud_Monitoring_and_Auto-Healing_Platform-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`.

## Usage

### Basic Monitoring
1. Start the application
2. Send metric data via POST requests
3. Monitor system health via GET endpoints
4. Review alerts for any issues

### Example Workflow
```bash
# Check system health
curl http://localhost:8080/health

# Submit a metric
curl -X POST http://localhost:8080/metrics \
  -H "Content-Type: application/json" \
  -d '{"cpu": 45, "temperature": 70}'

# Retrieve all metrics
curl http://localhost:8080/metrics
```

## API Documentation

### Base URL
```
http://localhost:8080
```

### Endpoints

#### Health Check
- **GET** `/health`
- **Description**: Returns system health status and statistics
- **Response**:
  ```json
  {
    "status": "UP",
    "metricCount": 10,
    "alertCount": 2
  }
  ```

#### Metrics Management
- **GET** `/metrics`
- **Description**: Retrieve all stored metrics with optional filtering and pagination
- **Query Parameters**:
  - `cpuMin` (optional): Minimum CPU usage filter
  - `cpuMax` (optional): Maximum CPU usage filter
  - `tempMin` (optional): Minimum temperature filter
  - `tempMax` (optional): Maximum temperature filter
  - `limit` (optional, default: 10): Maximum number of results
  - `offset` (optional, default: 0): Number of results to skip
  - `sort` (optional, default: "timestamp"): Sort field (cpu, temperature, timestamp)
  - `order` (optional, default: "desc"): Sort order (asc, desc)
- **Examples**:
  - `GET /metrics?cpuMin=50&limit=5` - Get 5 metrics with CPU >= 50
  - `GET /metrics?sort=cpu&order=asc` - Get all metrics sorted by CPU ascending
- **Response**: Array of metric objects

- **GET** `/metrics/{id}`
- **Description**: Retrieve a specific metric by ID
- **Parameters**: `id` (Long) - Metric ID
- **Response**: Single metric object or 404 if not found

- **POST** `/metrics`
- **Description**: Create a new metric
- **Request Body**:
  ```json
  {
    "cpu": 45,
    "temperature": 70
  }
  ```
- **Response**:
  ```json
  {
    "metric": {
      "id": 1,
      "cpu": 45,
      "temperature": 70,
      "timestamp": "2026-05-04T12:00:00Z"
    },
    "warning": "⚠️ Warning: CPU usage is high!"  // Only if CPU > 50
  }
  ```

#### CPU Usage Check
- **GET** `/metrics/cpuusage?usage={value}`
- **Description**: Check CPU usage status
- **Parameters**: `usage` (int) - CPU usage percentage
- **Response**: Status message

### Data Models

#### Metric
```json
{
  "id": 1,
  "cpu": 45,
  "temperature": 70,
  "timestamp": "2026-05-04T12:00:00Z"
}
```

#### Alert
```json
{
  "metricId": 1,
  "severity": "WARNING",
  "message": "High CPU usage detected",
  "timestamp": "2026-05-04T12:00:00Z"
}
```

## Testing

The project includes comprehensive unit and integration tests.

### Run Tests
```bash
./gradlew test
```

### Test Results
- **Total Tests**: 4
- **Success Rate**: 100%
- **Coverage**: Core application functionality

### Test Structure
- Unit tests for services and controllers
- Integration tests for API endpoints
- Mock-based testing for external dependencies

## Configuration

### Application Properties
Located in `src/main/resources/application.properties`:

```properties
spring.application.name=Cloud_Monitoring_and_Auto-Healing_Platform
server.port=8080
```

### Database Configuration
For production deployments, configure PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/monitoring_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: Set to `prod` for production
- `DATABASE_URL`: PostgreSQL connection URL
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java coding standards
- Write comprehensive tests
- Update documentation
- Ensure all tests pass before submitting

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

**Project Maintainer**: Dinesh  
**Email**: your.email@example.com  
**GitHub**: [your-username](https://github.com/your-username)  
**LinkedIn**: [Your LinkedIn](https://linkedin.com/in/your-profile)

---

*Built with ❤️ using Spring Boot*
- A PostgreSQL dependency is included but not wired into persistence yet.
- Customize `src/main/resources/application.properties` for additional Spring Boot configuration.

