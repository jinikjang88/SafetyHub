package com.safetyhub.simulator.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 시뮬레이터 이벤트 기본 클래스
 */
public class SimulatorEvent {
    private final String id;
    private final String type;
    private final String robotId;
    private final Instant timestamp;
    private final Object payload;
    private final EventPriority priority;

    public enum EventPriority {
        LOW, NORMAL, HIGH, CRITICAL
    }

    public SimulatorEvent(String type, String robotId, Object payload, EventPriority priority) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.robotId = robotId;
        this.timestamp = Instant.now();
        this.payload = payload;
        this.priority = priority;
    }

    public static SimulatorEvent location(String robotId, LocationData location) {
        return new SimulatorEvent("LOCATION_UPDATE", robotId, location, EventPriority.NORMAL);
    }

    public static SimulatorEvent heartbeat(String robotId, VitalsData vitals) {
        return new SimulatorEvent("HEARTBEAT", robotId, vitals, EventPriority.NORMAL);
    }

    public static SimulatorEvent emergency(String robotId, EmergencyData emergency) {
        return new SimulatorEvent("EMERGENCY", robotId, emergency, EventPriority.CRITICAL);
    }

    public static SimulatorEvent stateChange(String robotId, StateChangeData stateChange) {
        return new SimulatorEvent("STATE_CHANGE", robotId, stateChange, EventPriority.HIGH);
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getRobotId() { return robotId; }
    public Instant getTimestamp() { return timestamp; }
    public Object getPayload() { return payload; }
    public EventPriority getPriority() { return priority; }

    // Payload data classes
    public record LocationData(int x, int y, String zoneId) {}
    public record VitalsData(int heartRate, double temperature, int batteryLevel, String state) {}
    public record EmergencyData(String type, String healthStatus, int heartRate, double temperature, String zoneId) {}
    public record StateChangeData(String previousState, String newState, String reason) {}
}
