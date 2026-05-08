package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model;

import java.time.Instant;

/**
 * Represents an automated recovery action taken by the system
 */
public class RecoveryAction {

    public enum ActionType {
        SERVICE_RESTART,
        SCALING_UP,
        CACHE_CLEAR,
        ALERT_NOTIFICATION,
        CIRCUIT_BREAK,
        RESOURCE_REALLOCATION
    }

    public enum ActionStatus {
        PENDING,
        IN_PROGRESS,
        SUCCESS,
        FAILED,
        ROLLED_BACK
    }

    private String actionId;
    private String anomalyId;
    private ActionType actionType;
    private String targetService;
    private String description;
    private ActionStatus status;
    private String errorMessage;
    private Instant createdAt;
    private Instant executedAt;
    private Instant completedAt;

    public RecoveryAction() {
        this.createdAt = Instant.now();
        this.status = ActionStatus.PENDING;
    }

    public RecoveryAction(String anomalyId, ActionType actionType, String targetService, String description) {
        this.anomalyId = anomalyId;
        this.actionType = actionType;
        this.targetService = targetService;
        this.description = description;
        this.createdAt = Instant.now();
        this.status = ActionStatus.PENDING;
    }

    // Getters and Setters
    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getAnomalyId() {
        return anomalyId;
    }

    public void setAnomalyId(String anomalyId) {
        this.anomalyId = anomalyId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
