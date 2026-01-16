package com.safetyhub.application.charging;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 배터리 상태 정보
 */
@Getter
@Builder
@ToString
public class BatteryStatus {

    /**
     * 로봇 ID
     */
    private final String robotId;

    /**
     * 배터리 레벨 (0-100%)
     */
    private final int batteryLevel;

    /**
     * 배터리 상태
     */
    private final BatteryState state;

    /**
     * 예상 충전 필요 시간 (초)
     * -1이면 알 수 없음
     */
    private final long estimatedTimeToChargingNeeded;

    /**
     * 마지막 업데이트 시간
     */
    private final LocalDateTime lastUpdatedAt;

    /**
     * 배터리 상태 enum
     */
    public enum BatteryState {
        /**
         * 정상 (> 50%)
         */
        HEALTHY,

        /**
         * 주의 (20-50%)
         */
        WARNING,

        /**
         * 위험 (< 20%)
         */
        CRITICAL,

        /**
         * 충전 중
         */
        CHARGING,

        /**
         * 알 수 없음
         */
        UNKNOWN
    }

    /**
     * 배터리 레벨로부터 상태 결정
     */
    public static BatteryState determineState(int batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100) {
            return BatteryState.UNKNOWN;
        }
        if (batteryLevel > 50) {
            return BatteryState.HEALTHY;
        }
        if (batteryLevel >= 20) {
            return BatteryState.WARNING;
        }
        return BatteryState.CRITICAL;
    }

    /**
     * 충전이 필요한지 확인
     */
    public boolean needsCharging() {
        return state == BatteryState.CRITICAL || state == BatteryState.WARNING;
    }

    /**
     * 긴급 충전이 필요한지 확인
     */
    public boolean needsUrgentCharging() {
        return state == BatteryState.CRITICAL;
    }

    /**
     * 충전 중인지 확인
     */
    public boolean isCharging() {
        return state == BatteryState.CHARGING;
    }
}
