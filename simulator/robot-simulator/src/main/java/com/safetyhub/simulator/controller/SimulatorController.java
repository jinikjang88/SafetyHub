package com.safetyhub.simulator.controller;

import com.safetyhub.simulator.core.RobotWorker;
import com.safetyhub.simulator.scenario.Scenario;
import com.safetyhub.simulator.scenario.ScenarioEngine;
import com.safetyhub.simulator.scenario.ScenarioLoader;
import com.safetyhub.simulator.world.VirtualWorld;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 시뮬레이터 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

    private final ScenarioEngine scenarioEngine;
    private final ScenarioLoader scenarioLoader;
    private final VirtualWorld world;

    public SimulatorController(ScenarioEngine scenarioEngine, ScenarioLoader scenarioLoader, VirtualWorld world) {
        this.scenarioEngine = scenarioEngine;
        this.scenarioLoader = scenarioLoader;
        this.world = world;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", scenarioEngine.isRunning());
        status.put("elapsedSeconds", scenarioEngine.getElapsedSeconds());
        status.put("robotCount", world.getRobotCount());
        status.put("simulationTime", world.getSimulationTime().toString());
        status.put("emergencyMode", world.isEmergencyMode());
        status.put("zoneOccupancy", world.getZoneOccupancy());

        Scenario current = scenarioEngine.getCurrentScenario();
        if (current != null) {
            status.put("scenario", Map.of(
                    "id", current.getId(),
                    "name", current.getName(),
                    "durationMinutes", current.getDurationMinutes()
            ));
        }

        return ResponseEntity.ok(status);
    }

    @PostMapping("/start/{scenarioId}")
    public ResponseEntity<Map<String, String>> startScenario(@PathVariable String scenarioId) {
        try {
            Scenario scenario = scenarioLoader.loadFromResource("scenarios/" + scenarioId + ".yaml");
            scenarioEngine.loadScenario(scenario);
            scenarioEngine.start();
            return ResponseEntity.ok(Map.of("message", "Scenario started: " + scenarioId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopScenario() {
        scenarioEngine.stop();
        return ResponseEntity.ok(Map.of("message", "Scenario stopped"));
    }

    @PostMapping("/pause")
    public ResponseEntity<Map<String, String>> pauseScenario() {
        scenarioEngine.pause();
        return ResponseEntity.ok(Map.of("message", "Scenario paused"));
    }

    @PostMapping("/resume")
    public ResponseEntity<Map<String, String>> resumeScenario() {
        scenarioEngine.resume();
        return ResponseEntity.ok(Map.of("message", "Scenario resumed"));
    }

    @GetMapping("/robots")
    public ResponseEntity<List<Map<String, Object>>> getRobots(
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "100") int limit) {

        var robots = world.getAllRobots().stream();

        if (zoneId != null) {
            robots = robots.filter(r -> zoneId.equals(r.getLocation().getZoneId()));
        }
        if (state != null) {
            robots = robots.filter(r -> state.equalsIgnoreCase(r.getState().name()));
        }

        List<Map<String, Object>> result = robots
                .limit(limit)
                .map(this::toRobotMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/robots/{robotId}")
    public ResponseEntity<Map<String, Object>> getRobot(@PathVariable String robotId) {
        RobotWorker robot = world.getRobot(robotId);
        if (robot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toRobotMap(robot));
    }

    @GetMapping("/robots/emergency")
    public ResponseEntity<List<Map<String, Object>>> getEmergencyRobots() {
        List<Map<String, Object>> result = world.getRobotsInEmergency().stream()
                .map(this::toRobotMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/emergency/{zoneId}")
    public ResponseEntity<Map<String, String>> triggerEmergency(@PathVariable String zoneId) {
        world.triggerEmergency(zoneId);
        return ResponseEntity.ok(Map.of("message", "Emergency triggered in zone: " + zoneId));
    }

    @PostMapping("/emergency/clear")
    public ResponseEntity<Map<String, String>> clearEmergency() {
        world.clearEmergency();
        return ResponseEntity.ok(Map.of("message", "Emergency cleared"));
    }

    @GetMapping("/zones")
    public ResponseEntity<Map<String, Object>> getZones() {
        Map<String, Object> zones = new HashMap<>();
        world.getGridMap().getAllZones().forEach((id, zone) -> {
            zones.put(id, Map.of(
                    "name", zone.getName(),
                    "type", zone.getType().name(),
                    "capacity", zone.getCapacity(),
                    "occupancy", world.getRobotsInZone(id).size(),
                    "hazardous", zone.isHazardous()
            ));
        });
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/scenarios")
    public ResponseEntity<List<String>> getAvailableScenarios() {
        return ResponseEntity.ok(List.of(
                "daily_operation",
                "fire_emergency",
                "worker_fall",
                "gas_leak",
                "load_test"
        ));
    }

    private Map<String, Object> toRobotMap(RobotWorker robot) {
        return Map.of(
                "id", robot.getId(),
                "name", robot.getName(),
                "state", robot.getState().name(),
                "healthStatus", robot.getHealthStatus().name(),
                "location", Map.of(
                        "x", robot.getLocation().getX(),
                        "y", robot.getLocation().getY(),
                        "zoneId", robot.getLocation().getZoneId() != null ? robot.getLocation().getZoneId() : ""
                ),
                "vitals", Map.of(
                        "heartRate", robot.getHeartRate(),
                        "temperature", robot.getBodyTemperature(),
                        "batteryLevel", robot.getBatteryLevel()
                ),
                "lastUpdate", robot.getLastUpdate().toString()
        );
    }
}
