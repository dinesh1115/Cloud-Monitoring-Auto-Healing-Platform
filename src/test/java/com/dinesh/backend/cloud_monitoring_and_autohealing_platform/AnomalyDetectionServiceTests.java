package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Anomaly;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.AnomalyDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Anomaly Detection Service Tests")
class AnomalyDetectionServiceTests {

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

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
    @DisplayName("Normal metrics should not trigger anomaly detection")
    void testNormalMetricsNoAnomaly() {
        List<Anomaly> anomalies = anomalyDetectionService.analyzeMetric(metric);
        assertTrue(anomalies.isEmpty(), "Normal metrics should not generate anomalies");
    }

    @Test
    @DisplayName("High CPU should trigger CRITICAL anomaly")
    void testHighCpuTriggersAnomaly() {
        metric.setCpu(95.0);
        List<Anomaly> anomalies = anomalyDetectionService.analyzeMetric(metric);
        
        assertFalse(anomalies.isEmpty(), "High CPU should generate anomalies");
        assertTrue(anomalies.stream().anyMatch(a -> "CRITICAL".equals(a.getSeverity())),
                "Should have CRITICAL severity anomaly");
    }

    @Test
    @DisplayName("High temperature should trigger WARNING anomaly")
    void testHighTemperatureTriggersAnomaly() {
        metric.setTemperature(85.0);
        List<Anomaly> anomalies = anomalyDetectionService.analyzeMetric(metric);
        
        assertFalse(anomalies.isEmpty(), "High temperature should generate anomalies");
    }

    @Test
    @DisplayName("Multiple anomalies for extreme metrics")
    void testMultipleAnomalies() {
        metric.setCpu(98.0);
        metric.setTemperature(90.0);
        metric.setMemoryUsage(95.0);
        
        List<Anomaly> anomalies = anomalyDetectionService.analyzeMetric(metric);
        assertTrue(anomalies.size() >= 2, "Should detect multiple anomalies");
    }

    @Test
    @DisplayName("Edge case: Metric at threshold boundary")
    void testThresholdBoundary() {
        metric.setCpu(80.0); // At threshold
        List<Anomaly> anomalies = anomalyDetectionService.analyzeMetric(metric);
        assertNotNull(anomalies, "Should handle boundary metrics");
    }

    @Test
    @DisplayName("Low metrics should be normal")
    void testLowMetricsAreNormal() {
        metric.setCpu(20.0);
        metric.setTemperature(50.0);
        metric.setMemoryUsage(30.0);
        metric.setDiskUsage(25.0);
        
        List<Anomaly> anomalies = anomalyDetectionService.analyzeMetric(metric);
        assertTrue(anomalies.isEmpty(), "Low metrics should not generate anomalies");
    }

    @Test
    @DisplayName("Anomaly contains proper metadata")
    void testAnomalyMetadata() {
        metric.setCpu(92.0);
        List<Anomaly> anomalies = anomalyDetectionService.analyzeMetric(metric);
        
        assertFalse(anomalies.isEmpty());
        Anomaly anomaly = anomalies.get(0);
        assertNotNull(anomaly.getAnomalyId(), "Anomaly should have ID");
        assertNotNull(anomaly.getDetectedAt(), "Anomaly should have detection timestamp");
        assertNotNull(anomaly.getSeverity(), "Anomaly should have severity");
    }
}
