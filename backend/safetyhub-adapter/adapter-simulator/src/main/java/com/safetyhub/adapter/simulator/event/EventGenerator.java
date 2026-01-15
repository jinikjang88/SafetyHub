package com.safetyhub.adapter.simulator.event;

import com.safetyhub.adapter.simulator.robot.HealthStatus;
import com.safetyhub.adapter.simulator.robot.RobotState;
import com.safetyhub.adapter.simulator.robot.RobotWorker;
import com.safetyhub.adapter.simulator.world.VirtualWorld;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 이벤트 생성기
 * 로봇 상태 변화를 감지하여 이벤트 생성
 */
@Slf4j
@RequiredArgsConstructor
public class EventGenerator {

    private final VirtualWorld world;
    private final Queue<SimulationEvent> eventQueue = new ConcurrentLinkedQueue<>();

    // 이전 상태 추적
    private final Map<String, RobotState> previousStates = new HashMap<>();
    private final Map<String, String> previousZones = new HashMap<>();

    private final Random random = new Random();

    /**
     * 로봇 상태 변화 감지 및 이벤트 생성
     */
    public List<SimulationEvent> generateEvents(RobotWorker robot) {
        List<SimulationEvent> events = new ArrayList<>();
        String robotId = robot.getRobotId();

        // 상태 변경 감지
        RobotState previousState = previousStates.get(robotId);
        if (previousState != null && previousState != robot.getState()) {
            events.add(SimulationEvent.stateChanged(
                    robotId, previousState.name(), robot.getState().name()));

            // 긴급 상황 이벤트
            if (robot.getState() == RobotState.EMERGENCY) {
                events.add(generateEmergencyEvent(robot));
            }
        }
        previousStates.put(robotId, robot.getState());

        // 구역 변경 감지
        String previousZone = previousZones.get(robotId);
        String currentZone = robot.getCurrentZoneId();
        if (!Objects.equals(previousZone, currentZone)) {
            if (previousZone != null) {
                events.add(SimulationEvent.zoneExited(
                        robotId, previousZone, robot.getCurrentPosition()));
            }
            if (currentZone != null) {
                events.add(SimulationEvent.zoneEntered(
                        robotId, currentZone, robot.getCurrentPosition()));
            }
        }
        previousZones.put(robotId, currentZone);

        // 위치 업데이트 이벤트 (10% 확률로 전송)
        if (random.nextDouble() < 0.1) {
            events.add(SimulationEvent.locationUpdate(
                    robotId, robot.getCurrentPosition(), robot.getCurrentZoneId()));
        }

        // 건강 위험 감지
        if (robot.getHealthStatus() != null && robot.getHealthStatus().isDangerous()) {
            if (robot.getHealthStatus().isFallen()) {
                events.add(SimulationEvent.fallDetected(
                        robotId, robot.getCurrentZoneId(), robot.getCurrentPosition()));
            } else if (robot.getHealthStatus().getLevel() == HealthStatus.HealthLevel.CRITICAL) {
                events.add(SimulationEvent.healthCritical(
                        robotId, robot.getCurrentZoneId(), robot.getCurrentPosition(),
                        Map.of(
                                "heartRate", robot.getHealthStatus().getHeartRate(),
                                "temperature", robot.getHealthStatus().getTemperature(),
                                "oxygenLevel", robot.getHealthStatus().getOxygenLevel()
                        )));
            }
        }

        // 배터리 부족 경고
        if (robot.getBatteryStatus() != null && robot.getBatteryStatus().isCritical()) {
            events.add(SimulationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(SimulationEvent.EventType.BATTERY_LOW)
                    .robotId(robotId)
                    .priority(SimulationEvent.EventPriority.HIGH)
                    .data(Map.of("batteryLevel", robot.getBatteryStatus().getLevel()))
                    .build());
        }

        // 이벤트 큐에 추가
        eventQueue.addAll(events);

        return events;
    }

    /**
     * 센서 데이터 이벤트 생성
     */
    public SimulationEvent generateSensorData(RobotWorker robot) {
        Map<String, Object> sensorData = new HashMap<>();

        // 환경 센서 데이터
        sensorData.put("temperature", 20 + random.nextDouble() * 15);  // 20-35°C
        sensorData.put("humidity", 40 + random.nextDouble() * 40);      // 40-80%
        sensorData.put("gasLevel", random.nextDouble() * 50);           // 0-50 ppm
        sensorData.put("noise", 60 + random.nextDouble() * 30);         // 60-90 dB
        sensorData.put("vibration", random.nextDouble() * 5);           // 0-5 mm/s

        // 건강 데이터
        if (robot.getHealthStatus() != null) {
            sensorData.put("heartRate", robot.getHealthStatus().getHeartRate());
            sensorData.put("bodyTemperature", robot.getHealthStatus().getTemperature());
            sensorData.put("oxygenLevel", robot.getHealthStatus().getOxygenLevel());
        }

        SimulationEvent event = SimulationEvent.sensorData(robot.getRobotId(), sensorData);
        eventQueue.add(event);

        return event;
    }

    /**
     * 하트비트 이벤트 생성
     */
    public SimulationEvent generateHeartbeat(RobotWorker robot) {
        SimulationEvent event = SimulationEvent.heartbeat(robot.getRobotId());
        eventQueue.add(event);
        return event;
    }

    /**
     * 긴급 상황 이벤트 생성
     */
    private SimulationEvent generateEmergencyEvent(RobotWorker robot) {
        String emergencyType = "UNKNOWN";
        String description = "Emergency detected";

        if (robot.getHealthStatus() != null) {
            if (robot.getHealthStatus().isFallen()) {
                emergencyType = "FALL";
                description = "Worker fall detected - immediate assistance required";
            } else if (robot.getHealthStatus().getHeartRate() > 120) {
                emergencyType = "HEART_RATE";
                description = "Abnormal heart rate detected: " + robot.getHealthStatus().getHeartRate() + " bpm";
            } else if (robot.getHealthStatus().getTemperature() > 38.0) {
                emergencyType = "FEVER";
                description = "High body temperature detected: " + robot.getHealthStatus().getTemperature() + "°C";
            } else if (robot.getHealthStatus().getOxygenLevel() < 92) {
                emergencyType = "OXYGEN";
                description = "Low oxygen level detected: " + robot.getHealthStatus().getOxygenLevel() + "%";
            }
        }

        return SimulationEvent.emergency(
                robot.getRobotId(),
                robot.getCurrentZoneId(),
                robot.getCurrentPosition(),
                emergencyType,
                description
        );
    }

    /**
     * 이벤트 큐에서 이벤트 가져오기
     */
    public SimulationEvent pollEvent() {
        return eventQueue.poll();
    }

    /**
     * 대기 중인 이벤트 수
     */
    public int getPendingEventCount() {
        return eventQueue.size();
    }

    /**
     * 모든 대기 이벤트 가져오기
     */
    public List<SimulationEvent> drainEvents() {
        List<SimulationEvent> events = new ArrayList<>();
        SimulationEvent event;
        while ((event = eventQueue.poll()) != null) {
            events.add(event);
        }
        return events;
    }

    /**
     * 이벤트 큐 초기화
     */
    public void clear() {
        eventQueue.clear();
        previousStates.clear();
        previousZones.clear();
    }
}
