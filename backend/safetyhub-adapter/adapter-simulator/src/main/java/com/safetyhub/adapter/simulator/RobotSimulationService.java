package com.safetyhub.adapter.simulator;

import com.safetyhub.adapter.simulator.engine.RobotBehaviorEngine;
import com.safetyhub.adapter.simulator.event.EventGenerator;
import com.safetyhub.adapter.simulator.event.SimulationEvent;
import com.safetyhub.adapter.simulator.robot.Position;
import com.safetyhub.adapter.simulator.robot.RobotState;
import com.safetyhub.adapter.simulator.robot.RobotWorker;
import com.safetyhub.adapter.simulator.robot.ZoneType;
import com.safetyhub.adapter.simulator.scenario.SimulationScenario;
import com.safetyhub.adapter.simulator.world.SimulationZone;
import com.safetyhub.adapter.simulator.world.VirtualWorld;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 로봇 시뮬레이션 서비스
 * 가상 공장 환경에서 로봇 작업자들의 행동을 시뮬레이션
 */
@Slf4j
@Service
public class RobotSimulationService {

    @Getter
    private VirtualWorld world;
    private RobotBehaviorEngine behaviorEngine;
    private EventGenerator eventGenerator;

    private ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong tickCount = new AtomicLong(0);

    private SimulationScenario currentScenario;
    private LocalDateTime simulationStartTime;
    private LocalTime simulationTime;

    // 이벤트 리스너
    private final List<Consumer<SimulationEvent>> eventListeners = new CopyOnWriteArrayList<>();

    // 메트릭
    @Getter
    private final SimulationMetrics metrics = new SimulationMetrics();

    // 틱 간격 (ms)
    private static final long DEFAULT_TICK_INTERVAL = 100;

    /**
     * 시뮬레이션 초기화
     */
    public void initialize() {
        this.world = new VirtualWorld("FACTORY-001");
        this.behaviorEngine = new RobotBehaviorEngine(world);
        this.eventGenerator = new EventGenerator(world);
        log.info("Simulation initialized");
    }

    /**
     * 시나리오 시작
     */
    public void startScenario(SimulationScenario scenario) {
        if (running.get()) {
            log.warn("Simulation already running");
            return;
        }

        initialize();
        this.currentScenario = scenario;

        // 로봇 생성
        createRobots(scenario.getRobotCount());

        // 시뮬레이션 시작
        start();

        log.info("Scenario started: {} with {} robots",
                scenario.getName(), scenario.getRobotCount());
    }

    /**
     * 로봇 생성
     */
    private void createRobots(int count) {
        List<SimulationZone> workZones = world.getZonesByType(ZoneType.WORK_AREA);

        for (int i = 0; i < count; i++) {
            // 작업 구역에 균등 배치
            SimulationZone zone = workZones.get(i % workZones.size());
            Position position = zone.getRandomPosition();

            RobotWorker robot = RobotWorker.create(
                    "Worker-" + (i + 1),
                    zone.getZoneId(),
                    position
            );

            // 초기 상태를 WORKING으로 설정
            robot = robot.updateState(RobotState.WORKING);
            world.addRobot(robot);
        }

        log.info("Created {} robots", count);
    }

    /**
     * 시뮬레이션 시작
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            simulationStartTime = LocalDateTime.now();
            simulationTime = LocalTime.of(8, 0);  // 오전 8시 시작
            tickCount.set(0);
            metrics.reset();

            scheduler = Executors.newScheduledThreadPool(2);
            scheduler.scheduleAtFixedRate(
                    this::tick,
                    0,
                    DEFAULT_TICK_INTERVAL,
                    TimeUnit.MILLISECONDS
            );

            // 하트비트 (5초마다)
            scheduler.scheduleAtFixedRate(
                    this::sendHeartbeats,
                    0,
                    5000,
                    TimeUnit.MILLISECONDS
            );

            log.info("Simulation started");
        }
    }

    /**
     * 시뮬레이션 중지
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            log.info("Simulation stopped. Total ticks: {}, Events: {}",
                    tickCount.get(), metrics.getTotalEvents());
        }
    }

    /**
     * 시뮬레이션 틱
     */
    private void tick() {
        if (!running.get()) return;

        long startTime = System.nanoTime();
        tickCount.incrementAndGet();

        // 시뮬레이션 시간 진행 (1틱 = 1분)
        simulationTime = simulationTime.plusMinutes(1);
        if (simulationTime.getHour() >= 17) {
            simulationTime = LocalTime.of(8, 0);  // 다음 날 시작
        }

        // 모든 로봇 업데이트
        List<RobotWorker> robots = new ArrayList<>(world.getRobots().values());
        for (RobotWorker robot : robots) {
            try {
                // 행동 실행
                RobotWorker updated = behaviorEngine.tick(robot, simulationTime);
                world.getRobots().put(robot.getRobotId(), updated);

                // 이벤트 생성
                List<SimulationEvent> events = eventGenerator.generateEvents(updated);

                // 이벤트 발행
                for (SimulationEvent event : events) {
                    publishEvent(event);
                }

            } catch (Exception e) {
                log.error("Error processing robot {}: {}", robot.getRobotId(), e.getMessage());
            }
        }

        // 메트릭 업데이트
        long tickDuration = (System.nanoTime() - startTime) / 1_000_000;
        metrics.recordTick(tickDuration);
    }

    /**
     * 하트비트 전송
     */
    private void sendHeartbeats() {
        if (!running.get()) return;

        for (RobotWorker robot : world.getRobots().values()) {
            if (robot.isOnline()) {
                SimulationEvent heartbeat = eventGenerator.generateHeartbeat(robot);
                publishEvent(heartbeat);
            }
        }
    }

    /**
     * 이벤트 발행
     */
    private void publishEvent(SimulationEvent event) {
        metrics.recordEvent(event.getType());

        for (Consumer<SimulationEvent> listener : eventListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.error("Error in event listener: {}", e.getMessage());
            }
        }
    }

    /**
     * 이벤트 리스너 등록
     */
    public void addEventListener(Consumer<SimulationEvent> listener) {
        eventListeners.add(listener);
    }

    /**
     * 이벤트 리스너 제거
     */
    public void removeEventListener(Consumer<SimulationEvent> listener) {
        eventListeners.remove(listener);
    }

    /**
     * 대피 명령 실행
     */
    public void triggerEvacuation() {
        if (running.get() && behaviorEngine != null) {
            behaviorEngine.triggerEvacuation();
            log.warn("Evacuation triggered!");
        }
    }

    /**
     * 특정 로봇에 긴급 상황 발생
     */
    public void triggerEmergency(String robotId) {
        RobotWorker robot = world.getRobots().get(robotId);
        if (robot != null) {
            RobotWorker emergency = robot.triggerEmergency();
            world.getRobots().put(robotId, emergency);
            List<SimulationEvent> events = eventGenerator.generateEvents(emergency);
            events.forEach(this::publishEvent);
            log.warn("Emergency triggered for robot: {}", robotId);
        }
    }

    /**
     * 시뮬레이션 상태 조회
     */
    public SimulationStatus getStatus() {
        return SimulationStatus.builder()
                .running(running.get())
                .scenarioName(currentScenario != null ? currentScenario.getName() : null)
                .totalRobots(world != null ? world.getTotalRobotCount() : 0)
                .simulationTime(simulationTime)
                .tickCount(tickCount.get())
                .zoneOccupancy(world != null ? world.getZoneOccupancy() : Map.of())
                .metrics(metrics.getSnapshot())
                .build();
    }

    /**
     * 시뮬레이션 상태
     */
    @Getter
    @lombok.Builder
    public static class SimulationStatus {
        private final boolean running;
        private final String scenarioName;
        private final int totalRobots;
        private final LocalTime simulationTime;
        private final long tickCount;
        private final Map<String, Integer> zoneOccupancy;
        private final Map<String, Object> metrics;
    }

    /**
     * 시뮬레이션 메트릭
     */
    public static class SimulationMetrics {
        private final AtomicLong totalEvents = new AtomicLong(0);
        private final AtomicLong totalTicks = new AtomicLong(0);
        private final AtomicLong totalTickTime = new AtomicLong(0);
        private final Map<SimulationEvent.EventType, AtomicLong> eventCounts = new ConcurrentHashMap<>();

        public void reset() {
            totalEvents.set(0);
            totalTicks.set(0);
            totalTickTime.set(0);
            eventCounts.clear();
        }

        public void recordTick(long durationMs) {
            totalTicks.incrementAndGet();
            totalTickTime.addAndGet(durationMs);
        }

        public void recordEvent(SimulationEvent.EventType type) {
            totalEvents.incrementAndGet();
            eventCounts.computeIfAbsent(type, k -> new AtomicLong(0)).incrementAndGet();
        }

        public long getTotalEvents() {
            return totalEvents.get();
        }

        public Map<String, Object> getSnapshot() {
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("totalEvents", totalEvents.get());
            snapshot.put("totalTicks", totalTicks.get());
            snapshot.put("avgTickTimeMs", totalTicks.get() > 0
                    ? totalTickTime.get() / totalTicks.get() : 0);

            Map<String, Long> eventsByType = new HashMap<>();
            eventCounts.forEach((type, count) -> eventsByType.put(type.name(), count.get()));
            snapshot.put("eventsByType", eventsByType);

            return snapshot;
        }
    }
}
