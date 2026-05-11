package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.controller;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Controller for load testing and performance evaluation
 */
@RestController
@RequestMapping("/api/load-test")
public class LoadTestController {

    private final MetricService metricService;

    public LoadTestController(MetricService metricService) {
        this.metricService = metricService;
    }

    /**
     * Simulate workload by creating metrics
     * Usage: POST /api/load-test/simulate?duration=60&rps=10&cpuVariation=5
     */
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateWorkload(
            @RequestParam(defaultValue = "60") int duration,
            @RequestParam(defaultValue = "10") int rps,
            @RequestParam(defaultValue = "5") int cpuVariation) {

        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        int totalRequests = 0;
        int successCount = 0;
        int errorCount = 0;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        try {
            long endTime = startTime + (duration * 1000L);
            Random random = new Random();

            while (System.currentTimeMillis() < endTime) {
                for (int i = 0; i < rps; i++) {
                    totalRequests++;
                    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            Metric metric = new Metric();
                            metric.setCpu(30 + random.nextInt(cpuVariation * 2)); // Variable CPU
                            metric.setTemperature(50 + random.nextInt(20));
                            metricService.save(metric);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }, executor);
                    futures.add(future);
                }

                Thread.sleep(1000 / rps); // Control request rate
            }

            // Wait for all futures to complete
            List<Boolean> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            successCount = (int) results.stream().filter(b -> b).count();
            errorCount = totalRequests - successCount;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double avgThroughput = (totalRequests * 1000.0) / totalTime;

        result.put("status", "completed");
        result.put("duration_ms", totalTime);
        result.put("total_requests", totalRequests);
        result.put("successful_requests", successCount);
        result.put("failed_requests", errorCount);
        result.put("success_rate", ((double) successCount / totalRequests) * 100);
        result.put("avg_throughput_rps", avgThroughput);

        return ResponseEntity.ok(result);
    }

    /**
     * Stress test - gradually increase load
     * Usage: POST /api/load-test/stress?maxRps=50&increment=5&duration=300
     */
    @PostMapping("/stress")
    public ResponseEntity<Map<String, Object>> stressTest(
            @RequestParam(defaultValue = "50") int maxRps,
            @RequestParam(defaultValue = "5") int increment,
            @RequestParam(defaultValue = "300") int duration) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> phases = new ArrayList<>();
        int totalRequests = 0;
        int totalSuccess = 0;

        long startTime = System.currentTimeMillis();

        for (int currentRps = 5; currentRps <= maxRps; currentRps += increment) {
            long phaseStart = System.currentTimeMillis();
            int phaseRequests = 0;
            int phaseSuccess = 0;

            try {
                while (System.currentTimeMillis() - phaseStart < 30000) { // 30 seconds per phase
                    for (int i = 0; i < currentRps; i++) {
                        phaseRequests++;
                        totalRequests++;

                        Metric metric = new Metric();
                        metric.setCpu(40 + new Random().nextInt(40));
                        metric.setTemperature(60 + new Random().nextInt(30));
                        metricService.save(metric);
                        phaseSuccess++;
                        totalSuccess++;
                    }
                    Thread.sleep(1000 / currentRps);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Map<String, Object> phase = new HashMap<>();
            phase.put("rps", currentRps);
            phase.put("requests", phaseRequests);
            phase.put("success", phaseSuccess);
            phase.put("success_rate", ((double) phaseSuccess / Math.max(phaseRequests, 1)) * 100);
            phases.add(phase);
        }

        long totalTime = System.currentTimeMillis() - startTime;

        result.put("test_type", "stress");
        result.put("total_duration_ms", totalTime);
        result.put("total_requests", totalRequests);
        result.put("total_success", totalSuccess);
        result.put("overall_success_rate", ((double) totalSuccess / Math.max(totalRequests, 1)) * 100);
        result.put("phases", phases);

        return ResponseEntity.ok(result);
    }

    /**
     * Get current system metrics for comparison
     */
    @GetMapping("/metrics-summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        java.util.List<Metric> allMetrics = metricService.findAll();
        
        Map<String, Object> summary = new HashMap<>();
        if (!allMetrics.isEmpty()) {
            double avgCpu = allMetrics.stream().mapToDouble(Metric::getCpu).average().orElse(0.0);
            double avgTemp = allMetrics.stream().mapToDouble(Metric::getTemperature).average().orElse(0.0);
            double maxCpu = allMetrics.stream().mapToDouble(Metric::getCpu).max().orElse(0.0);
            double maxTemp = allMetrics.stream().mapToDouble(Metric::getTemperature).max().orElse(0.0);

            summary.put("total_metrics", allMetrics.size());
            summary.put("avg_cpu", String.format("%.2f", avgCpu));
            summary.put("avg_temperature", String.format("%.2f", avgTemp));
            summary.put("max_cpu", maxCpu);
            summary.put("max_temperature", maxTemp);
        }
        
        return ResponseEntity.ok(summary);
    }
}
