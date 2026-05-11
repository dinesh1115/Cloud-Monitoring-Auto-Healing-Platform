package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Alert;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Anomaly;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.AnomalyRule;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AnomalyDetectionService {

    private static final int MOVING_AVERAGE_WINDOW = 5;
    private static final double SPIKE_MULTIPLIER = 1.25;
    private static final int CPU_CRITICAL_THRESHOLD = 80;

    private final Map<String, AnomalyRule> rules = new ConcurrentHashMap<>();
    private final Map<String, Anomaly> detectedAnomalies = new ConcurrentHashMap<>();

    public AnomalyDetectionService() {
        initializeDefaultRules();
    }

    private void initializeDefaultRules() {
        AnomalyRule highCpuRule = new AnomalyRule(
                "cpu_high",
                AnomalyRule.MetricType.CPU,
                AnomalyRule.Operator.GREATER_EQUAL,
                80.0,
                "HIGH",
                "CPU usage is 80% or higher"
        );
        rules.put("cpu_high", highCpuRule);

        AnomalyRule criticalCpuRule = new AnomalyRule(
                "cpu_critical",
                AnomalyRule.MetricType.CPU,
                AnomalyRule.Operator.GREATER_EQUAL,
                95.0,
                "CRITICAL",
                "CPU usage is 95% or higher"
        );
        rules.put("cpu_critical", criticalCpuRule);

        AnomalyRule highTempRule = new AnomalyRule(
                "temp_high",
                AnomalyRule.MetricType.TEMPERATURE,
                AnomalyRule.Operator.GREATER_THAN,
                85.0,
                "WARNING",
                "Temperature is 85°C or higher"
        );
        rules.put("temp_high", highTempRule);

        AnomalyRule criticalTempRule = new AnomalyRule(
                "temp_critical",
                AnomalyRule.MetricType.TEMPERATURE,
                AnomalyRule.Operator.GREATER_THAN,
                95.0,
                "CRITICAL",
                "Temperature is 95°C or higher"
        );
        rules.put("temp_critical", criticalTempRule);
    }

    public Optional<Alert> detectAnomaly(Metric metric, List<Metric> priorMetrics) {
        if (metric == null) {
            return Optional.empty();
        }

        if (metric.getCpu() > CPU_CRITICAL_THRESHOLD) {
            return Optional.of(new Alert(
                    metric.getId(),
                    "CRITICAL",
                    "CPU usage exceeds critical threshold: " + metric.getCpu() + "%",
                    Instant.now()
            ));
        }

        double movingAverage = calculateMovingAverageCpu(priorMetrics);
        if (movingAverage > 0 && metric.getCpu() > movingAverage * SPIKE_MULTIPLIER) {
            return Optional.of(new Alert(
                    metric.getId(),
                    "WARN",
                    String.format("CPU usage is anomalous: current=%d%%, movingAverage=%.1f%%", metric.getCpu(), movingAverage),
                    Instant.now()
            ));
        }

        return Optional.empty();
    }

    public List<Anomaly> analyzeMetric(Metric metric) {
        List<Anomaly> detectedInMetric = new ArrayList<>();
        double cpuValue = metric.getCpu();
        double tempValue = metric.getTemperature();

        rules.values().stream()
                .filter(AnomalyRule::isEnabled)
                .forEach(rule -> {
                    boolean matches = false;
                    if (rule.getMetricType() == AnomalyRule.MetricType.CPU && rule.evaluate(cpuValue)) {
                        matches = true;
                    } else if (rule.getMetricType() == AnomalyRule.MetricType.TEMPERATURE && rule.evaluate(tempValue)) {
                        matches = true;
                    }

                    if (matches) {
                        Anomaly anomaly = new Anomaly();
                        anomaly.setAnomalyId(UUID.randomUUID().toString());
                        anomaly.setMetricId(metric.getId());
                        anomaly.setRuleId(rule.getRuleId());
                        anomaly.setMetricType(rule.getMetricType());
                        anomaly.setMetricValue(rule.getMetricType() == AnomalyRule.MetricType.CPU ? cpuValue : tempValue);
                        anomaly.setThreshold(rule.getThreshold());
                        anomaly.setSeverity(rule.getSeverity());
                        anomaly.setDescription(rule.getDescription());
                        anomaly.setStatus("DETECTED");
                        detectedInMetric.add(anomaly);
                        detectedAnomalies.put(anomaly.getAnomalyId(), anomaly);
                    }
                });

        return detectedInMetric;
    }

    public void addRule(AnomalyRule rule) {
        rules.put(rule.getRuleId(), rule);
    }

    public List<AnomalyRule> getAllRules() {
        return new ArrayList<>(rules.values());
    }

    public List<Anomaly> getAllAnomalies() {
        return new ArrayList<>(detectedAnomalies.values());
    }

    public List<Anomaly> getActiveAnomalies() {
        return detectedAnomalies.values().stream()
                .filter(a -> !a.getStatus().equalsIgnoreCase("RESOLVED"))
                .collect(Collectors.toList());
    }

    public List<Anomaly> getAnomaliesBySeverity(String severity) {
        return detectedAnomalies.values().stream()
                .filter(a -> a.getSeverity().equalsIgnoreCase(severity))
                .collect(Collectors.toList());
    }

    public void acknowledgeAnomaly(String anomalyId) {
        Anomaly anomaly = detectedAnomalies.get(anomalyId);
        if (anomaly != null) {
            anomaly.setStatus("ACKNOWLEDGED");
        }
    }

    public void resolveAnomaly(String anomalyId) {
        Anomaly anomaly = detectedAnomalies.get(anomalyId);
        if (anomaly != null) {
            anomaly.setStatus("RESOLVED");
        }
    }

    private double calculateMovingAverageCpu(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return 0;
        }

        int end = Math.min(metrics.size(), MOVING_AVERAGE_WINDOW);
        return metrics.subList(metrics.size() - end, metrics.size())
                .stream()
                .mapToDouble(Metric::getCpu)
                .average()
                .orElse(0.0);
    }
}
