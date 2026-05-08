package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event model for pub/sub communication
 * Represents system events (metrics, alerts, anomalies)
 */
public class SystemEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private EventType eventType;
    private EventSeverity severity;
    private String source;
    private String message;
    private Object payload;
    private LocalDateTime timestamp;
    private String correlationId;

    public enum EventType {
        METRIC_RECEIVED,
        ANOMALY_DETECTED,
        ALERT_TRIGGERED,
        HEALING_ACTION_INITIATED,
        HEALING_ACTION_COMPLETED,
        SYSTEM_ERROR
    }

    public enum EventSeverity {
        INFO,
        WARNING,
        CRITICAL
    }

    public SystemEvent() {
    }

    public SystemEvent(EventType eventType, EventSeverity severity, String source, String message) {
        this.eventType = eventType;
        this.severity = severity;
        this.source = source;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public EventSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(EventSeverity severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        return "SystemEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType=" + eventType +
                ", severity=" + severity +
                ", source='" + source + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}
