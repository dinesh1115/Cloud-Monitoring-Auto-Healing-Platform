package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Alert;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.AlertService;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SystemStatusController {

    private final MetricService metricService;
    private final AlertService alertService;

    public SystemStatusController(MetricService metricService, AlertService alertService) {
        this.metricService = metricService;
        this.alertService = alertService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "metricCount", metricService.count(),
                "alertCount", alertService.getAlertCount()
        ));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts() {
        return ResponseEntity.ok(alertService.getAlerts());
    }
}
