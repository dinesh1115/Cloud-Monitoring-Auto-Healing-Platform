package com.dinesh.backend.cloud_monitoring_and_autohealing_platform;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.repository.MetricRepository;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CloudMonitoringAndAutoHealingPlatformApplicationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private AlertService alertService;

    @BeforeEach
    void cleanup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        metricRepository.clear();
        alertService.clearAlerts();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void healthEndpointReturnsUpAndCounts() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.metricCount").value(0))
                .andExpect(jsonPath("$.alertCount").value(0));
    }

    @Test
    void alertsEndpointReturnsEmptyListInitially() throws Exception {
        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    void postingHighCpuMetricGeneratesAnomalyAlert() throws Exception {
        mockMvc.perform(post("/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpu\": 85, \"temperature\": 70}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warning").exists())
                .andExpect(jsonPath("$.metric.cpu").value(85));

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    void latestMetricEndpointReturnsMostRecentMetric() throws Exception {
        mockMvc.perform(post("/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpu\": 20, \"temperature\": 45}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/metrics/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpu").value(20))
                .andExpect(jsonPath("$.temperature").value(45));
    }

    @Test
    void deleteAlertsEndpointClearsAlertHistory() throws Exception {
        mockMvc.perform(post("/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpu\": 85, \"temperature\": 70}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/alerts"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").doesNotExist());
    }
}
