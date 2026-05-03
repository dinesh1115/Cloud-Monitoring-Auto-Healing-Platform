package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.repository.MetricRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MetricService {

    private final MetricRepository metricRepository;
    private final AnomalyDetectionService anomalyDetectionService;
    private final AlertService alertService;

    public MetricService(MetricRepository metricRepository,
                         AnomalyDetectionService anomalyDetectionService,
                         AlertService alertService) {
        this.metricRepository = metricRepository;
        this.anomalyDetectionService = anomalyDetectionService;
        this.alertService = alertService;
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
        anomalyDetectionService.detectAnomaly(savedMetric, priorMetrics)
                .ifPresent(alertService::recordAlert);

        return savedMetric;
    }

    public int count() {
        return metricRepository.findAll().size();
    }
}

