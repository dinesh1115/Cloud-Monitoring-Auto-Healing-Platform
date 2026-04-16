package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metrics")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping
    public ResponseEntity<List<Metric>> getAllMetrics() {
        return ResponseEntity.ok(metricService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Metric> getMetricById(@PathVariable Long id) {
        return ResponseEntity.of(metricService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Metric> createMetric(@RequestBody Metric metric) {
        Metric saved = metricService.save(metric);
        return ResponseEntity.ok(saved);
    }
}