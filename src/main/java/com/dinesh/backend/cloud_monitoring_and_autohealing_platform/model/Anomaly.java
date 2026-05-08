package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model;

import java.time.Instant;

/**
 * Represents a detected anomaly in the system
 */
public class Anomaly {

    private String anomalyId;
    private Long metricId;
    private String ruleId;
    private AnomalyRule.MetricType metricType;
    private double metricValue;
    private double threshold;
    private String severity;
    private String description;
    private Instant detectedAt;
    private String status; // DETECTED, ACKNOWLEDGED, RESOLVED

    public Anomaly() {
        this.detectedAt = Instant.now();
        this.status = "DETECTED";
    }

    public Anomaly(Long metricId, String ruleId, AnomalyRule.MetricType metricType, double metricValue, double threshold, String severity, String description) {
        this.metricId = metricId;
        this.ruleId = ruleId;
        this.metricType = metricType;
        this.metricValue = metricValue;
        this.threshold = threshold;
        this.severity = severity;
        this.description = description;
        this.detectedAt = Instant.now();
        this.status = "DETECTED";
    }

    // Getters and Setters
    public String getAnomalyId() {
        return anomalyId;
    }

    public void setAnomalyId(String anomalyId) {
        this.anomalyId = anomalyId;
    }

    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public AnomalyRule.MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(AnomalyRule.MetricType metricType) {
        this.metricType = metricType;
    }

    public double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(double metricValue) {
        this.metricValue = metricValue;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
