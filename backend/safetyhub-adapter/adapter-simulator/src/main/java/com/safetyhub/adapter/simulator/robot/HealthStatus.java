package com.safetyhub.adapter.simulator.robot;

import lombok.Builder;
import lombok.Getter;

import java.util.Random;

/**
 * 로봇 작업자 건강 상태 시뮬레이션
 */
@Getter
@Builder
public class HealthStatus {

    private int heartRate;        // 심박수 (bpm)
    private double temperature;   // 체온 (°C)
    private int oxygenLevel;      // 산소포화도 (%)
    private int stressLevel;      // 스트레스 레벨 (0-100)
    private boolean fallen;       // 낙상 여부

    private static final Random random = new Random();

    /**
     * 정상 건강 상태 생성
     */
    public static HealthStatus createNormal() {
        return HealthStatus.builder()
                .heartRate(70 + random.nextInt(20))      // 70-90 bpm
                .temperature(36.5 + random.nextDouble() * 0.5)  // 36.5-37.0°C
                .oxygenLevel(96 + random.nextInt(4))      // 96-99%
                .stressLevel(random.nextInt(30))          // 0-30
                .fallen(false)
                .build();
    }

    /**
     * 작업 중 건강 상태 (약간 상승)
     */
    public static HealthStatus createWorking() {
        return HealthStatus.builder()
                .heartRate(80 + random.nextInt(30))      // 80-110 bpm
                .temperature(36.8 + random.nextDouble() * 0.7)  // 36.8-37.5°C
                .oxygenLevel(95 + random.nextInt(4))      // 95-98%
                .stressLevel(20 + random.nextInt(40))     // 20-60
                .fallen(false)
                .build();
    }

    /**
     * 위험 건강 상태 생성 (긴급 상황)
     */
    public static HealthStatus createDanger() {
        int type = random.nextInt(3);
        return switch (type) {
            case 0 -> // 심박 이상
                    HealthStatus.builder()
                            .heartRate(130 + random.nextInt(50))  // 130-180 bpm
                            .temperature(37.0 + random.nextDouble())
                            .oxygenLevel(90 + random.nextInt(5))
                            .stressLevel(80 + random.nextInt(20))
                            .fallen(false)
                            .build();
            case 1 -> // 체온 이상
                    HealthStatus.builder()
                            .heartRate(90 + random.nextInt(30))
                            .temperature(38.5 + random.nextDouble() * 1.5)  // 38.5-40°C
                            .oxygenLevel(92 + random.nextInt(5))
                            .stressLevel(70 + random.nextInt(20))
                            .fallen(false)
                            .build();
            default -> // 낙상
                    HealthStatus.builder()
                            .heartRate(100 + random.nextInt(40))
                            .temperature(36.5 + random.nextDouble())
                            .oxygenLevel(93 + random.nextInt(5))
                            .stressLevel(90 + random.nextInt(10))
                            .fallen(true)
                            .build();
        };
    }

    /**
     * 건강 상태가 위험한지 확인
     */
    public boolean isDangerous() {
        return heartRate > 120 || heartRate < 50
                || temperature > 38.0 || temperature < 35.5
                || oxygenLevel < 92
                || stressLevel > 80
                || fallen;
    }

    /**
     * 건강 상태 레벨 반환
     */
    public HealthLevel getLevel() {
        if (fallen) return HealthLevel.CRITICAL;
        if (isDangerous()) return HealthLevel.DANGER;
        if (heartRate > 100 || stressLevel > 60) return HealthLevel.WARNING;
        return HealthLevel.NORMAL;
    }

    public enum HealthLevel {
        NORMAL, WARNING, DANGER, CRITICAL
    }
}
