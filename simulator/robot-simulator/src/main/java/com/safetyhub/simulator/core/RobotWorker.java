package com.safetyhub.simulator.core;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 로봇 더미 작업자 엔티티
 * 실제 작업자(LifeGuard Band 착용)를 시뮬레이션
 */
public class RobotWorker {
    private final String id;
    private final String name;
    private Location location;
    private RobotState state;
    private HealthStatus healthStatus;
    private RobotSchedule schedule;
    private int batteryLevel;
    private int heartRate;
    private double bodyTemperature;
    private Instant lastUpdate;
    private List<Location> currentPath;
    private int pathIndex;
    private final Random random = new Random();

    public RobotWorker(String id, String name, Location initialLocation) {
        this.id = id;
        this.name = name;
        this.location = initialLocation;
        this.state = RobotState.IDLE;
        this.healthStatus = HealthStatus.NORMAL;
        this.schedule = RobotSchedule.createDefaultSchedule();
        this.batteryLevel = 100;
        this.heartRate = 70 + random.nextInt(10);
        this.bodyTemperature = 36.5 + random.nextDouble() * 0.5;
        this.lastUpdate = Instant.now();
        this.currentPath = new CopyOnWriteArrayList<>();
        this.pathIndex = 0;
    }

    public static RobotWorker create(String name, Location location) {
        return new RobotWorker(UUID.randomUUID().toString(), name, location);
    }

    public void updateState(LocalTime currentTime) {
        if (state == RobotState.EMERGENCY || state == RobotState.EVACUATING) {
            return; // 긴급 상황에서는 스케줄 무시
        }

        RobotState scheduledState = schedule.getScheduledState(currentTime);
        if (scheduledState != state && !isMoving()) {
            String targetZone = schedule.getScheduledZone(currentTime);
            if (targetZone != null && !targetZone.equals(location.getZoneId())) {
                state = RobotState.MOVING;
            } else {
                state = scheduledState;
            }
        }
    }

    public void tick() {
        lastUpdate = Instant.now();
        updateVitals();
        consumeBattery();

        if (isMoving() && !currentPath.isEmpty()) {
            moveAlongPath();
        }
    }

    private void updateVitals() {
        // 상태에 따른 바이탈 변화 시뮬레이션
        switch (state) {
            case WORKING:
                heartRate = Math.min(120, heartRate + random.nextInt(3) - 1);
                bodyTemperature = Math.min(37.5, bodyTemperature + random.nextDouble() * 0.1);
                break;
            case RESTING:
            case EATING:
                heartRate = Math.max(60, heartRate - random.nextInt(2));
                bodyTemperature = Math.max(36.3, bodyTemperature - random.nextDouble() * 0.05);
                break;
            case MOVING:
            case EVACUATING:
                heartRate = Math.min(140, heartRate + random.nextInt(5));
                break;
            default:
                heartRate = 70 + random.nextInt(10);
        }

        // 건강 상태 업데이트
        if (heartRate > 130 || bodyTemperature > 38.0) {
            healthStatus = HealthStatus.WARNING;
        } else if (heartRate > 150 || bodyTemperature > 39.0) {
            healthStatus = HealthStatus.DANGER;
        } else {
            healthStatus = HealthStatus.NORMAL;
        }
    }

    private void consumeBattery() {
        if (state == RobotState.CHARGING) {
            batteryLevel = Math.min(100, batteryLevel + 2);
        } else {
            int consumption = switch (state) {
                case WORKING -> 2;
                case MOVING, EVACUATING -> 3;
                case EMERGENCY -> 1;
                default -> 1;
            };
            batteryLevel = Math.max(0, batteryLevel - consumption / 10);
        }
    }

    public void setPath(List<Location> path) {
        this.currentPath = new CopyOnWriteArrayList<>(path);
        this.pathIndex = 0;
        if (!path.isEmpty()) {
            this.state = RobotState.MOVING;
        }
    }

    private void moveAlongPath() {
        if (pathIndex < currentPath.size()) {
            location = currentPath.get(pathIndex);
            pathIndex++;
        }
        if (pathIndex >= currentPath.size()) {
            currentPath.clear();
            pathIndex = 0;
        }
    }

    public void triggerEmergency(HealthStatus emergencyStatus) {
        this.healthStatus = emergencyStatus;
        this.state = RobotState.EMERGENCY;
    }

    public void triggerFall() {
        this.healthStatus = HealthStatus.DANGER;
        this.state = RobotState.EMERGENCY;
        this.heartRate = 150 + random.nextInt(30);
    }

    public void startEvacuation(List<Location> evacuationPath) {
        this.state = RobotState.EVACUATING;
        setPath(evacuationPath);
    }

    public boolean isMoving() {
        return state == RobotState.MOVING || state == RobotState.EVACUATING;
    }

    public boolean needsCharging() {
        return batteryLevel < 20;
    }

    public boolean isInEmergency() {
        return state == RobotState.EMERGENCY || healthStatus.isEmergency();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public RobotState getState() { return state; }
    public HealthStatus getHealthStatus() { return healthStatus; }
    public int getBatteryLevel() { return batteryLevel; }
    public int getHeartRate() { return heartRate; }
    public double getBodyTemperature() { return bodyTemperature; }
    public Instant getLastUpdate() { return lastUpdate; }
    public RobotSchedule getSchedule() { return schedule; }

    // Setters
    public void setLocation(Location location) { this.location = location; }
    public void setState(RobotState state) { this.state = state; }
    public void setHealthStatus(HealthStatus healthStatus) { this.healthStatus = healthStatus; }
    public void setSchedule(RobotSchedule schedule) { this.schedule = schedule; }
    public void setBatteryLevel(int batteryLevel) { this.batteryLevel = batteryLevel; }

    @Override
    public String toString() {
        return String.format("RobotWorker[id=%s, name=%s, state=%s, location=%s, health=%s, battery=%d%%]",
                id, name, state, location, healthStatus, batteryLevel);
    }
}
