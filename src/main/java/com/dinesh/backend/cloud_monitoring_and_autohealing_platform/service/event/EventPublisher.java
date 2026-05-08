package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.event;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.event.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for publishing system events to Redis pub/sub channels
 * Enables event-driven communication between microservices
 */
@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis channel names
    public static final String METRICS_CHANNEL = "events:metrics";
    public static final String ANOMALIES_CHANNEL = "events:anomalies";
    public static final String ALERTS_CHANNEL = "events:alerts";
    public static final String HEALING_CHANNEL = "events:healing";
    public static final String SYSTEM_CHANNEL = "events:system";

    public EventPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Publish an event to the appropriate Redis channel
     */
    public void publishEvent(SystemEvent event) {
        try {
            // Generate event ID if not present
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }

            // Determine the channel based on event type
            String channel = getChannelForEventType(event.getEventType());

            // Publish to Redis
            redisTemplate.convertAndSend(channel, event);

            logger.info("Event published to channel '{}': {}", channel, event);
        } catch (Exception e) {
            logger.error("Failed to publish event: {}", event, e);
        }
    }

    /**
     * Publish a metric event
     */
    public void publishMetricEvent(Object metric) {
        SystemEvent event = new SystemEvent(
                SystemEvent.EventType.METRIC_RECEIVED,
                SystemEvent.EventSeverity.INFO,
                "MetricService",
                "New metric received"
        );
        event.setPayload(metric);
        publishEvent(event);
    }

    /**
     * Publish an anomaly detected event
     */
    public void publishAnomalyEvent(String message, Object anomalyData) {
        SystemEvent event = new SystemEvent(
                SystemEvent.EventType.ANOMALY_DETECTED,
                SystemEvent.EventSeverity.WARNING,
                "AnomalyDetectionService",
                message
        );
        event.setPayload(anomalyData);
        publishEvent(event);
    }

    /**
     * Publish an alert triggered event
     */
    public void publishAlertEvent(String message, SystemEvent.EventSeverity severity, Object alertData) {
        SystemEvent event = new SystemEvent(
                SystemEvent.EventType.ALERT_TRIGGERED,
                severity,
                "AlertService",
                message
        );
        event.setPayload(alertData);
        publishEvent(event);
    }

    /**
     * Publish a healing action initiated event
     */
    public void publishHealingInitiatedEvent(String message, Object healingData) {
        SystemEvent event = new SystemEvent(
                SystemEvent.EventType.HEALING_ACTION_INITIATED,
                SystemEvent.EventSeverity.INFO,
                "HealingService",
                message
        );
        event.setPayload(healingData);
        publishEvent(event);
    }

    /**
     * Publish a healing action completed event
     */
    public void publishHealingCompletedEvent(String message, Object healingResult) {
        SystemEvent event = new SystemEvent(
                SystemEvent.EventType.HEALING_ACTION_COMPLETED,
                SystemEvent.EventSeverity.INFO,
                "HealingService",
                message
        );
        event.setPayload(healingResult);
        publishEvent(event);
    }

    /**
     * Determine the Redis channel for an event type
     */
    private String getChannelForEventType(SystemEvent.EventType eventType) {
        return switch (eventType) {
            case METRIC_RECEIVED -> METRICS_CHANNEL;
            case ANOMALY_DETECTED -> ANOMALIES_CHANNEL;
            case ALERT_TRIGGERED -> ALERTS_CHANNEL;
            case HEALING_ACTION_INITIATED, HEALING_ACTION_COMPLETED -> HEALING_CHANNEL;
            default -> SYSTEM_CHANNEL;
        };
    }
}
