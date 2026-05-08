package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.RecoveryAction;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.recovery.RecoveryOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recovery")
public class RecoveryController {

    private final RecoveryOrchestratorService recoveryOrchestratorService;

    public RecoveryController(RecoveryOrchestratorService recoveryOrchestratorService) {
        this.recoveryOrchestratorService = recoveryOrchestratorService;
    }

    @GetMapping("/actions")
    public ResponseEntity<List<RecoveryAction>> getAllActions() {
        return ResponseEntity.ok(recoveryOrchestratorService.getAllActions());
    }

    @GetMapping("/actions/status/{status}")
    public ResponseEntity<List<RecoveryAction>> getActionsByStatus(@PathVariable RecoveryAction.ActionStatus status) {
        return ResponseEntity.ok(recoveryOrchestratorService.getActionsByStatus(status));
    }

    @GetMapping("/actions/anomaly/{anomalyId}")
    public ResponseEntity<List<RecoveryAction>> getActionsByAnomaly(@PathVariable String anomalyId) {
        return ResponseEntity.ok(recoveryOrchestratorService.getActionsByAnomaly(anomalyId));
    }

    @PostMapping("/actions/{actionId}/rollback")
    public ResponseEntity<String> rollbackAction(@PathVariable String actionId) {
        boolean success = recoveryOrchestratorService.rollbackAction(actionId);
        if (success) {
            return ResponseEntity.ok("Action rolled back: " + actionId);
        } else {
            return ResponseEntity.status(500).body("Failed to rollback action");
        }
    }
}
