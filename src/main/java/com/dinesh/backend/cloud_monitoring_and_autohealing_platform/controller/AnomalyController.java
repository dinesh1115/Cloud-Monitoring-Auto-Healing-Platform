package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Anomaly;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.AnomalyRule;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.RecoveryAction;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.AnomalyDetectionService;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.recovery.RecoveryOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyController {

    private final AnomalyDetectionService anomalyDetectionService;
    private final RecoveryOrchestratorService recoveryOrchestratorService;

    public AnomalyController(AnomalyDetectionService anomalyDetectionService,
                            RecoveryOrchestratorService recoveryOrchestratorService) {
        this.anomalyDetectionService = anomalyDetectionService;
        this.recoveryOrchestratorService = recoveryOrchestratorService;
    }

    @GetMapping
    public ResponseEntity<List<Anomaly>> getAllAnomalies() {
        return ResponseEntity.ok(anomalyDetectionService.getAllAnomalies());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Anomaly>> getActiveAnomalies() {
        return ResponseEntity.ok(anomalyDetectionService.getActiveAnomalies());
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Anomaly>> getAnomaliesBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok(anomalyDetectionService.getAnomaliesBySeverity(severity));
    }

    @PostMapping("/{anomalyId}/acknowledge")
    public ResponseEntity<String> acknowledgeAnomaly(@PathVariable String anomalyId) {
        anomalyDetectionService.acknowledgeAnomaly(anomalyId);
        return ResponseEntity.ok("Anomaly acknowledged");
    }

    @PostMapping("/{anomalyId}/resolve")
    public ResponseEntity<String> resolveAnomaly(@PathVariable String anomalyId) {
        anomalyDetectionService.resolveAnomaly(anomalyId);
        return ResponseEntity.ok("Anomaly resolved");
    }

    @GetMapping("/rules")
    public ResponseEntity<List<AnomalyRule>> getAllRules() {
        return ResponseEntity.ok(anomalyDetectionService.getAllRules());
    }

    @PostMapping("/rules")
    public ResponseEntity<String> addRule(@RequestBody AnomalyRule rule) {
        anomalyDetectionService.addRule(rule);
        return ResponseEntity.ok("Rule added: " + rule.getRuleId());
    }
}
