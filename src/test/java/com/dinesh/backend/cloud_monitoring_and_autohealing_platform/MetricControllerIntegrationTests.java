package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.repository.MetricRepository;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Metric Controller Integration Tests")
class MetricControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private AlertService alertService;

    @BeforeEach
    void setUp() {
        metricRepository.clear();
        alertService.clearAlerts();
    }

    @Test
    @DisplayName("POST /api/metrics with normal metrics returns 200")
    void testSubmitNormalMetric() throws Exception {
        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 45.5,
                                  "temperature": 70.0,
                                  "memoryUsage": 60.2,
                                  "diskUsage": 55.8
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metric.cpuUsage").value(45.5))
                .andExpect(jsonPath("$.anomalies", hasSize(0)))
                .andExpect(jsonPath("$.recoveryActions", hasSize(0)));
    }

    @Test
    @DisplayName("POST /api/metrics with high CPU triggers anomaly detection")
    void testHighCpuTriggersAnomaly() throws Exception {
        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 92.0,
                                  "temperature": 75.0,
                                  "memoryUsage": 85.0,
                                  "diskUsage": 80.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metric.cpuUsage").value(92.0))
                .andExpect(jsonPath("$.anomalies", not(empty())))
                .andExpect(jsonPath("$.anomalies[0].severity").value("CRITICAL"));
    }

    @Test
    @DisplayName("GET /api/metrics returns all metrics")
    void testGetAllMetrics() throws Exception {
        // Submit a metric first
        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 50.0,
                                  "temperature": 70.0,
                                  "memoryUsage": 60.0,
                                  "diskUsage": 55.0
                                }
                                """))
                .andExpect(status().isOk());

        // Then retrieve all
        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/metrics/{id} returns specific metric")
    void testGetMetricById() throws Exception {
        // Create metric
        String response = mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 55.0,
                                  "temperature": 75.0,
                                  "memoryUsage": 65.0,
                                  "diskUsage": 60.0
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID and retrieve
        mockMvc.perform(get("/api/metrics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpuUsage").exists());
    }

    @Test
    @DisplayName("GET /api/metrics with CPU filter")
    void testGetMetricsWithCpuFilter() throws Exception {
        // Submit metrics
        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 85.0,
                                  "temperature": 75.0,
                                  "memoryUsage": 80.0,
                                  "diskUsage": 75.0
                                }
                                """))
                .andExpect(status().isOk());

        // Query with filter
        mockMvc.perform(get("/api/metrics?cpuMin=80&cpuMax=90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cpuUsage", greaterThanOrEqualTo(80.0)));
    }

    @Test
    @DisplayName("GET /api/anomalies returns detected anomalies")
    void testGetAnomalies() throws Exception {
        // Submit high CPU metric to trigger anomaly
        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 95.0,
                                  "temperature": 80.0,
                                  "memoryUsage": 90.0,
                                  "diskUsage": 85.0
                                }
                                """))
                .andExpect(status().isOk());

        // Get anomalies
        mockMvc.perform(get("/api/anomalies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/recovery/actions returns recovery actions")
    void testGetRecoveryActions() throws Exception {
        // Trigger anomaly first
        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 95.0,
                                  "temperature": 80.0,
                                  "memoryUsage": 90.0,
                                  "diskUsage": 85.0
                                }
                                """))
                .andExpect(status().isOk());

        // Get recovery actions
        mockMvc.perform(get("/api/recovery/actions"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/metrics/{id} removes metric")
    void testDeleteMetric() throws Exception {
        // Create metric
        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 50.0,
                                  "temperature": 70.0,
                                  "memoryUsage": 60.0,
                                  "diskUsage": 55.0
                                }
                                """))
                .andExpect(status().isOk());

        // Delete metric
        mockMvc.perform(delete("/api/metrics/1"))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/metrics/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/ml/detect-anomaly uses ML model")
    void testMLAnomalyDetection() throws Exception {
        mockMvc.perform(post("/api/ml/detect-anomaly")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpuUsage": 95.0,
                                  "temperature": 85.0,
                                  "memoryUsage": 92.0,
                                  "diskUsage": 88.0
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /actuator/health returns UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Simultaneous metric submissions are handled correctly")
    void testConcurrentMetricSubmissions() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/metrics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                      "cpuUsage": %d,
                                      "temperature": 70.0,
                                      "memoryUsage": 60.0,
                                      "diskUsage": 55.0
                                    }
                                    """, 40 + i * 10)))
                    .andExpect(status().isOk());
        }

        // Verify all metrics were stored
        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }
}
