package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model;

/**
 * Represents a rule for detecting anomalies in metrics
 */
public class AnomalyRule {

    public enum MetricType {
        CPU, TEMPERATURE, MEMORY
    }

    public enum Operator {
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<="),
        EQUAL("==");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private String ruleId;
    private MetricType metricType;
    private Operator operator;
    private double threshold;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String description;
    private boolean enabled;

    public AnomalyRule() {
        this.enabled = true;
    }

    public AnomalyRule(String ruleId, MetricType metricType, Operator operator, double threshold, String severity, String description) {
        this.ruleId = ruleId;
        this.metricType = metricType;
        this.operator = operator;
        this.threshold = threshold;
        this.severity = severity;
        this.description = description;
        this.enabled = true;
    }

    public boolean evaluate(double metricValue) {
        if (!enabled) return false;

        return switch (operator) {
            case GREATER_THAN -> metricValue > threshold;
            case LESS_THAN -> metricValue < threshold;
            case GREATER_EQUAL -> metricValue >= threshold;
            case LESS_EQUAL -> metricValue <= threshold;
            case EQUAL -> metricValue == threshold;
        };
    }

    // Getters and Setters
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
