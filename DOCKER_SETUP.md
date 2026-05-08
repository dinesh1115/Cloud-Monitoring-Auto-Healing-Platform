# Docker Setup & Deployment Guide

## Overview
This guide explains how to containerize and run the Cloud Monitoring & Auto-Healing Platform using Docker.

## Prerequisites
- Docker 20.10+
- Docker Compose 1.29+
- 2GB RAM minimum
- 10GB disk space

## Quick Start

### 1. Build and Run with Docker Compose (Recommended)

```bash
# Build the Docker image
docker-compose build

# Start all services (app + Redis + PostgreSQL)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down
```

### 2. Access the Application

- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/health
- **Actuator**: http://localhost:8080/actuator
- **Redis CLI**: `docker exec -it cloud-monitoring-redis redis-cli -a redis-secure-password`
- **PostgreSQL**: 
  - Host: localhost:5432
  - User: monitoring_user
  - Password: secure_password_123
  - Database: cloud_monitoring_db

## Building Standalone Docker Image

### Build the image
```bash
docker build -t cloud-monitoring-app:latest .
```

### Run the container
```bash
docker run -d \
  --name cloud-monitoring-app \
  -p 8080:8080 \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_REDIS_PORT=6379 \
  cloud-monitoring-app:latest
```

## Docker Compose Services

### app
- **Purpose**: Spring Boot application
- **Port**: 8080
- **Dependencies**: redis, postgres
- **Healthcheck**: HTTP GET /health (30s interval)

### redis
- **Purpose**: Event handling, caching, pub/sub
- **Port**: 6379
- **Auth**: `redis-secure-password`
- **Persistence**: Enabled (AOF)
- **Volumes**: `redis-data` (persistent storage)

### postgres
- **Purpose**: Persistent data storage
- **Port**: 5432
- **User**: monitoring_user
- **Password**: secure_password_123
- **Database**: cloud_monitoring_db
- **Volumes**: `postgres-data` (persistent storage)

## Environment Variables

### For the app service:
```
SPRING_REDIS_HOST=redis          # Redis hostname
SPRING_REDIS_PORT=6379           # Redis port
SERVER_PORT=8080                 # Application port
JAVA_OPTS=-Xmx512m -Xms256m     # JVM memory settings
```

### Override defaults:
```bash
docker-compose up -d \
  -e SPRING_REDIS_PASSWORD=your-password \
  -e SERVER_PORT=9090
```

## Useful Docker Commands

### Container Management
```bash
# View running containers
docker-compose ps

# Stop all containers
docker-compose stop

# Stop and remove containers
docker-compose down

# Remove all volumes (WARNING: deletes data!)
docker-compose down -v

# Restart a specific service
docker-compose restart app
```

### Logs & Debugging
```bash
# View app logs
docker-compose logs app

# Follow logs in real-time
docker-compose logs -f app

# View logs from all services
docker-compose logs -f

# Get shell access to container
docker exec -it cloud-monitoring-app /bin/sh
```

### Redis Operations
```bash
# Connect to Redis CLI
docker exec -it cloud-monitoring-redis redis-cli -a redis-secure-password

# Check Redis info
docker exec cloud-monitoring-redis redis-cli -a redis-secure-password INFO

# Monitor Redis commands
docker exec -it cloud-monitoring-redis redis-cli -a redis-secure-password MONITOR
```

### Database Operations
```bash
# Connect to PostgreSQL
docker exec -it cloud-monitoring-db psql -U monitoring_user -d cloud_monitoring_db

# Backup database
docker exec cloud-monitoring-db pg_dump -U monitoring_user cloud_monitoring_db > backup.sql

# Restore database
docker exec -i cloud-monitoring-db psql -U monitoring_user cloud_monitoring_db < backup.sql
```

## Production Deployment

### Best Practices

1. **Security**
   - Change default passwords in docker-compose.yml
   - Use Docker secrets for sensitive data
   - Use a reverse proxy (Nginx)
   - Enable TLS/SSL

2. **Resource Limits**
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '2'
         memory: 2G
       reservations:
         cpus: '1'
         memory: 1G
   ```

3. **Persistence**
   - Use named volumes for Redis and PostgreSQL
   - Enable PostgreSQL WAL backup
   - Schedule regular database backups

4. **Monitoring**
   - Monitor container metrics
   - Set up log aggregation (ELK stack)
   - Configure alerts for service failures

## Troubleshooting

### Container exits immediately
```bash
# Check logs
docker-compose logs app

# Ensure port 8080 is not in use
lsof -i :8080
```

### Redis connection refused
```bash
# Verify Redis is running
docker-compose ps redis

# Check Redis logs
docker-compose logs redis

# Test connection
docker exec cloud-monitoring-redis redis-cli -a redis-secure-password PING
```

### Database connection issues
```bash
# Verify PostgreSQL is running
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Test connection
docker exec cloud-monitoring-db psql -U monitoring_user -d cloud_monitoring_db -c "SELECT 1"
```

### Out of memory
```bash
# Increase Docker memory limit (in Docker Desktop settings)
# Or modify docker-compose.yml JAVA_OPTS:
JAVA_OPTS=-Xmx2g -Xms1g
```

## Multi-Stage Build Explanation

The Dockerfile uses a multi-stage build strategy:
1. **Stage 1 (builder)**: Compiles the application, reducing final image size
2. **Stage 2 (runtime)**: Contains only the JRE and compiled JAR
3. **Non-root user**: Runs the app as `appuser` for security
4. **Health checks**: Continuous monitoring of application health

## Next Steps

1. Integrate CI/CD pipeline (GitHub Actions)
2. Push image to Docker registry (Docker Hub, ECR)
3. Deploy to Kubernetes (if scaling needed)
4. Set up monitoring with Prometheus + Grafana
5. Configure ELK stack for centralized logging
