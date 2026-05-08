# Multi-stage Dockerfile for Cloud Monitoring & Auto-Healing Platform

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace
COPY . .

# Make gradlew executable and build the application
RUN chmod +x ./gradlew && \
    ./gradlew clean build -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
