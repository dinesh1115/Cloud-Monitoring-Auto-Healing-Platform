# Cloud Monitoring and Auto-Healing Platform -- IN progress

A simple Spring Boot application that tracks runtime metrics for cloud systems and exposes REST endpoints for metric ingestion and retrieval.

## Features

- Exposes `/metrics` REST endpoints
- Stores metric objects in-memory for demo purposes
- Returns metrics with timestamp, CPU, and temperature values

## API Endpoints

- `GET /metrics` - list all metrics
- `GET /metrics/{id}` - fetch a metric by ID
- `POST /metrics` - create a new metric

Example request body for POST:

```json
{
  "cpu": 400,
  "temperature": 75
}
```
{
    "warning": "⚠️ Warning: CPU usage is high!",
    "metric": {
        "id": 4,
        "cpu": 400,
        "temperature": 70,
        "timestamp": "2026-04-19T09:31:35.312679500Z"
    }
}

## Build and Run

```powershell
./gradlew bootRun
```

or build the JAR and run:

```powershell
./gradlew build
java -jar build/libs/Cloud_Monitoring_and_Auto-Healing_Platform-0.0.1-SNAPSHOT.jar
```

## Notes

- This project currently uses an in-memory repository implementation.
- A PostgreSQL dependency is included but not wired into persistence yet.
- Customize `src/main/resources/application.properties` for additional Spring Boot configuration.

