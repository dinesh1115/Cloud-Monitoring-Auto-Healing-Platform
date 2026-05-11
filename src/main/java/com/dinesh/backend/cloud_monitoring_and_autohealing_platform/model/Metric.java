package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model;

import java.time.Instant;

public class Metric {

    private Long id;
    private double cpu;
    private double temperature;
    private double memoryUsage;
    private double diskUsage;
    private Instant timestamp;

    public Metric() {
        this.timestamp = Instant.now();
    }

    public Metric(Long id, double cpu, double temperature, double memoryUsage, double diskUsage, Instant timestamp) {
        this.id = id;
        this.cpu = cpu;
        this.temperature = temperature;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
