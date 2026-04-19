package com.dinesh.backend.cloud_monitoring_and_autohealing_platform.service;

import org.springframework.stereotype.Service;

@Service
public class CpuUsageService {

    public String getCpuUsage(int usage) {
        if (usage > 50) {
            return " Warning";
        }
        return "Normal";
    }
}