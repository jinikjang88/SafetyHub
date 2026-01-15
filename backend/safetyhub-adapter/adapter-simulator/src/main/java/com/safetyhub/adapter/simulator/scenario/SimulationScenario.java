package com.safetyhub.adapter.simulator.scenario;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 시뮬레이션 시나리오 정의
 */
@Getter
@Builder
public class SimulationScenario {

    private final String name;
    private final String description;
    private final int robotCount;
    private final Duration duration;
    private final List<TimelineEvent> timeline;
    private final Map<String, Object> config;

    /**
     * 일상 운영 시나리오
     */
    public static SimulationScenario dailyOperation(int robotCount) {
        return SimulationScenario.builder()
                .name("daily_operation")
                .description("평상시 공장 운영 시뮬레이션")
                .robotCount(robotCount)
                .duration(Duration.ofHours(8))
                .timeline(List.of())
                .config(Map.of(
                        "emergencyProbability", 0.0001,
                        "healthIssueProbability", 0.0005
                ))
                .build();
    }

    /**
     * 화재 긴급 시나리오
     */
    public static SimulationScenario fireEmergency(int robotCount) {
        return SimulationScenario.builder()
                .name("fire_emergency")
                .description("Zone C 화재 발생 시뮬레이션")
                .robotCount(robotCount)
                .duration(Duration.ofMinutes(30))
                .timeline(List.of(
                        TimelineEvent.builder()
                                .timeOffset(Duration.ZERO)
                                .eventType("FIRE_DETECTED")
                                .targetZone("ZONE-C")
                                .description("Zone C 화재 감지")
                                .build(),
                        TimelineEvent.builder()
                                .timeOffset(Duration.ofMinutes(1))
                                .eventType("EVACUATION_ORDER")
                                .targetZone("ALL")
                                .description("전체 대피 명령")
                                .build(),
                        TimelineEvent.builder()
                                .timeOffset(Duration.ofMinutes(5))
                                .eventType("CALL_119")
                                .description("119 자동 신고")
                                .build()
                ))
                .config(Map.of(
                        "emergencyZone", "ZONE-C",
                        "emergencyType", "FIRE"
                ))
                .build();
    }

    /**
     * 작업자 낙상 시나리오
     */
    public static SimulationScenario workerFall(int robotCount) {
        return SimulationScenario.builder()
                .name("worker_fall")
                .description("작업자 낙상 감지 시뮬레이션")
                .robotCount(robotCount)
                .duration(Duration.ofMinutes(10))
                .timeline(List.of(
                        TimelineEvent.builder()
                                .timeOffset(Duration.ZERO)
                                .eventType("FALL_DETECTED")
                                .description("작업자 낙상 감지")
                                .build()
                ))
                .config(Map.of(
                        "emergencyType", "FALL",
                        "forceFall", true
                ))
                .build();
    }

    /**
     * 가스 누출 시나리오
     */
    public static SimulationScenario gasLeak(int robotCount) {
        return SimulationScenario.builder()
                .name("gas_leak")
                .description("유해 가스 누출 시뮬레이션")
                .robotCount(robotCount)
                .duration(Duration.ofMinutes(20))
                .timeline(List.of(
                        TimelineEvent.builder()
                                .timeOffset(Duration.ZERO)
                                .eventType("GAS_DETECTED")
                                .targetZone("ZONE-C")
                                .description("Zone C 가스 농도 상승 감지")
                                .build(),
                        TimelineEvent.builder()
                                .timeOffset(Duration.ofSeconds(1))
                                .eventType("EQUIPMENT_SHUTDOWN")
                                .targetZone("ZONE-C")
                                .description("즉시 설비 차단")
                                .build(),
                        TimelineEvent.builder()
                                .timeOffset(Duration.ofMinutes(1))
                                .eventType("EVACUATION_ORDER")
                                .targetZone("ZONE-C")
                                .description("Zone C 대피 명령")
                                .build()
                ))
                .config(Map.of(
                        "emergencyZone", "ZONE-C",
                        "emergencyType", "GAS_LEAK"
                ))
                .build();
    }

    /**
     * 부하 테스트 시나리오
     */
    public static SimulationScenario loadTest(int robotCount) {
        return SimulationScenario.builder()
                .name("load_test")
                .description(robotCount + "대 동시 접속 테스트")
                .robotCount(robotCount)
                .duration(Duration.ofHours(1))
                .timeline(List.of())
                .config(Map.of(
                        "highFrequencyUpdates", true,
                        "updateIntervalMs", 100
                ))
                .build();
    }

    @Getter
    @Builder
    public static class TimelineEvent {
        private final Duration timeOffset;
        private final String eventType;
        private final String targetZone;
        private final String description;
        private final Map<String, Object> params;
    }
}
