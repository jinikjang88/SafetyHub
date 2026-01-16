package com.safetyhub.application.charging;

import com.safetyhub.core.domain.Location;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 충전 요청
 */
@Getter
@Builder
@ToString
public class ChargingRequest {

    /**
     * 요청 ID
     */
    private final String requestId;

    /**
     * 로봇 ID
     */
    private final String robotId;

    /**
     * 현재 배터리 레벨 (%)
     */
    private final int currentBatteryLevel;

    /**
     * 목표 배터리 레벨 (%)
     */
    private final int targetBatteryLevel;

    /**
     * 로봇의 현재 위치
     */
    private final Location currentLocation;

    /**
     * 우선순위
     */
    private final ChargingPriority priority;

    /**
     * 요청 생성 시간
     */
    private final LocalDateTime requestedAt;

    /**
     * 할당된 충전소 ID (할당 전에는 null)
     */
    private final String assignedStationId;

    /**
     * 요청 상태
     */
    private final RequestStatus status;

    /**
     * 충전 우선순위
     */
    public enum ChargingPriority {
        /**
         * 긴급 (배터리 < 20%)
         */
        URGENT(0),

        /**
         * 높음 (배터리 20-50%)
         */
        HIGH(1),

        /**
         * 보통 (배터리 50-80%)
         */
        NORMAL(2),

        /**
         * 낮음 (배터리 > 80%)
         */
        LOW(3);

        private final int level;

        ChargingPriority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        /**
         * 배터리 레벨로부터 우선순위 결정
         */
        public static ChargingPriority fromBatteryLevel(int batteryLevel) {
            if (batteryLevel < 20) {
                return URGENT;
            } else if (batteryLevel < 50) {
                return HIGH;
            } else if (batteryLevel < 80) {
                return NORMAL;
            } else {
                return LOW;
            }
        }

        public int compareTo(ChargingPriority other) {
            return Integer.compare(this.level, other.level);
        }
    }

    /**
     * 요청 상태
     */
    public enum RequestStatus {
        /**
         * 대기 중
         */
        PENDING,

        /**
         * 충전소 할당됨
         */
        ASSIGNED,

        /**
         * 충전 중
         */
        CHARGING,

        /**
         * 완료
         */
        COMPLETED,

        /**
         * 취소됨
         */
        CANCELLED
    }

    /**
     * 빌더 클래스 커스터마이징
     */
    public static class ChargingRequestBuilder {
        public ChargingRequest build() {
            if (requestId == null || requestId.isEmpty()) {
                requestId = java.util.UUID.randomUUID().toString();
            }
            if (requestedAt == null) {
                requestedAt = LocalDateTime.now();
            }
            if (status == null) {
                status = RequestStatus.PENDING;
            }
            if (priority == null) {
                priority = ChargingPriority.fromBatteryLevel(currentBatteryLevel);
            }
            if (targetBatteryLevel <= 0) {
                targetBatteryLevel = 100; // 기본값: 100%까지 충전
            }

            // 필수 필드 검증
            if (robotId == null || robotId.isEmpty()) {
                throw new IllegalArgumentException("로봇 ID는 필수입니다");
            }
            if (currentBatteryLevel < 0 || currentBatteryLevel > 100) {
                throw new IllegalArgumentException("배터리 레벨은 0-100 사이여야 합니다");
            }

            return new ChargingRequest(requestId, robotId, currentBatteryLevel, targetBatteryLevel,
                    currentLocation, priority, requestedAt, assignedStationId, status);
        }
    }

    /**
     * 충전소 할당
     */
    public ChargingRequest assignStation(String stationId) {
        if (stationId == null || stationId.isEmpty()) {
            throw new IllegalArgumentException("충전소 ID는 필수입니다");
        }
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 요청만 할당할 수 있습니다. 현재 상태: " + status);
        }

        return ChargingRequest.builder()
                .requestId(requestId)
                .robotId(robotId)
                .currentBatteryLevel(currentBatteryLevel)
                .targetBatteryLevel(targetBatteryLevel)
                .currentLocation(currentLocation)
                .priority(priority)
                .requestedAt(requestedAt)
                .assignedStationId(stationId)
                .status(RequestStatus.ASSIGNED)
                .build();
    }

    /**
     * 충전 시작
     */
    public ChargingRequest startCharging() {
        if (status != RequestStatus.ASSIGNED) {
            throw new IllegalStateException("ASSIGNED 상태의 요청만 충전을 시작할 수 있습니다. 현재 상태: " + status);
        }

        return ChargingRequest.builder()
                .requestId(requestId)
                .robotId(robotId)
                .currentBatteryLevel(currentBatteryLevel)
                .targetBatteryLevel(targetBatteryLevel)
                .currentLocation(currentLocation)
                .priority(priority)
                .requestedAt(requestedAt)
                .assignedStationId(assignedStationId)
                .status(RequestStatus.CHARGING)
                .build();
    }

    /**
     * 충전 완료
     */
    public ChargingRequest complete() {
        if (status != RequestStatus.CHARGING) {
            throw new IllegalStateException("CHARGING 상태의 요청만 완료할 수 있습니다. 현재 상태: " + status);
        }

        return ChargingRequest.builder()
                .requestId(requestId)
                .robotId(robotId)
                .currentBatteryLevel(currentBatteryLevel)
                .targetBatteryLevel(targetBatteryLevel)
                .currentLocation(currentLocation)
                .priority(priority)
                .requestedAt(requestedAt)
                .assignedStationId(assignedStationId)
                .status(RequestStatus.COMPLETED)
                .build();
    }

    /**
     * 요청 취소
     */
    public ChargingRequest cancel() {
        if (status == RequestStatus.COMPLETED || status == RequestStatus.CANCELLED) {
            throw new IllegalStateException("이미 완료되거나 취소된 요청입니다. 현재 상태: " + status);
        }

        return ChargingRequest.builder()
                .requestId(requestId)
                .robotId(robotId)
                .currentBatteryLevel(currentBatteryLevel)
                .targetBatteryLevel(targetBatteryLevel)
                .currentLocation(currentLocation)
                .priority(priority)
                .requestedAt(requestedAt)
                .assignedStationId(assignedStationId)
                .status(RequestStatus.CANCELLED)
                .build();
    }
}
