package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Anomaly;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.repository.MetricRepository;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.aws.CloudWatchService;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.recovery.RecoveryOrchestratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MetricService {

    private final MetricRepository metricRepository;
    private final AnomalyDetectionService anomalyDetectionService;
    private final AlertService alertService;
    private final CloudWatchService cloudWatchService;
    private final RecoveryOrchestratorService recoveryOrchestratorService;

    @Value("${app.cloudwatch.enabled:false}")
    private boolean cloudWatchEnabled;

    public MetricService(MetricRepository metricRepository,
                         AnomalyDetectionService anomalyDetectionService,
                         AlertService alertService,
                         CloudWatchService cloudWatchService,
                         RecoveryOrchestratorService recoveryOrchestratorService) {
        this.metricRepository = metricRepository;
        this.anomalyDetectionService = anomalyDetectionService;
        this.alertService = alertService;
        this.cloudWatchService = cloudWatchService;
        this.recoveryOrchestratorService = recoveryOrchestratorService;
    }

    public List<Metric> findAll() {
        return metricRepository.findAll();
    }

    public Optional<Metric> findById(Long id) {
        return metricRepository.findById(id);
    }

    public Metric save(Metric metric) {
        List<Metric> priorMetrics = metricRepository.findAll().stream()
                .sorted(Comparator.comparing(Metric::getTimestamp))
                .toList();

        Metric savedMetric = metricRepository.save(metric);

        // Publish to CloudWatch if enabled
        if (cloudWatchEnabled) {
            try {
                cloudWatchService.publishMetric(savedMetric);
            } catch (Exception e) {
                // Log error but don't fail the save operation
                System.err.println("Failed to publish metric to CloudWatch: " + e.getMessage());
            }
        }

        // Detect anomalies
        anomalyDetectionService.detectAnomaly(savedMetric, priorMetrics)
                .ifPresent(alertService::recordAlert);

        // Analyze for rule-based anomalies and trigger recovery if needed
        List<Anomaly> anomalies = anomalyDetectionService.analyzeMetric(savedMetric);
        for (Anomaly anomaly : anomalies) {
            // Create CloudWatch alarm for critical anomalies
            if (cloudWatchEnabled && anomaly.getSeverity().equals("CRITICAL")) {
                try {
                    cloudWatchService.createAnomalyAlarm(anomaly);
                } catch (Exception e) {
                    System.err.println("Failed to create CloudWatch alarm: " + e.getMessage());
                }
            }

            // Trigger auto-healing recovery workflow
            try {
                recoveryOrchestratorService.executeRecoveryWorkflow(anomaly);
            } catch (Exception e) {
                System.err.println("Failed to execute recovery workflow: " + e.getMessage());
            }
        }

        return savedMetric;
    }

    public int count() {
        return metricRepository.findAll().size();
    }

    public List<Metric> findAllFiltered(Integer cpuMin, Integer cpuMax, Integer tempMin, Integer tempMax,
                                       Integer limit, Integer offset, String sort, String order) {
        return metricRepository.findAll().stream()
                .filter(metric -> cpuMin == null || metric.getCpu() >= cpuMin)
                .filter(metric -> cpuMax == null || metric.getCpu() <= cpuMax)
                .filter(metric -> tempMin == null || metric.getTemperature() >= tempMin)
                .filter(metric -> tempMax == null || metric.getTemperature() <= tempMax)
                .sorted(getComparator(sort, order))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    public Optional<Metric> findLatest() {
        return metricRepository.findAll().stream()
                .max(Comparator.comparing(Metric::getTimestamp));
    }

    private Comparator<Metric> getComparator(String sort, String order) {
        Comparator<Metric> comparator;
        switch (sort.toLowerCase()) {
            case "cpu":
                comparator = Comparator.comparing(Metric::getCpu);
                break;
            case "temperature":
                comparator = Comparator.comparing(Metric::getTemperature);
                break;
            case "timestamp":
            default:
                comparator = Comparator.comparing(Metric::getTimestamp);
                break;
        }
        return order.equalsIgnoreCase("asc") ? comparator : comparator.reversed();
    }
}

