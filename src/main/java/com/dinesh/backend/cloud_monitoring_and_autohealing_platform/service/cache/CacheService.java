package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for caching operations using Redis
 * Provides TTL-based caching for metrics and other data
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    // Cache key prefixes
    public static final String METRIC_CACHE_PREFIX = "cache:metric:";
    public static final String ALERT_CACHE_PREFIX = "cache:alert:";
    public static final String ANOMALY_CACHE_PREFIX = "cache:anomaly:";

    // Default cache TTL in minutes
    private static final long DEFAULT_TTL_MINUTES = 60;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Set a cache entry with default TTL
     */
    public void set(String key, Object value) {
        set(key, value, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Set a cache entry with custom TTL
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            logger.debug("Cache set: {} (TTL: {} {})", key, timeout, unit);
        } catch (Exception e) {
            logger.error("Failed to set cache for key: {}", key, e);
        }
    }

    /**
     * Get a cached value
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Failed to get cache for key: {}", key, e);
            return null;
        }
    }

    /**
     * Get a cached value as a specific type
     */
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && type.isAssignableFrom(value.getClass())) {
                return type.cast(value);
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to get cache for key: {}", key, e);
            return null;
        }
    }

    /**
     * Check if a key exists
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Failed to check cache existence for key: {}", key, e);
            return false;
        }
    }

    /**
     * Delete a cache entry
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            logger.debug("Cache deleted: {}", key);
        } catch (Exception e) {
            logger.error("Failed to delete cache for key: {}", key, e);
        }
    }

    /**
     * Delete all cache entries with a given prefix
     */
    public void deletePattern(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("Deleted {} cache entries with pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            logger.error("Failed to delete cache with pattern: {}", pattern, e);
        }
    }

    /**
     * Get the TTL of a key
     */
    public long getTTL(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2; // -2 if key doesn't exist, -1 if no expiry
        } catch (Exception e) {
            logger.error("Failed to get TTL for key: {}", key, e);
            return -2;
        }
    }

    /**
     * Increment a counter in Redis
     */
    public long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            logger.error("Failed to increment counter: {}", key, e);
            return -1;
        }
    }

    /**
     * Get count of cached metrics
     */
    public long getMetricsCacheCount() {
        try {
            var keys = redisTemplate.keys(METRIC_CACHE_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.error("Failed to get metrics cache count", e);
            return 0;
        }
    }

    /**
     * Clear all caches
     */
    public void clearAll() {
        try {
            var keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("Cleared all cache entries");
            }
        } catch (Exception e) {
            logger.error("Failed to clear all caches", e);
        }
    }
}
