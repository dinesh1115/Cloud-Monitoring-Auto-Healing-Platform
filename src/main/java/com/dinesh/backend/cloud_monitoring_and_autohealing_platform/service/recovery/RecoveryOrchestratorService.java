package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.recovery;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Anomaly;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.RecoveryAction;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates automated recovery workflows based on detected anomalies
 */
@Service
public class RecoveryOrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(RecoveryOrchestratorService.class);

    private final Map<RecoveryAction.ActionType, RecoveryExecutor> executors = new ConcurrentHashMap<>();
    private final Map<String, RecoveryAction> executedActions = new ConcurrentHashMap<>();

    public RecoveryOrchestratorService(ServiceRestartExecutor restartExecutor,
                                      AlertNotificationExecutor alertExecutor) {
        this.executors.put(RecoveryAction.ActionType.SERVICE_RESTART, restartExecutor);
        this.executors.put(RecoveryAction.ActionType.ALERT_NOTIFICATION, alertExecutor);
    }

    /**
     * Execute recovery workflow based on anomaly severity
     */
    public List<RecoveryAction> executeRecoveryWorkflow(Anomaly anomaly) {
        List<RecoveryAction> recoveryPlan = planRecoveryActions(anomaly);
        List<RecoveryAction> executedPlan = new ArrayList<>();

        for (RecoveryAction action : recoveryPlan) {
            if (executeAction(action)) {
                executedPlan.add(action);
                String actionId = UUID.randomUUID().toString();
                action.setActionId(actionId);
                executedActions.put(actionId, action);
            } else {
                logger.warn("Recovery action failed: {} for anomaly: {}", action.getActionType(), anomaly.getAnomalyId());
                // Continue with next recovery action even if one fails
            }
        }

        return executedPlan;
    }

    /**
     * Plan recovery actions based on anomaly severity
     */
    private List<RecoveryAction> planRecoveryActions(Anomaly anomaly) {
        List<RecoveryAction> plan = new ArrayList<>();

        // Always send alert
        RecoveryAction alertAction = new RecoveryAction(
                anomaly.getAnomalyId(),
                RecoveryAction.ActionType.ALERT_NOTIFICATION,
                "monitoring-system",
                "Anomaly detected: " + anomaly.getDescription()
        );
        plan.add(alertAction);

        // Add severity-based recovery actions
        switch (anomaly.getSeverity().toUpperCase()) {
            case "CRITICAL":
                // For critical anomalies, restart the service
                RecoveryAction restartAction = new RecoveryAction(
                        anomaly.getAnomalyId(),
                        RecoveryAction.ActionType.SERVICE_RESTART,
                        "affected-service",
                        "Critical anomaly detected - initiating service restart"
                );
                plan.add(restartAction);
                break;

            case "HIGH":
                // For high severity, cache clear and alert
                RecoveryAction cacheAction = new RecoveryAction(
                        anomaly.getAnomalyId(),
                        RecoveryAction.ActionType.CACHE_CLEAR,
                        "cache-system",
                        "High severity anomaly detected - clearing cache"
                );
                plan.add(cacheAction);
                break;

            case "MEDIUM":
            case "LOW":
                // For lower severity, just alert (already added above)
                break;
        }

        return plan;
    }

    /**
     * Execute a single recovery action
     */
    private boolean executeAction(RecoveryAction action) {
        RecoveryExecutor executor = executors.get(action.getActionType());
        if (executor != null) {
            logger.info("Executing recovery action: {} for anomaly: {}", 
                    action.getActionType(), action.getAnomalyId());
            return executor.execute(action);
        } else {
            logger.warn("No executor found for action type: {}", action.getActionType());
            return false;
        }
    }

    /**
     * Rollback a recovery action
     */
    public boolean rollbackAction(String actionId) {
        RecoveryAction action = executedActions.get(actionId);
        if (action != null) {
            RecoveryExecutor executor = executors.get(action.getActionType());
            if (executor != null) {
                logger.info("Rolling back recovery action: {}", actionId);
                return executor.rollback(action);
            }
        }
        return false;
    }

    /**
     * Get all executed recovery actions
     */
    public List<RecoveryAction> getAllActions() {
        return new ArrayList<>(executedActions.values());
    }

    /**
     * Get recovery actions by status
     */
    public List<RecoveryAction> getActionsByStatus(RecoveryAction.ActionStatus status) {
        return executedActions.values().stream()
                .filter(action -> action.getStatus() == status)
                .toList();
    }

    /**
     * Get recovery actions by anomaly
     */
    public List<RecoveryAction> getActionsByAnomaly(String anomalyId) {
        return executedActions.values().stream()
                .filter(action -> action.getAnomalyId().equals(anomalyId))
                .toList();
    }
}
