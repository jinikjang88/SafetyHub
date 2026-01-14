package com.safetyhub.adapter.simulator;

import com.safetyhub.adapter.simulator.scenario.SimulationScenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 시뮬레이션 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final RobotSimulationService simulationService;

    /**
     * 시뮬레이션 시작
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSimulation(
            @RequestBody StartRequest request) {

        SimulationScenario scenario = switch (request.scenario()) {
            case "daily_operation" -> SimulationScenario.dailyOperation(request.robots());
            case "fire_emergency" -> SimulationScenario.fireEmergency(request.robots());
            case "worker_fall" -> SimulationScenario.workerFall(request.robots());
            case "gas_leak" -> SimulationScenario.gasLeak(request.robots());
            case "load_test" -> SimulationScenario.loadTest(request.robots());
            default -> SimulationScenario.dailyOperation(request.robots());
        };

        simulationService.startScenario(scenario);

        return ResponseEntity.ok(Map.of(
                "status", "started",
                "scenario", scenario.getName(),
                "robots", request.robots()
        ));
    }

    /**
     * 시뮬레이션 중지
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopSimulation() {
        simulationService.stop();

        return ResponseEntity.ok(Map.of(
                "status", "stopped"
        ));
    }

    /**
     * 시뮬레이션 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<RobotSimulationService.SimulationStatus> getStatus() {
        return ResponseEntity.ok(simulationService.getStatus());
    }

    /**
     * 메트릭 조회
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(simulationService.getMetrics().getSnapshot());
    }

    /**
     * 대피 명령
     */
    @PostMapping("/evacuate")
    public ResponseEntity<Map<String, Object>> triggerEvacuation() {
        simulationService.triggerEvacuation();

        return ResponseEntity.ok(Map.of(
                "status", "evacuation_triggered"
        ));
    }

    /**
     * 특정 로봇 긴급 상황 발생
     */
    @PostMapping("/emergency/{robotId}")
    public ResponseEntity<Map<String, Object>> triggerEmergency(
            @PathVariable String robotId) {
        simulationService.triggerEmergency(robotId);

        return ResponseEntity.ok(Map.of(
                "status", "emergency_triggered",
                "robotId", robotId
        ));
    }

    /**
     * 구역별 로봇 현황
     */
    @GetMapping("/zones")
    public ResponseEntity<Map<String, Integer>> getZoneOccupancy() {
        if (simulationService.getWorld() == null) {
            return ResponseEntity.ok(Map.of());
        }
        return ResponseEntity.ok(simulationService.getWorld().getZoneOccupancy());
    }

    /**
     * 시작 요청 DTO
     */
    public record StartRequest(
            String scenario,
            int robots
    ) {
        public StartRequest {
            if (robots <= 0) robots = 100;
            if (scenario == null) scenario = "daily_operation";
        }
    }
}
