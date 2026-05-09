package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.aws;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Anomaly;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * AWS CloudWatch integration for publishing metrics and alarms
 */
@Service
public class CloudWatchService {

    private final CloudWatchClient cloudWatchClient;

    @Value("${aws.cloudwatch.namespace:CloudMonitoringPlatform}")
    private String namespace;

    @Value("${aws.region:us-east-1}")
    private String region;

    public CloudWatchService() {
        this.cloudWatchClient = CloudWatchClient.create();
    }

    /**
     * Publish metric data to CloudWatch
     */
    public void publishMetric(Metric metric) {
        try {
            List<MetricDatum> metricData = new ArrayList<>();

            // CPU metric
            MetricDatum cpuDatum = MetricDatum.builder()
                    .metricName("CPUUsage")
                    .value((double) metric.getCpu())
                    .unit(StandardUnit.PERCENT)
                    .timestamp(Instant.now())
                    .dimensions(Dimension.builder()
                            .name("Service")
                            .value("CloudMonitoringPlatform")
                            .build())
                    .build();
            metricData.add(cpuDatum);

            // Temperature metric
            MetricDatum tempDatum = MetricDatum.builder()
                    .metricName("Temperature")
                    .value((double) metric.getTemperature())
                    .unit(StandardUnit.NONE) // Temperature in Celsius (no specific unit in CloudWatch)
                    .timestamp(Instant.now())
                    .dimensions(Dimension.builder()
                            .name("Service")
                            .value("CloudMonitoringPlatform")
                            .build())
                    .build();
            metricData.add(tempDatum);

            PutMetricDataRequest request = PutMetricDataRequest.builder()
                    .namespace(namespace)
                    .metricData(metricData)
                    .build();

            cloudWatchClient.putMetricData(request);

        } catch (Exception e) {
            // Log error but don't fail the application
            System.err.println("Failed to publish metrics to CloudWatch: " + e.getMessage());
        }
    }

    /**
     * Create CloudWatch alarm for anomaly detection
     */
    public void createAnomalyAlarm(Anomaly anomaly) {
        try {
            String alarmName = "Anomaly-" + anomaly.getAnomalyId();
            String metricName = anomaly.getMetricType() == com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.AnomalyRule.MetricType.CPU
                    ? "CPUUsage" : "Temperature";

            PutMetricAlarmRequest alarmRequest = PutMetricAlarmRequest.builder()
                    .alarmName(alarmName)
                    .alarmDescription(anomaly.getDescription())
                    .metricName(metricName)
                    .namespace(namespace)
                    .statistic(Statistic.MAXIMUM)
                    .period(300) // 5 minutes
                    .threshold(anomaly.getThreshold())
                    .comparisonOperator(
                        anomaly.getSeverity().equals("CRITICAL") ?
                        ComparisonOperator.GREATER_THAN_THRESHOLD :
                        ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD
                    )
                    .evaluationPeriods(2)
                    .alarmActions("arn:aws:sns:" + region + ":123456789012:AnomalyAlerts") // Replace with actual SNS topic
                    .build();

            cloudWatchClient.putMetricAlarm(alarmRequest);

        } catch (Exception e) {
            System.err.println("Failed to create CloudWatch alarm: " + e.getMessage());
        }
    }

    /**
     * Get metric statistics from CloudWatch
     */
    public List<Datapoint> getMetricStatistics(String metricName, int hours) {
        try {
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace(namespace)
                    .metricName(metricName)
                    .startTime(Instant.now().minusSeconds(hours * 3600L))
                    .endTime(Instant.now())
                    .period(300) // 5-minute intervals
                    .statistics(Statistic.AVERAGE, Statistic.MAXIMUM, Statistic.MINIMUM)
                    .build();

            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
            return response.datapoints();

        } catch (Exception e) {
            System.err.println("Failed to get metric statistics: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * List all alarms
     */
    public List<MetricAlarm> listAlarms() {
        try {
            DescribeAlarmsRequest request = DescribeAlarmsRequest.builder().build();
            DescribeAlarmsResponse response = cloudWatchClient.describeAlarms(request);
            return response.metricAlarms();

        } catch (Exception e) {
            System.err.println("Failed to list alarms: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
