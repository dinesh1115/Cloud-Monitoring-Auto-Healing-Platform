package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Alert;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AnomalyDetectionService {

    private static final int MOVING_AVERAGE_WINDOW = 5;
    private static final double SPIKE_MULTIPLIER = 1.25;
    private static final int CPU_CRITICAL_THRESHOLD = 80;

    public Optional<Alert> detectAnomaly(Metric metric, List<Metric> priorMetrics) {
        if (metric == null) {
            return Optional.empty();
        }

        if (metric.getCpu() > CPU_CRITICAL_THRESHOLD) {
            return Optional.of(new Alert(
                    metric.getId(),
                    "CRITICAL",
                    "CPU usage exceeds critical threshold: " + metric.getCpu() + "%",
                    Instant.now()
            ));
        }

        double movingAverage = calculateMovingAverageCpu(priorMetrics);
        if (movingAverage > 0 && metric.getCpu() > movingAverage * SPIKE_MULTIPLIER) {
            return Optional.of(new Alert(
                    metric.getId(),
                    "WARN",
                    String.format("CPU usage is anomalous: current=%d%%, movingAverage=%.1f%%", metric.getCpu(), movingAverage),
                    Instant.now()
            ));
        }

        return Optional.empty();
    }

    private double calculateMovingAverageCpu(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return 0;
        }

        int end = Math.min(metrics.size(), MOVING_AVERAGE_WINDOW);
        return metrics.subList(metrics.size() - end, metrics.size())
                .stream()
                .mapToInt(Metric::getCpu)
                .average()
                .orElse(0);
    }
}
