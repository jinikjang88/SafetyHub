package com.safetyhub.adapter.simulator.robot;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 로봇 작업자 (시뮬레이션용 더미)
 * 실제 작업자를 시뮬레이션하여 SafetyHub 시스템 테스트
 */
@Slf4j
@Getter
@Builder
public class RobotWorker {

    private final String robotId;
    private final String name;
    private String assignedZoneId;

    private RobotState state;
    private Position currentPosition;
    private Position targetPosition;
    private String currentZoneId;

    private RobotSchedule schedule;
    private HealthStatus healthStatus;
    private BatteryStatus batteryStatus;

    private LocalDateTime lastUpdate;
    private LocalDateTime createdAt;

    /**
     * 새 로봇 작업자 생성
     */
    public static RobotWorker create(String name, String assignedZoneId, Position initialPosition) {
        return RobotWorker.builder()
                .robotId("ROBOT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .name(name)
                .assignedZoneId(assignedZoneId)
                .state(RobotState.OFFLINE)
                .currentPosition(initialPosition)
                .targetPosition(null)
                .currentZoneId(assignedZoneId)
                .schedule(RobotSchedule.createDefaultSchedule())
                .healthStatus(HealthStatus.createNormal())
                .batteryStatus(BatteryStatus.createRandom())
                .lastUpdate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 상태 업데이트
     */
    public RobotWorker updateState(RobotState newState) {
        log.debug("Robot {} state changed: {} -> {}", robotId, state, newState);
        return RobotWorker.builder()
                .robotId(robotId)
                .name(name)
                .assignedZoneId(assignedZoneId)
                .state(newState)
                .currentPosition(currentPosition)
                .targetPosition(targetPosition)
                .currentZoneId(currentZoneId)
                .schedule(schedule)
                .healthStatus(healthStatus)
                .batteryStatus(batteryStatus)
                .lastUpdate(LocalDateTime.now())
                .createdAt(createdAt)
                .build();
    }

    /**
     * 위치 업데이트
     */
    public RobotWorker updatePosition(Position newPosition, String newZoneId) {
        log.debug("Robot {} moved: {} -> {} (Zone: {})", robotId, currentPosition, newPosition, newZoneId);
        return RobotWorker.builder()
                .robotId(robotId)
                .name(name)
                .assignedZoneId(assignedZoneId)
                .state(state)
                .currentPosition(newPosition)
                .targetPosition(targetPosition)
                .currentZoneId(newZoneId)
                .schedule(schedule)
                .healthStatus(healthStatus)
                .batteryStatus(batteryStatus)
                .lastUpdate(LocalDateTime.now())
                .createdAt(createdAt)
                .build();
    }

    /**
     * 목표 위치 설정
     */
    public RobotWorker setTarget(Position target) {
        return RobotWorker.builder()
                .robotId(robotId)
                .name(name)
                .assignedZoneId(assignedZoneId)
                .state(RobotState.MOVING)
                .currentPosition(currentPosition)
                .targetPosition(target)
                .currentZoneId(currentZoneId)
                .schedule(schedule)
                .healthStatus(healthStatus)
                .batteryStatus(batteryStatus)
                .lastUpdate(LocalDateTime.now())
                .createdAt(createdAt)
                .build();
    }

    /**
     * 건강 상태 업데이트
     */
    public RobotWorker updateHealth(HealthStatus newHealth) {
        return RobotWorker.builder()
                .robotId(robotId)
                .name(name)
                .assignedZoneId(assignedZoneId)
                .state(state)
                .currentPosition(currentPosition)
                .targetPosition(targetPosition)
                .currentZoneId(currentZoneId)
                .schedule(schedule)
                .healthStatus(newHealth)
                .batteryStatus(batteryStatus)
                .lastUpdate(LocalDateTime.now())
                .createdAt(createdAt)
                .build();
    }

    /**
     * 배터리 상태 업데이트
     */
    public RobotWorker updateBattery(BatteryStatus newBattery) {
        return RobotWorker.builder()
                .robotId(robotId)
                .name(name)
                .assignedZoneId(assignedZoneId)
                .state(state)
                .currentPosition(currentPosition)
                .targetPosition(targetPosition)
                .currentZoneId(currentZoneId)
                .schedule(schedule)
                .healthStatus(healthStatus)
                .batteryStatus(newBattery)
                .lastUpdate(LocalDateTime.now())
                .createdAt(createdAt)
                .build();
    }

    /**
     * 긴급 상황 발생
     */
    public RobotWorker triggerEmergency() {
        log.warn("EMERGENCY triggered for Robot {}", robotId);
        return RobotWorker.builder()
                .robotId(robotId)
                .name(name)
                .assignedZoneId(assignedZoneId)
                .state(RobotState.EMERGENCY)
                .currentPosition(currentPosition)
                .targetPosition(null)
                .currentZoneId(currentZoneId)
                .schedule(schedule)
                .healthStatus(HealthStatus.createDanger())
                .batteryStatus(batteryStatus)
                .lastUpdate(LocalDateTime.now())
                .createdAt(createdAt)
                .build();
    }

    /**
     * 대피 시작
     */
    public RobotWorker startEvacuation(Position evacuationPoint) {
        log.warn("Robot {} starting evacuation to {}", robotId, evacuationPoint);
        return RobotWorker.builder()
                .robotId(robotId)
                .name(name)
                .assignedZoneId(assignedZoneId)
                .state(RobotState.EVACUATING)
                .currentPosition(currentPosition)
                .targetPosition(evacuationPoint)
                .currentZoneId(currentZoneId)
                .schedule(schedule)
                .healthStatus(healthStatus)
                .batteryStatus(batteryStatus)
                .lastUpdate(LocalDateTime.now())
                .createdAt(createdAt)
                .build();
    }

    /**
     * 현재 스케줄에 따른 상태 결정
     */
    public RobotState getScheduledState(LocalTime currentTime) {
        RobotSchedule.ScheduleEntry entry = schedule.getCurrentEntry(currentTime);
        if (entry == null) {
            return RobotState.OFFLINE;
        }
        return entry.getActivity();
    }

    /**
     * 목표에 도착했는지 확인
     */
    public boolean hasReachedTarget() {
        if (targetPosition == null) return true;
        return currentPosition.equals(targetPosition);
    }

    /**
     * 이동이 필요한지 확인
     */
    public boolean needsToMove() {
        return targetPosition != null && !currentPosition.equals(targetPosition);
    }

    /**
     * 온라인 상태인지 확인
     */
    public boolean isOnline() {
        return state != RobotState.OFFLINE;
    }

    /**
     * 위험 상태인지 확인
     */
    public boolean isInDanger() {
        return state == RobotState.EMERGENCY
                || (healthStatus != null && healthStatus.isDangerous());
    }

    @Override
    public String toString() {
        return String.format("RobotWorker[%s, state=%s, pos=%s, zone=%s]",
                robotId, state, currentPosition, currentZoneId);
    }
}
