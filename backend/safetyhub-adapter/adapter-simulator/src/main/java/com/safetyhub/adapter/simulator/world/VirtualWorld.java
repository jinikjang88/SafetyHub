package com.safetyhub.adapter.simulator.world;

import com.safetyhub.adapter.simulator.robot.Position;
import com.safetyhub.adapter.simulator.robot.RobotWorker;
import com.safetyhub.adapter.simulator.robot.ZoneType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 가상 세계 (Virtual Factory)
 * 시뮬레이션 환경의 전체 공간 관리
 */
@Slf4j
@Getter
public class VirtualWorld {

    private final String worldId;
    private final GridMap gridMap;
    private final PathFinder pathFinder;
    private final Map<String, SimulationZone> zones;
    private final Map<String, RobotWorker> robots;

    // 기본 맵 크기
    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 50;

    public VirtualWorld(String worldId) {
        this.worldId = worldId;
        this.gridMap = new GridMap(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.pathFinder = new PathFinder(gridMap);
        this.zones = new ConcurrentHashMap<>();
        this.robots = new ConcurrentHashMap<>();

        initializeDefaultFactory();
    }

    /**
     * 기본 공장 레이아웃 초기화
     */
    private void initializeDefaultFactory() {
        // 외벽 생성
        gridMap.createHorizontalWall(0, DEFAULT_WIDTH - 1, 0);
        gridMap.createHorizontalWall(0, DEFAULT_WIDTH - 1, DEFAULT_HEIGHT - 1);
        gridMap.createVerticalWall(0, 0, DEFAULT_HEIGHT - 1);
        gridMap.createVerticalWall(DEFAULT_WIDTH - 1, 0, DEFAULT_HEIGHT - 1);

        // Zone A: 작업장 1 (좌상단)
        createZone("ZONE-A", "작업장 1", ZoneType.WORK_AREA,
                SimulationZone.DangerLevel.MEDIUM, 50,
                Position.of(5, 5), Position.of(25, 20));

        // Zone B: 작업장 2
        createZone("ZONE-B", "작업장 2", ZoneType.WORK_AREA,
                SimulationZone.DangerLevel.MEDIUM, 50,
                Position.of(30, 5), Position.of(50, 20));

        // Zone C: 위험 구역
        createZone("ZONE-C", "위험 구역", ZoneType.DANGER_ZONE,
                SimulationZone.DangerLevel.HIGH, 10,
                Position.of(55, 5), Position.of(70, 20));
        gridMap.fillRect(55, 5, 70, 20, GridMap.CellType.DANGER);

        // Zone D: 창고
        createZone("ZONE-D", "창고", ZoneType.WORK_AREA,
                SimulationZone.DangerLevel.LOW, 30,
                Position.of(75, 5), Position.of(94, 20));

        // 중앙 복도
        createZone("ZONE-CORRIDOR", "중앙 복도", ZoneType.CORRIDOR,
                SimulationZone.DangerLevel.LOW, 100,
                Position.of(5, 22), Position.of(94, 28));

        // Zone E: 휴게실 (좌하단)
        createZone("ZONE-E", "휴게실", ZoneType.REST_AREA,
                SimulationZone.DangerLevel.LOW, 20,
                Position.of(5, 30), Position.of(25, 44));

        // Zone F: 식당
        createZone("ZONE-F", "식당", ZoneType.CAFETERIA,
                SimulationZone.DangerLevel.LOW, 50,
                Position.of(30, 30), Position.of(50, 44));

        // Zone G: 의무실
        createZone("ZONE-G", "의무실", ZoneType.MEDICAL,
                SimulationZone.DangerLevel.LOW, 5,
                Position.of(55, 30), Position.of(70, 44));

        // Zone H: 대피소
        createZone("ZONE-H", "대피소", ZoneType.ASSEMBLY_POINT,
                SimulationZone.DangerLevel.LOW, 200,
                Position.of(75, 30), Position.of(94, 44));

        log.info("Virtual World initialized: {} zones created", zones.size());
    }

    /**
     * 구역 생성
     */
    private void createZone(String zoneId, String name, ZoneType type,
                            SimulationZone.DangerLevel dangerLevel, int maxCapacity,
                            Position topLeft, Position bottomRight) {
        SimulationZone zone = SimulationZone.builder()
                .zoneId(zoneId)
                .name(name)
                .type(type)
                .dangerLevel(dangerLevel)
                .maxCapacity(maxCapacity)
                .topLeft(topLeft)
                .bottomRight(bottomRight)
                .build();
        zones.put(zoneId, zone);
    }

    /**
     * 로봇 추가
     */
    public void addRobot(RobotWorker robot) {
        robots.put(robot.getRobotId(), robot);

        // 현재 위치의 구역에 로봇 등록
        String zoneId = findZoneAtPosition(robot.getCurrentPosition());
        if (zoneId != null) {
            zones.get(zoneId).addRobot(robot.getRobotId());
        }

        log.debug("Robot added to world: {}", robot.getRobotId());
    }

    /**
     * 로봇 제거
     */
    public void removeRobot(String robotId) {
        RobotWorker robot = robots.remove(robotId);
        if (robot != null && robot.getCurrentZoneId() != null) {
            SimulationZone zone = zones.get(robot.getCurrentZoneId());
            if (zone != null) {
                zone.removeRobot(robotId);
            }
        }
    }

    /**
     * 로봇 위치 업데이트
     */
    public void updateRobotPosition(String robotId, Position newPosition) {
        RobotWorker robot = robots.get(robotId);
        if (robot == null) return;

        String oldZoneId = robot.getCurrentZoneId();
        String newZoneId = findZoneAtPosition(newPosition);

        // 구역 변경 처리
        if (!Objects.equals(oldZoneId, newZoneId)) {
            if (oldZoneId != null && zones.containsKey(oldZoneId)) {
                zones.get(oldZoneId).removeRobot(robotId);
            }
            if (newZoneId != null && zones.containsKey(newZoneId)) {
                zones.get(newZoneId).addRobot(robotId);
            }
        }

        // 로봇 상태 업데이트
        RobotWorker updatedRobot = robot.updatePosition(newPosition, newZoneId);
        robots.put(robotId, updatedRobot);
    }

    /**
     * 특정 위치가 속한 구역 ID 찾기
     */
    public String findZoneAtPosition(Position position) {
        return zones.values().stream()
                .filter(zone -> zone.contains(position))
                .map(SimulationZone::getZoneId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 특정 타입의 구역 목록 반환
     */
    public List<SimulationZone> getZonesByType(ZoneType type) {
        return zones.values().stream()
                .filter(zone -> zone.getType() == type)
                .toList();
    }

    /**
     * 가장 가까운 특정 타입 구역 찾기
     */
    public SimulationZone findNearestZone(Position from, ZoneType type) {
        return getZonesByType(type).stream()
                .min(Comparator.comparingInt(zone ->
                        from.manhattanDistance(zone.getCenterPosition())))
                .orElse(null);
    }

    /**
     * 대피소 찾기
     */
    public SimulationZone getAssemblyPoint() {
        return getZonesByType(ZoneType.ASSEMBLY_POINT).stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 경로 찾기
     */
    public List<Position> findPath(Position start, Position goal) {
        return pathFinder.findPath(start, goal);
    }

    /**
     * 구역 내 모든 로봇 반환
     */
    public List<RobotWorker> getRobotsInZone(String zoneId) {
        SimulationZone zone = zones.get(zoneId);
        if (zone == null) return Collections.emptyList();

        return zone.getRobotIds().stream()
                .map(robots::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 전체 로봇 수
     */
    public int getTotalRobotCount() {
        return robots.size();
    }

    /**
     * 구역별 인원 현황
     */
    public Map<String, Integer> getZoneOccupancy() {
        Map<String, Integer> occupancy = new HashMap<>();
        for (SimulationZone zone : zones.values()) {
            occupancy.put(zone.getZoneId(), zone.getCurrentOccupancy());
        }
        return occupancy;
    }

    /**
     * 맵 정보 출력 (디버깅용)
     */
    public String getMapAscii() {
        return gridMap.toAscii();
    }
}
