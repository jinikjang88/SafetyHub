package com.safetyhub.simulator.scenario;

import com.safetyhub.simulator.core.HealthStatus;
import com.safetyhub.simulator.core.RobotWorker;
import com.safetyhub.simulator.event.EventGenerator;
import com.safetyhub.simulator.world.VirtualWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 시나리오 실행 엔진
 */
public class ScenarioEngine {
    private static final Logger log = LoggerFactory.getLogger(ScenarioEngine.class);

    private final VirtualWorld world;
    private final EventGenerator eventGenerator;
    private Scenario currentScenario;
    private final AtomicInteger elapsedSeconds = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    private final List<ScenarioEventListener> listeners = new ArrayList<>();

    public ScenarioEngine(VirtualWorld world, EventGenerator eventGenerator) {
        this.world = world;
        this.eventGenerator = eventGenerator;
    }

    public void loadScenario(Scenario scenario) {
        this.currentScenario = scenario;
        log.info("Scenario loaded: {} - {}", scenario.getId(), scenario.getName());
    }

    public void start() {
        if (currentScenario == null) {
            throw new IllegalStateException("No scenario loaded");
        }

        if (running.get()) {
            log.warn("Scenario is already running");
            return;
        }

        // 로봇 생성
        world.spawnRobots(currentScenario.getRobotCount());
        log.info("Spawned {} robots", currentScenario.getRobotCount());

        running.set(true);
        elapsedSeconds.set(0);
        world.setSimulationTime(LocalTime.of(8, 0));

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::tick, 0, 100, TimeUnit.MILLISECONDS); // 10x 속도

        log.info("Scenario started: {}", currentScenario.getName());
        notifyListeners("SCENARIO_STARTED", null);
    }

    public void stop() {
        running.set(false);
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Scenario stopped");
        notifyListeners("SCENARIO_STOPPED", null);
    }

    public void pause() {
        running.set(false);
        log.info("Scenario paused");
    }

    public void resume() {
        running.set(true);
        log.info("Scenario resumed");
    }

    private void tick() {
        if (!running.get()) return;

        int currentSecond = elapsedSeconds.incrementAndGet();

        // 월드 업데이트
        world.tick();

        // 이벤트 생성
        eventGenerator.generateEvents(world);

        // 예약된 시나리오 이벤트 확인
        checkScheduledEvents(currentSecond);

        // 시나리오 종료 확인
        if (currentSecond >= currentScenario.getDurationMinutes() * 60) {
            stop();
            notifyListeners("SCENARIO_COMPLETED", null);
        }
    }

    private void checkScheduledEvents(int currentSecond) {
        for (Scenario.ScenarioEvent event : currentScenario.getEvents()) {
            if (event.getTriggerTimeSeconds() == currentSecond) {
                executeEvent(event);
            }
        }
    }

    private void executeEvent(Scenario.ScenarioEvent event) {
        log.info("Executing event: {} at {}s", event.getEventType(), elapsedSeconds.get());

        switch (event.getEventType()) {
            case "FIRE" -> {
                world.triggerEmergency(event.getTargetZone());
                notifyListeners("FIRE_DETECTED", event.getTargetZone());
            }
            case "GAS_LEAK" -> {
                world.triggerEmergency(event.getTargetZone());
                notifyListeners("GAS_LEAK_DETECTED", event.getTargetZone());
            }
            case "WORKER_FALL" -> {
                RobotWorker robot = selectRandomRobot(event.getTargetZone());
                if (robot != null) {
                    robot.triggerFall();
                    notifyListeners("WORKER_FALL", robot.getId());
                }
            }
            case "HEALTH_EMERGENCY" -> {
                RobotWorker robot = selectRandomRobot(event.getTargetZone());
                if (robot != null) {
                    robot.triggerEmergency(HealthStatus.CRITICAL);
                    notifyListeners("HEALTH_EMERGENCY", robot.getId());
                }
            }
            case "CLEAR_EMERGENCY" -> {
                world.clearEmergency();
                notifyListeners("EMERGENCY_CLEARED", null);
            }
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private RobotWorker selectRandomRobot(String zoneId) {
        List<RobotWorker> robots;
        if (zoneId != null) {
            robots = world.getRobotsInZone(zoneId);
        } else {
            robots = new ArrayList<>(world.getAllRobots());
        }

        if (robots.isEmpty()) return null;
        return robots.get((int) (Math.random() * robots.size()));
    }

    public void addListener(ScenarioEventListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(String eventType, Object data) {
        for (ScenarioEventListener listener : listeners) {
            try {
                listener.onEvent(eventType, data);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        }
    }

    public interface ScenarioEventListener {
        void onEvent(String eventType, Object data);
    }

    // Getters
    public boolean isRunning() { return running.get(); }
    public int getElapsedSeconds() { return elapsedSeconds.get(); }
    public Scenario getCurrentScenario() { return currentScenario; }
    public VirtualWorld getWorld() { return world; }
}
