package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.MetricService;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.ml.PythonMLService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for ML-based anomaly detection
 */
@RestController
@RequestMapping("/api/ml")
public class MLController {

    private final PythonMLService pythonMLService;
    private final MetricService metricService;

    public MLController(PythonMLService pythonMLService, MetricService metricService) {
        this.pythonMLService = pythonMLService;
        this.metricService = metricService;
    }

    /**
     * Detect anomaly in a metric using ML
     */
    @PostMapping("/detect")
    public ResponseEntity<PythonMLService.MLAnomalyResult> detectAnomaly(@RequestBody Metric metric) {
        PythonMLService.MLAnomalyResult result = pythonMLService.detectAnomaly(metric);
        return ResponseEntity.ok(result);
    }

    /**
     * Train the ML model with historical metrics
     */
    @PostMapping("/train")
    public ResponseEntity<Map<String, Object>> trainModel() {
        java.util.List<Metric> allMetrics = metricService.findAll();
        boolean success = pythonMLService.trainModel(allMetrics);

        Map<String, Object> response = Map.of(
            "success", success,
            "training_samples", allMetrics.size(),
            "message", success ? "Model trained successfully" : "Model training failed"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get ML model information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getModelInfo() {
        Map<String, Object> info = pythonMLService.getModelInfo();
        return ResponseEntity.ok(info);
    }

    /**
     * Analyze all recent metrics for anomalies
     */
    @GetMapping("/analyze-recent")
    public ResponseEntity<Map<String, Object>> analyzeRecentMetrics(@RequestParam(defaultValue = "100") int limit) {
        java.util.List<Metric> recentMetrics = metricService.findAll().stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .toList();

        java.util.List<Map<String, Object>> results = recentMetrics.stream()
            .map(metric -> {
                PythonMLService.MLAnomalyResult result = pythonMLService.detectAnomaly(metric);
                return Map.<String, Object>of(
                    "metric_id", metric.getId(),
                    "cpu", metric.getCpu(),
                    "temperature", metric.getTemperature(),
                    "timestamp", metric.getTimestamp(),
                    "is_anomaly", result.isAnomaly(),
                    "score", result.getScore(),
                    "confidence", result.getConfidence()
                );
            })
            .toList();

        long anomalyCount = results.stream()
            .filter(r -> (Boolean) r.get("is_anomaly"))
            .count();

        Map<String, Object> response = Map.of(
            "total_analyzed", results.size(),
            "anomalies_detected", anomalyCount,
            "anomaly_rate", ((double) anomalyCount / results.size()) * 100,
            "results", results
        );

        return ResponseEntity.ok(response);
    }
}
