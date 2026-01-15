package com.safetyhub.adapter.simulator.robot;

import lombok.Builder;
import lombok.Getter;

/**
 * 웨어러블 기기 배터리 상태 시뮬레이션
 */
@Getter
@Builder
public class BatteryStatus {

    private int level;           // 배터리 잔량 (0-100%)
    private boolean charging;    // 충전 중 여부

    private static final int DRAIN_RATE_WORKING = 2;   // 작업 중 소모율 (/hour)
    private static final int DRAIN_RATE_IDLE = 1;      // 대기 중 소모율 (/hour)
    private static final int CHARGE_RATE = 20;         // 충전율 (/hour)

    /**
     * 완충 배터리 생성
     */
    public static BatteryStatus createFull() {
        return BatteryStatus.builder()
                .level(100)
                .charging(false)
                .build();
    }

    /**
     * 랜덤 배터리 상태 생성 (50-100%)
     */
    public static BatteryStatus createRandom() {
        return BatteryStatus.builder()
                .level(50 + (int) (Math.random() * 50))
                .charging(false)
                .build();
    }

    /**
     * 배터리 소모 (작업 중)
     */
    public BatteryStatus drainWorking(int minutes) {
        int drain = (DRAIN_RATE_WORKING * minutes) / 60;
        return BatteryStatus.builder()
                .level(Math.max(0, level - drain))
                .charging(false)
                .build();
    }

    /**
     * 배터리 소모 (대기 중)
     */
    public BatteryStatus drainIdle(int minutes) {
        int drain = (DRAIN_RATE_IDLE * minutes) / 60;
        return BatteryStatus.builder()
                .level(Math.max(0, level - drain))
                .charging(false)
                .build();
    }

    /**
     * 배터리 충전
     */
    public BatteryStatus charge(int minutes) {
        int charged = (CHARGE_RATE * minutes) / 60;
        return BatteryStatus.builder()
                .level(Math.min(100, level + charged))
                .charging(true)
                .build();
    }

    /**
     * 배터리 부족 여부 (20% 이하)
     */
    public boolean isLow() {
        return level <= 20;
    }

    /**
     * 배터리 위험 여부 (10% 이하)
     */
    public boolean isCritical() {
        return level <= 10;
    }
}
