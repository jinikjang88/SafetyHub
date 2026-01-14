package com.safetyhub.adapter.simulator.event;

import com.safetyhub.adapter.simulator.robot.Position;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 시뮬레이션 이벤트
 */
@Getter
@Builder
public class SimulationEvent {

    private final String eventId;
    private final EventType type;
    private final String robotId;
    private final String zoneId;
    private final Position position;
    private final EventPriority priority;
    private final Map<String, Object> data;
    private final LocalDateTime timestamp;

    public enum EventType {
        // 위치 이벤트
        LOCATION_UPDATE,
        ZONE_ENTERED,
        ZONE_EXITED,

        // 상태 이벤트
        STATE_CHANGED,
        HEALTH_UPDATE,
        BATTERY_LOW,

        // 센서 이벤트
        SENSOR_DATA,
        HEARTBEAT,

        // 긴급 이벤트
        EMERGENCY_DETECTED,
        FALL_DETECTED,
        SOS_TRIGGERED,
        HEALTH_CRITICAL,

        // 시스템 이벤트
        EVACUATION_STARTED,
        EVACUATION_COMPLETED,
        SIMULATION_STARTED,
        SIMULATION_STOPPED
    }

    public enum EventPriority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }

    /**
     * 위치 업데이트 이벤트 생성
     */
    public static SimulationEvent locationUpdate(String robotId, Position position, String zoneId) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.LOCATION_UPDATE)
                .robotId(robotId)
                .zoneId(zoneId)
                .position(position)
                .priority(EventPriority.LOW)
                .data(Map.of(
                        "latitude", position.toLatitude(),
                        "longitude", position.toLongitude()
                ))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 구역 진입 이벤트 생성
     */
    public static SimulationEvent zoneEntered(String robotId, String zoneId, Position position) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.ZONE_ENTERED)
                .robotId(robotId)
                .zoneId(zoneId)
                .position(position)
                .priority(EventPriority.NORMAL)
                .data(Map.of("action", "entered"))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 구역 퇴장 이벤트 생성
     */
    public static SimulationEvent zoneExited(String robotId, String zoneId, Position position) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.ZONE_EXITED)
                .robotId(robotId)
                .zoneId(zoneId)
                .position(position)
                .priority(EventPriority.NORMAL)
                .data(Map.of("action", "exited"))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 상태 변경 이벤트 생성
     */
    public static SimulationEvent stateChanged(String robotId, String oldState, String newState) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.STATE_CHANGED)
                .robotId(robotId)
                .priority(EventPriority.NORMAL)
                .data(Map.of(
                        "previousState", oldState,
                        "currentState", newState
                ))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 센서 데이터 이벤트 생성
     */
    public static SimulationEvent sensorData(String robotId, Map<String, Object> sensorValues) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.SENSOR_DATA)
                .robotId(robotId)
                .priority(EventPriority.LOW)
                .data(sensorValues)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 하트비트 이벤트 생성
     */
    public static SimulationEvent heartbeat(String robotId) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.HEARTBEAT)
                .robotId(robotId)
                .priority(EventPriority.LOW)
                .data(Map.of("status", "alive"))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 긴급 상황 이벤트 생성
     */
    public static SimulationEvent emergency(String robotId, String zoneId, Position position,
                                            String emergencyType, String description) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.EMERGENCY_DETECTED)
                .robotId(robotId)
                .zoneId(zoneId)
                .position(position)
                .priority(EventPriority.CRITICAL)
                .data(Map.of(
                        "emergencyType", emergencyType,
                        "description", description,
                        "latitude", position.toLatitude(),
                        "longitude", position.toLongitude()
                ))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 낙상 감지 이벤트 생성
     */
    public static SimulationEvent fallDetected(String robotId, String zoneId, Position position) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.FALL_DETECTED)
                .robotId(robotId)
                .zoneId(zoneId)
                .position(position)
                .priority(EventPriority.CRITICAL)
                .data(Map.of(
                        "emergencyType", "FALL",
                        "description", "Worker fall detected",
                        "latitude", position.toLatitude(),
                        "longitude", position.toLongitude()
                ))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 건강 위험 이벤트 생성
     */
    public static SimulationEvent healthCritical(String robotId, String zoneId, Position position,
                                                 Map<String, Object> healthData) {
        return SimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(EventType.HEALTH_CRITICAL)
                .robotId(robotId)
                .zoneId(zoneId)
                .position(position)
                .priority(EventPriority.CRITICAL)
                .data(healthData)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
