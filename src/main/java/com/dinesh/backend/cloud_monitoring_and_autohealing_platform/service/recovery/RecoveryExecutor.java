package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.recovery;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.RecoveryAction;

/**
 * Strategy interface for executing recovery actions
 */
public interface RecoveryExecutor {

    /**
     * Execute the recovery action
     */
    boolean execute(RecoveryAction action);

    /**
     * Rollback the recovery action if needed
     */
    boolean rollback(RecoveryAction action);

    /**
     * Check if this executor can handle the action type
     */
    boolean supports(RecoveryAction.ActionType actionType);
}
