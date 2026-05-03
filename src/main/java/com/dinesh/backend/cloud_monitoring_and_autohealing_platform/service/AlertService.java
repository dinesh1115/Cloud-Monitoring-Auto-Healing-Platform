package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Alert;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AlertService {

    private final List<Alert> alerts = new CopyOnWriteArrayList<>();

    public void recordAlert(Alert alert) {
        if (alert != null) {
            alerts.add(alert);
        }
    }

    public List<Alert> getAlerts() {
        return Collections.unmodifiableList(alerts);
    }

    public int getAlertCount() {
        return alerts.size();
    }

    public void clearAlerts() {
        alerts.clear();
    }
}
