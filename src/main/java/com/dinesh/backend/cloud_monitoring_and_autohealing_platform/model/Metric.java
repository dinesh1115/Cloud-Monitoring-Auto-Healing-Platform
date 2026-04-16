package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.model;

import java.time.Instant;

public class Metric {

    private Long id;
    private int cpu;
    private int temperature;
    private Instant timestamp;

    public Metric() {
        this.timestamp = Instant.now();
    }

    public Metric(Long id, int cpu, int temperature, Instant timestamp) {
        this.id = id;
        this.cpu = cpu;
        this.temperature = temperature;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
