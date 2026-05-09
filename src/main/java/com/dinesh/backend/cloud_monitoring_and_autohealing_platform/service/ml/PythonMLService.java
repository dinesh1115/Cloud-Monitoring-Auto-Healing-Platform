package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service.ml;

import com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model.Metric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Service for integrating with Python ML-based anomaly detection
 */
@Service
public class PythonMLService {

    private final ObjectMapper objectMapper;

    @Value("${app.python.enabled:false}")
    private boolean pythonEnabled;

    @Value("${app.python.script.path:python/ml_anomaly_detector.py}")
    private String pythonScriptPath;

    public PythonMLService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Detect anomalies using Python ML model
     */
    public MLAnomalyResult detectAnomaly(Metric metric) {
        if (!pythonEnabled) {
            return new MLAnomalyResult(false, 0.0, 0.0, 0);
        }

        try {
            // Prepare input data
            Map<String, Object> inputData = Map.of(
                "cpu", metric.getCpu(),
                "temperature", metric.getTemperature()
            );

            String jsonInput = objectMapper.writeValueAsString(inputData);

            // Execute Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python3",
                getScriptAbsolutePath(),
                "detect",
                jsonInput
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Parse JSON response
                Map<String, Object> result = objectMapper.readValue(output.toString(), Map.class);
                return new MLAnomalyResult(
                    (Boolean) result.get("is_anomaly"),
                    ((Number) result.get("score")).doubleValue(),
                    ((Number) result.get("confidence")).doubleValue(),
                    ((Number) result.get("prediction")).intValue()
                );
            } else {
                System.err.println("Python ML script failed with exit code: " + exitCode);
                System.err.println("Output: " + output.toString());
                return new MLAnomalyResult(false, 0.0, 0.0, 0);
            }

        } catch (Exception e) {
            System.err.println("Error calling Python ML service: " + e.getMessage());
            return new MLAnomalyResult(false, 0.0, 0.0, 0);
        }
    }

    /**
     * Train the ML model with historical data
     */
    public boolean trainModel(java.util.List<Metric> metrics) {
        if (!pythonEnabled) {
            return false;
        }

        try {
            // Convert metrics to JSON format
            java.util.List<Map<String, Object>> trainingData = metrics.stream()
                .map(metric -> Map.<String, Object>of(
                    "cpu", metric.getCpu(),
                    "temperature", metric.getTemperature()
                ))
                .toList();

            String jsonData = objectMapper.writeValueAsString(trainingData);

            // Write to temporary file
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("training_data", ".json");
            java.nio.file.Files.writeString(tempFile, jsonData);

            // Execute training
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python3",
                getScriptAbsolutePath(),
                "train",
                tempFile.toString()
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // Clean up temp file
            java.nio.file.Files.deleteIfExists(tempFile);

            if (exitCode == 0) {
                System.out.println("Python ML model training completed successfully");
                return true;
            } else {
                System.err.println("Python ML model training failed with exit code: " + exitCode);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error training Python ML model: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get ML model information
     */
    public Map<String, Object> getModelInfo() {
        if (!pythonEnabled) {
            return Map.of("enabled", false, "message", "Python ML integration is disabled");
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python3",
                getScriptAbsolutePath(),
                "info"
            );

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return objectMapper.readValue(output.toString(), Map.class);
            } else {
                return Map.of("error", "Failed to get model info", "exit_code", exitCode);
            }

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    private String getScriptAbsolutePath() {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        return projectRoot.resolve(pythonScriptPath).toString();
    }

    /**
     * Result class for ML anomaly detection
     */
    public static class MLAnomalyResult {
        private final boolean isAnomaly;
        private final double score;
        private final double confidence;
        private final int prediction;

        public MLAnomalyResult(boolean isAnomaly, double score, double confidence, int prediction) {
            this.isAnomaly = isAnomaly;
            this.score = score;
            this.confidence = confidence;
            this.prediction = prediction;
        }

        public boolean isAnomaly() {
            return isAnomaly;
        }

        public double getScore() {
            return score;
        }

        public double getConfidence() {
            return confidence;
        }

        public int getPrediction() {
            return prediction;
        }
    }
}
