package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition to enable Redis configuration only when app.redis.enabled=true
 */
public class RedisEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String redisEnabled = context.getEnvironment().getProperty("app.redis.enabled", "false");
        return Boolean.parseBoolean(redisEnabled);
    }
}