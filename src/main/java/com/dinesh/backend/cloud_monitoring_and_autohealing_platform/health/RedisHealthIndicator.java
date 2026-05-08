package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Redis connectivity
 * Integrates with Spring Boot Actuator health endpoints
 */
@Component("redisHealth")
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            // Test Redis connection with a simple operation
            redisTemplate.getConnectionFactory().getConnection().ping();
            
            return Health.up()
                    .withDetail("status", "Redis connection successful")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Failed to connect to Redis")
                    .withException(e)
                    .build();
        }
    }
}
