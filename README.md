# Cloud Monitoring and Auto-Healing Platform

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
  "cpu": 65,
  "temperature": 75
}
```

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

