package com.dinesh.backend.cloud_monitoring_and_autohealing_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudMonitoringAndAutoHealingPlatformApplication {

    public String checkCpu(int cpu) {
        if (cpu > 50) {
            return "⚠️ Warning: CPU usage is high!";
        }
        return "CPU is normal";
    }
    public static void main(String[] args) {
        SpringApplication.run(CloudMonitoringAndAutoHealingPlatformApplication.class, args);
    }

}
