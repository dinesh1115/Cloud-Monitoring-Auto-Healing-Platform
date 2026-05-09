package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.MetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Metric Service Tests")
class MetricServiceTests {

    @Autowired
    private MetricService metricService;

    private Metric metric;

    @BeforeEach
    void setUp() {
        metric = new Metric();
        metric.setCpu(50.0);
        metric.setTemperature(70.0);
        metric.setMemoryUsage(60.0);
        metric.setDiskUsage(55.0);
    }

    @Test
    @DisplayName("Should save metric successfully")
    void testSaveMetric() {
        Metric saved = metricService.save(metric);
        
        assertNotNull(saved, "Saved metric should not be null");
        assertNotNull(saved.getId(), "Saved metric should have ID");
        assertEquals(metric.getCpu(), saved.getCpu(), "CPU should match");
        assertEquals(metric.getTemperature(), saved.getTemperature(), "Temperature should match");
    }

    @Test
    @DisplayName("Should retrieve saved metric by ID")
    void testGetMetricById() {
        Metric saved = metricService.save(metric);
        Metric retrieved = metricService.getMetricById(saved.getId());
        
        assertNotNull(retrieved, "Retrieved metric should not be null");
        assertEquals(saved.getId(), retrieved.getId(), "IDs should match");
        assertEquals(saved.getCpu(), retrieved.getCpu(), "CPU should match");
    }

    @Test
    @DisplayName("Should retrieve all metrics")
    void testGetAllMetrics() {
        metricService.save(metric);
        
        var metrics = metricService.getAllMetrics();
        assertNotNull(metrics, "Metrics list should not be null");
        assertFalse(metrics.isEmpty(), "Should have at least one metric");
    }

    @Test
    @DisplayName("Should filter metrics by CPU range")
    void testFilterMetricsByCpuRange() {
        metric.setCpu(85.0);
        metricService.save(metric);
        
        metric.setCpu(35.0);
        metricService.save(metric);
        
        var filtered = metricService.getMetricsByCpuRange(50, 90);
        assertTrue(filtered.stream().allMatch(m -> m.getCpu() >= 50 && m.getCpu() <= 90),
                "All metrics should be within range");
    }

    @Test
    @DisplayName("Should get latest metric")
    void testGetLatestMetric() {
        metric.setCpu(40.0);
        metricService.save(metric);
        
        Thread.sleep(100); // Small delay to ensure timestamp difference
        
        metric.setCpu(60.0);
        Metric latest = metricService.save(metric);
        
        Metric retrieved = metricService.getLatestMetric();
        assertEquals(latest.getId(), retrieved.getId(), "Should get most recent metric");
    }

    @Test
    @DisplayName("Should delete metric")
    void testDeleteMetric() {
        Metric saved = metricService.save(metric);
        metricService.deleteMetric(saved.getId());
        
        Metric retrieved = metricService.getMetricById(saved.getId());
        assertNull(retrieved, "Deleted metric should not be retrievable");
    }

    @Test
    @DisplayName("Should handle invalid metric data gracefully")
    void testInvalidMetricData() {
        metric.setCpu(-1.0); // Invalid CPU
        
        assertThrows(Exception.class, () -> {
            metricService.save(metric);
        }, "Should reject invalid metric data");
    }

    @Test
    @DisplayName("Should sort metrics by CPU descending")
    void testSortByCpuDescending() {
        metricService.save(createMetricWithCpu(40.0));
        metricService.save(createMetricWithCpu(80.0));
        metricService.save(createMetricWithCpu(60.0));
        
        var sorted = metricService.getSortedMetrics("cpu", "desc");
        
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).getCpu() >= sorted.get(i + 1).getCpu(),
                    "Should be sorted by CPU descending");
        }
    }

    private Metric createMetricWithCpu(double cpu) {
        Metric m = new Metric();
        m.setCpu(cpu);
        m.setTemperature(70.0);
        m.setMemoryUsage(60.0);
        m.setDiskUsage(55.0);
        return m;
    }
}
