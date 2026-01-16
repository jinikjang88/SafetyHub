package com.safetyhub.application.charging;

import com.safetyhub.core.domain.Location;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 충전소 도메인 모델
 */
@Getter
@Builder
@ToString
public class ChargingStation {

    /**
     * 충전소 ID
     */
    private final String stationId;

    /**
     * 충전소 이름
     */
    private final String name;

    /**
     * 충전소 위치
     */
    private final Location location;

    /**
     * 충전소가 속한 구역 ID
     */
    private final String zoneId;

    /**
     * 총 충전 슬롯 수
     */
    private final int totalSlots;

    /**
     * 현재 사용 중인 슬롯에 있는 로봇 ID 집합
     */
    private final Set<String> occupiedSlots;

    /**
     * 충전소 상태
     */
    private final StationStatus status;

    /**
     * 충전 속도 (% per minute)
     */
    private final double chargingSpeed;

    /**
     * 마지막 업데이트 시간
     */
    private final LocalDateTime lastUpdatedAt;

    /**
     * 충전소 상태 enum
     */
    public enum StationStatus {
        /**
         * 정상 운영
         */
        OPERATIONAL,

        /**
         * 유지보수 중
         */
        MAINTENANCE,

        /**
         * 고장
         */
        BROKEN,

        /**
         * 오프라인
         */
        OFFLINE
    }

    /**
     * 빌더 클래스 커스터마이징
     */
    public static class ChargingStationBuilder {
        public ChargingStation build() {
            if (occupiedSlots == null) {
                occupiedSlots = new HashSet<>();
            }
            if (status == null) {
                status = StationStatus.OPERATIONAL;
            }
            if (chargingSpeed <= 0) {
                chargingSpeed = 10.0; // 기본값: 분당 10%
            }
            if (lastUpdatedAt == null) {
                lastUpdatedAt = LocalDateTime.now();
            }

            // 필수 필드 검증
            if (stationId == null || stationId.isEmpty()) {
                throw new IllegalArgumentException("충전소 ID는 필수입니다");
            }
            if (totalSlots <= 0) {
                throw new IllegalArgumentException("총 슬롯 수는 0보다 커야 합니다");
            }

            return new ChargingStation(stationId, name, location, zoneId, totalSlots,
                    occupiedSlots, status, chargingSpeed, lastUpdatedAt);
        }
    }

    /**
     * 사용 가능한 슬롯 수
     */
    public int getAvailableSlots() {
        return totalSlots - occupiedSlots.size();
    }

    /**
     * 사용 중인 슬롯 수
     */
    public int getOccupiedSlotsCount() {
        return occupiedSlots.size();
    }

    /**
     * 슬롯이 사용 가능한지 확인
     */
    public boolean hasAvailableSlot() {
        return getAvailableSlots() > 0 && status == StationStatus.OPERATIONAL;
    }

    /**
     * 충전소가 운영 중인지 확인
     */
    public boolean isOperational() {
        return status == StationStatus.OPERATIONAL;
    }

    /**
     * 로봇이 이 충전소에서 충전 중인지 확인
     */
    public boolean isRobotCharging(String robotId) {
        return occupiedSlots.contains(robotId);
    }

    /**
     * 충전 슬롯 점유 (로봇이 충전 시작)
     */
    public ChargingStation occupySlot(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            throw new IllegalArgumentException("로봇 ID는 필수입니다");
        }
        if (!hasAvailableSlot()) {
            throw new IllegalStateException("사용 가능한 충전 슬롯이 없습니다. Station: " + stationId);
        }
        if (occupiedSlots.contains(robotId)) {
            throw new IllegalStateException("로봇이 이미 충전 중입니다. Robot: " + robotId);
        }

        Set<String> newOccupiedSlots = new HashSet<>(occupiedSlots);
        newOccupiedSlots.add(robotId);

        return ChargingStation.builder()
                .stationId(stationId)
                .name(name)
                .location(location)
                .zoneId(zoneId)
                .totalSlots(totalSlots)
                .occupiedSlots(newOccupiedSlots)
                .status(status)
                .chargingSpeed(chargingSpeed)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 충전 슬롯 해제 (로봇이 충전 완료)
     */
    public ChargingStation releaseSlot(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            throw new IllegalArgumentException("로봇 ID는 필수입니다");
        }
        if (!occupiedSlots.contains(robotId)) {
            throw new IllegalStateException("로봇이 이 충전소에서 충전 중이 아닙니다. Robot: " + robotId);
        }

        Set<String> newOccupiedSlots = new HashSet<>(occupiedSlots);
        newOccupiedSlots.remove(robotId);

        return ChargingStation.builder()
                .stationId(stationId)
                .name(name)
                .location(location)
                .zoneId(zoneId)
                .totalSlots(totalSlots)
                .occupiedSlots(newOccupiedSlots)
                .status(status)
                .chargingSpeed(chargingSpeed)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 충전 시간 예측 (분)
     * @param currentBattery 현재 배터리 레벨 (%)
     * @param targetBattery 목표 배터리 레벨 (%)
     * @return 예상 충전 시간 (분)
     */
    public double estimateChargingTime(int currentBattery, int targetBattery) {
        if (currentBattery < 0 || currentBattery > 100 ||
            targetBattery < 0 || targetBattery > 100) {
            throw new IllegalArgumentException("배터리 레벨은 0-100 사이여야 합니다");
        }
        if (currentBattery >= targetBattery) {
            return 0.0;
        }

        int batteryToCharge = targetBattery - currentBattery;
        return batteryToCharge / chargingSpeed;
    }

    /**
     * 충전소 상태 변경
     */
    public ChargingStation changeStatus(StationStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("충전소 상태는 null일 수 없습니다");
        }

        return ChargingStation.builder()
                .stationId(stationId)
                .name(name)
                .location(location)
                .zoneId(zoneId)
                .totalSlots(totalSlots)
                .occupiedSlots(occupiedSlots)
                .status(newStatus)
                .chargingSpeed(chargingSpeed)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }
}
