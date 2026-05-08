package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.recovery;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.RecoveryAction;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for service restart recovery actions
 */
@Component
public class ServiceRestartExecutor implements RecoveryExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRestartExecutor.class);

    @Override
    public boolean execute(RecoveryAction action) {
        try {
            logger.info("Executing service restart for: {}", action.getTargetService());
            
            action.setStatus(RecoveryAction.ActionStatus.IN_PROGRESS);
            action.setExecutedAt(java.time.Instant.now());

            // Simulate service restart
            Thread.sleep(1000); // Simulate restart time
            
            logger.info("Service {} restarted successfully", action.getTargetService());
            action.setStatus(RecoveryAction.ActionStatus.SUCCESS);
            action.setCompletedAt(java.time.Instant.now());
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to restart service {}: {}", action.getTargetService(), e.getMessage());
            action.setStatus(RecoveryAction.ActionStatus.FAILED);
            action.setErrorMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean rollback(RecoveryAction action) {
        try {
            logger.info("Rolling back service restart for: {}", action.getTargetService());
            action.setStatus(RecoveryAction.ActionStatus.ROLLED_BACK);
            return true;
        } catch (Exception e) {
            logger.error("Failed to rollback service restart: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean supports(RecoveryAction.ActionType actionType) {
        return actionType == RecoveryAction.ActionType.SERVICE_RESTART;
    }
}
