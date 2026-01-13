package com.safetyhub.simulator.event;

import com.safetyhub.simulator.core.Location;
import com.safetyhub.simulator.core.RobotState;
import com.safetyhub.simulator.core.RobotWorker;
import com.safetyhub.simulator.world.VirtualWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 시뮬레이션 이벤트 생성기
 */
public class EventGenerator {
    private final List<EventListener> listeners = new ArrayList<>();
    private final Map<String, RobotState> previousStates = new ConcurrentHashMap<>();
    private final AtomicLong tickCounter = new AtomicLong(0);
    private int locationUpdateInterval = 10;  // 10틱마다 위치 업데이트
    private int heartbeatInterval = 50;       // 50틱마다 하트비트

    public void generateEvents(VirtualWorld world) {
        long tick = tickCounter.incrementAndGet();

        for (RobotWorker robot : world.getAllRobots()) {
            // 상태 변경 감지
            RobotState previousState = previousStates.get(robot.getId());
            if (previousState != robot.getState()) {
                generateStateChangeEvent(robot, previousState);
                previousStates.put(robot.getId(), robot.getState());
            }

            // 긴급 상황 감지
            if (robot.isInEmergency()) {
                generateEmergencyEvent(robot);
            }

            // 주기적 위치 업데이트
            if (tick % locationUpdateInterval == 0) {
                generateLocationEvent(robot);
            }

            // 주기적 하트비트
            if (tick % heartbeatInterval == 0) {
                generateHeartbeatEvent(robot);
            }
        }
    }

    private void generateLocationEvent(RobotWorker robot) {
        Location loc = robot.getLocation();
        SimulatorEvent event = SimulatorEvent.location(
                robot.getId(),
                new SimulatorEvent.LocationData(loc.getX(), loc.getY(), loc.getZoneId())
        );
        notifyListeners(event);
    }

    private void generateHeartbeatEvent(RobotWorker robot) {
        SimulatorEvent event = SimulatorEvent.heartbeat(
                robot.getId(),
                new SimulatorEvent.VitalsData(
                        robot.getHeartRate(),
                        robot.getBodyTemperature(),
                        robot.getBatteryLevel(),
                        robot.getState().name()
                )
        );
        notifyListeners(event);
    }

    private void generateEmergencyEvent(RobotWorker robot) {
        SimulatorEvent event = SimulatorEvent.emergency(
                robot.getId(),
                new SimulatorEvent.EmergencyData(
                        "HEALTH_EMERGENCY",
                        robot.getHealthStatus().name(),
                        robot.getHeartRate(),
                        robot.getBodyTemperature(),
                        robot.getLocation().getZoneId()
                )
        );
        notifyListeners(event);
    }

    private void generateStateChangeEvent(RobotWorker robot, RobotState previousState) {
        SimulatorEvent event = SimulatorEvent.stateChange(
                robot.getId(),
                new SimulatorEvent.StateChangeData(
                        previousState != null ? previousState.name() : "UNKNOWN",
                        robot.getState().name(),
                        "Schedule or emergency"
                )
        );
        notifyListeners(event);
    }

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(SimulatorEvent event) {
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                // 로그 처리
            }
        }
    }

    public void setLocationUpdateInterval(int interval) {
        this.locationUpdateInterval = interval;
    }

    public void setHeartbeatInterval(int interval) {
        this.heartbeatInterval = interval;
    }

    public interface EventListener {
        void onEvent(SimulatorEvent event);
    }
}
