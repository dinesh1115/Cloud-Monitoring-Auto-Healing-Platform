package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.repository;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MetricRepository {

    private final Map<Long, Metric> metrics = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public List<Metric> findAll() {
        return new ArrayList<>(metrics.values());
    }

    public Optional<Metric> findById(Long id) {
        return Optional.ofNullable(metrics.get(id));
    }

    public Metric save(Metric metric) {
        if (metric == null) {
            throw new IllegalArgumentException("Metric cannot be null");
        }

        if (metric.getId() == null) {
            metric.setId(idSequence.incrementAndGet());
        }

        if (metric.getTimestamp() == null) {
            metric.setTimestamp(Instant.now());
        }

        metrics.put(metric.getId(), metric);
        return metric;
    }
}

