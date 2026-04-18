package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.repository.MetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetricService {

    private final MetricRepository metricRepository;

    public MetricService(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    public List<Metric> findAll() {
        return metricRepository.findAll();
    }

    public Optional<Metric> findById(Long id) {
        return metricRepository.findById(id);
    }

    public Metric save(Metric metric) {
        return metricRepository.save(metric);
    }
}

