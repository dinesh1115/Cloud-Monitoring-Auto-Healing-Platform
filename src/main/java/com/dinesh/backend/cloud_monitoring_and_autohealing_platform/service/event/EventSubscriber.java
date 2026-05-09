package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.event;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.event.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.config.RedisEnabledCondition;

/**
 * Service for subscribing to system events from Redis pub/sub channels
 * Only active when Redis is enabled
 * Handles event processing and dispatching to appropriate handlers
 */
@Service
@Conditional(RedisEnabledCondition.class)
public class EventSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(EventSubscriber.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;

    public EventSubscriber(RedisTemplate<String, Object> redisTemplate,
                          RedisMessageListenerContainer listenerContainer) {
        this.redisTemplate = redisTemplate;
        this.listenerContainer = listenerContainer;
        initializeSubscriptions();
    }

    /**
     * Initialize subscriptions to Redis channels
     */
    private void initializeSubscriptions() {
        try {
            listenerContainer.addMessageListener(this, new ChannelTopic(EventPublisher.METRICS_CHANNEL));
            listenerContainer.addMessageListener(this, new ChannelTopic(EventPublisher.ANOMALIES_CHANNEL));
            listenerContainer.addMessageListener(this, new ChannelTopic(EventPublisher.ALERTS_CHANNEL));
            listenerContainer.addMessageListener(this, new ChannelTopic(EventPublisher.HEALING_CHANNEL));
            listenerContainer.addMessageListener(this, new ChannelTopic(EventPublisher.SYSTEM_CHANNEL));

            logger.info("Event subscriptions initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize event subscriptions", e);
        }
    }

    /**
     * Message listener for Redis pub/sub
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            logger.debug("Message received on channel: {}", channel);

            // Get the raw message body as string for logging
            String messageBody = new String(message.getBody());
            logger.debug("Message body: {}", messageBody);

            // For now, just log the event - in production, deserialize properly
            // SystemEvent event = (SystemEvent) redisTemplate.getValueSerializer()
            //         .deserialize(message.getBody());
            
            logger.info("Event received on channel '{}': {}", channel, messageBody);
        } catch (Exception e) {
            logger.error("Error processing Redis message", e);
        }
    }

    /**
     * Handle events based on type
     */
    private void handleEvent(SystemEvent event, String channel) {
        logger.info("Processing event: {} from channel: {}", event.getEventType(), channel);

        switch (event.getEventType()) {
            case METRIC_RECEIVED:
                handleMetricEvent(event);
                break;
            case ANOMALY_DETECTED:
                handleAnomalyEvent(event);
                break;
            case ALERT_TRIGGERED:
                handleAlertEvent(event);
                break;
            case HEALING_ACTION_INITIATED:
                handleHealingInitiated(event);
                break;
            case HEALING_ACTION_COMPLETED:
                handleHealingCompleted(event);
                break;
            default:
                logger.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleMetricEvent(SystemEvent event) {
        logger.info("Handling metric event: {}", event.getMessage());
        // Implement metric event handling logic
    }

    private void handleAnomalyEvent(SystemEvent event) {
        logger.info("Handling anomaly event: {}", event.getMessage());
        // Implement anomaly event handling logic
    }

    private void handleAlertEvent(SystemEvent event) {
        logger.info("Handling alert event (severity: {}): {}", event.getSeverity(), event.getMessage());
        // Implement alert event handling logic
    }

    private void handleHealingInitiated(SystemEvent event) {
        logger.info("Healing action initiated: {}", event.getMessage());
        // Implement healing initiation handling logic
    }

    private void handleHealingCompleted(SystemEvent event) {
        logger.info("Healing action completed: {}", event.getMessage());
        // Implement healing completion handling logic
    }
}
