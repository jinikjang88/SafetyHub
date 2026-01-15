package com.safetyhub.simulator.core;

/**
 * 로봇 작업자의 건강 상태
 */
public enum HealthStatus {
    NORMAL("정상"),
    WARNING("주의"),
    DANGER("위험"),
    CRITICAL("치명적");

    private final String description;

    HealthStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEmergency() {
        return this == DANGER || this == CRITICAL;
    }
}
