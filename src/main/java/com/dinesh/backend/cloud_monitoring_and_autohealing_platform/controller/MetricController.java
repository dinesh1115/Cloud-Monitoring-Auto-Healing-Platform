package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.CpuUsageService;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
public class MetricController {

    private final MetricService metricService;
    private final CpuUsageService cpuUsageService;

    @GetMapping("/cpuusage")
    public String getCpuUsage(@RequestParam int usage) {
        return cpuUsageService.getCpuUsage(usage);
    }

    public MetricController(MetricService metricService, CpuUsageService cpuUsageService) {
        this.metricService = metricService;
        this.cpuUsageService = cpuUsageService;
    }

    @GetMapping
    public ResponseEntity<List<Metric>> getAllMetrics(
            @RequestParam(required = false) Integer cpuMin,
            @RequestParam(required = false) Integer cpuMax,
            @RequestParam(required = false) Integer tempMin,
            @RequestParam(required = false) Integer tempMax,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "timestamp") String sort,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        return ResponseEntity.ok(metricService.findAllFiltered(cpuMin, cpuMax, tempMin, tempMax, limit, offset, sort, order));
    }

    @PostMapping
    public ResponseEntity<?> createMetric(@RequestBody Metric metric) {
        Metric saved = metricService.save(metric);
        if (saved.getCpu() > 50) {
            return ResponseEntity.ok(Map.of("metric", saved, "warning", "⚠️ Warning: CPU usage is high!"));
        }
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Metric> getMetricById(@PathVariable Long id) {
        return ResponseEntity.of(metricService.findById(id));
    }

}