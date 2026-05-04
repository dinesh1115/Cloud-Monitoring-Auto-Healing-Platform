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

