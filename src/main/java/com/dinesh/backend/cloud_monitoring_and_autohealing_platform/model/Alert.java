package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model;

import java.time.Instant;

public class Alert {

    private final Long metricId;
    private final String severity;
    private final String message;
    private final Instant timestamp;

    public Alert(Long metricId, String severity, String message, Instant timestamp) {
        this.metricId = metricId;
        this.severity = severity;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public Long getMetricId() {
        return metricId;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
