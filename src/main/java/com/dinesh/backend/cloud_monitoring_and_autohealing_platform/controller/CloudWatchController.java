package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.aws.CloudWatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.MetricAlarm;

import java.util.List;
import java.util.Map;

/**
 * Controller for AWS CloudWatch integration
 */
@RestController
@RequestMapping("/api/aws/cloudwatch")
public class CloudWatchController {

    private final CloudWatchService cloudWatchService;

    public CloudWatchController(CloudWatchService cloudWatchService) {
        this.cloudWatchService = cloudWatchService;
    }

    /**
     * Get CPU metrics from CloudWatch
     */
    @GetMapping("/metrics/cpu")
    public ResponseEntity<List<Datapoint>> getCpuMetrics(@RequestParam(defaultValue = "24") int hours) {
        List<Datapoint> datapoints = cloudWatchService.getMetricStatistics("CPUUsage", hours);
        return ResponseEntity.ok(datapoints);
    }

    /**
     * Get temperature metrics from CloudWatch
     */
    @GetMapping("/metrics/temperature")
    public ResponseEntity<List<Datapoint>> getTemperatureMetrics(@RequestParam(defaultValue = "24") int hours) {
        List<Datapoint> datapoints = cloudWatchService.getMetricStatistics("Temperature", hours);
        return ResponseEntity.ok(datapoints);
    }

    /**
     * Get all CloudWatch alarms
     */
    @GetMapping("/alarms")
    public ResponseEntity<List<MetricAlarm>> getAlarms() {
        List<MetricAlarm> alarms = cloudWatchService.listAlarms();
        return ResponseEntity.ok(alarms);
    }

    /**
     * Get CloudWatch dashboard summary
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        List<Datapoint> cpuData = cloudWatchService.getMetricStatistics("CPUUsage", 1);
        List<Datapoint> tempData = cloudWatchService.getMetricStatistics("Temperature", 1);
        List<MetricAlarm> alarms = cloudWatchService.listAlarms();

        Map<String, Object> dashboard = Map.of(
            "cpu_metrics_last_hour", cpuData.size(),
            "temperature_metrics_last_hour", tempData.size(),
            "active_alarms", alarms.stream().filter(a -> a.stateValueAsString().equals("ALARM")).count(),
            "total_alarms", alarms.size()
        );

        return ResponseEntity.ok(dashboard);
    }
}
