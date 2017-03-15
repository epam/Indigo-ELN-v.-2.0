package com.epam.indigoeln.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.temporal.ChronoUnit;

@Configuration
@ConfigurationProperties("indigoeln.dashboard")
public class DashboardProperties {

    private Integer thresholdLevel;
    private ChronoUnit thresholdUnit;

    public Integer getThresholdLevel() {
        return thresholdLevel;
    }

    public void setThresholdLevel(Integer thresholdLevel) {
        this.thresholdLevel = thresholdLevel;
    }

    public ChronoUnit getThresholdUnit() {
        return thresholdUnit;
    }

    public void setThresholdUnit(ChronoUnit thresholdUnit) {
        this.thresholdUnit = thresholdUnit;
    }
}
