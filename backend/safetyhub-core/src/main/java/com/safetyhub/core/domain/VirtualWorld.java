package com.safetyhub.core.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 가상 세계
 * 로봇 시뮬레이션을 위한 가상 공장 환경
 */
@Getter
public class VirtualWorld {

    private final GridMap map;
    private final Map<String, RobotWorker> robots;  // robotId -> RobotWorker
    private final Map<String, List<String>> zoneRobots;  // zoneId -> List<robotId>
    private WorldStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 세계 상태
     */
    public enum WorldStatus {
        IDLE,       // 대기 중
        RUNNING,    // 실행 중
        PAUSED,     // 일시 정지
        EMERGENCY,  // 긴급 상황
        STOPPED     // 중지됨
    }

    public VirtualWorld(GridMap map) {
        this.map = map;
        this.robots = new ConcurrentHashMap<>();
        this.zoneRobots = new ConcurrentHashMap<>();
        this.status = WorldStatus.IDLE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 맵의 각 구역에 대한 로봇 리스트 초기화
        for (String zoneId : map.getZones().keySet()) {
            zoneRobots.put(zoneId, new ArrayList<>());
        }
    }

    /**
     * 기본 가상 세계 생성
     */
    public static VirtualWorld createDefault() {
        GridMap map = GridMap.createDefault();

        // 기본 구역 생성 및 추가
        createDefaultZones(map);

        return new VirtualWorld(map);
    }

    /**
     * 기본 구역 생성
     */
    private static void createDefaultZones(GridMap map) {
        List<Zone> defaultZones = new ArrayList<>();

        // Zone A: 작업장 1
        defaultZones.add(Zone.builder()
                .zoneId("ZONE_A")
                .name("작업장 1")
                .type(Zone.ZoneType.WORK_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.MEDIUM)
                .maxCapacity(50)
                .currentWorkerCount(0)
                .centerLocation(Location.builder().latitude(5.0).longitude(5.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build());

        // Zone B: 작업장 2
        defaultZones.add(Zone.builder()
                .zoneId("ZONE_B")
                .name("작업장 2")
                .type(Zone.ZoneType.WORK_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.MEDIUM)
                .maxCapacity(50)
                .currentWorkerCount(0)
                .centerLocation(Location.builder().latitude(5.0).longitude(15.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build());

        // Zone C: 위험 구역
        defaultZones.add(Zone.builder()
                .zoneId("ZONE_C")
                .name("위험 구역")
                .type(Zone.ZoneType.DANGER_ZONE)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.HIGH)
                .maxCapacity(10)
                .currentWorkerCount(0)
                .centerLocation(Location.builder().latitude(5.0).longitude(25.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build());

        // Zone D: 창고
        defaultZones.add(Zone.builder()
                .zoneId("ZONE_D")
                .name("창고")
                .type(Zone.ZoneType.WORK_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .maxCapacity(30)
                .currentWorkerCount(0)
                .centerLocation(Location.builder().latitude(5.0).longitude(35.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build());

        // Zone E: 휴게실
        defaultZones.add(Zone.builder()
                .zoneId("ZONE_E")
                .name("휴게실")
                .type(Zone.ZoneType.REST_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .maxCapacity(20)
                .currentWorkerCount(0)
                .centerLocation(Location.builder().latitude(35.0).longitude(5.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build());

        // Zone F: 식당
        defaultZones.add(Zone.builder()
                .zoneId("ZONE_F")
                .name("식당")
                .type(Zone.ZoneType.REST_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .maxCapacity(50)
                .currentWorkerCount(0)
                .centerLocation(Location.builder().latitude(35.0).longitude(15.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build());

        // Zone G: 의무실
        defaultZones.add(Zone.builder()
                .zoneId("ZONE_G")
                .name("의무실")
                .type(Zone.ZoneType.REST_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .maxCapacity(5)
                .currentWorkerCount(0)
                .centerLocation(Location.builder().latitude(35.0).longitude(25.0).build())
                .radius(3.0)
                .createdAt(LocalDateTime.now())
                .build());

        // Zone H: 대피소
        defaultZones.add(Zone.builder()
                .zoneId("ZONE_H")
                .name("대피소")
                .type(Zone.ZoneType.ASSEMBLY_POINT)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .maxCapacity(200)
                .currentWorkerCount(0)
                .centerLocation(Location.builder().latitude(35.0).longitude(35.0).build())
                .radius(8.0)
                .createdAt(LocalDateTime.now())
                .build());

        // 맵에 구역 추가
        for (Zone zone : defaultZones) {
            map.addZone(zone);
        }
    }

    /**
     * 로봇 추가
     */
    public void addRobot(RobotWorker robot) {
        if (robot != null && robot.getRobotId() != null) {
            robots.put(robot.getRobotId(), robot);

            // 로봇이 속한 구역에 추가
            if (robot.getCurrentZoneId() != null) {
                addRobotToZone(robot.getRobotId(), robot.getCurrentZoneId());
            }

            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 로봇 제거
     */
    public void removeRobot(String robotId) {
        RobotWorker robot = robots.remove(robotId);
        if (robot != null && robot.getCurrentZoneId() != null) {
            removeRobotFromZone(robotId, robot.getCurrentZoneId());
        }
        updatedAt = LocalDateTime.now();
    }

    /**
     * 로봇 가져오기
     */
    public RobotWorker getRobot(String robotId) {
        return robots.get(robotId);
    }

    /**
     * 모든 로봇 가져오기
     */
    public List<RobotWorker> getAllRobots() {
        return new ArrayList<>(robots.values());
    }

    /**
     * 특정 구역의 로봇 가져오기
     */
    public List<RobotWorker> getRobotsInZone(String zoneId) {
        List<String> robotIds = zoneRobots.get(zoneId);
        if (robotIds == null) {
            return new ArrayList<>();
        }
        return robotIds.stream()
                .map(robots::get)
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    /**
     * 로봇을 구역에 추가
     */
    private void addRobotToZone(String robotId, String zoneId) {
        List<String> robotList = zoneRobots.computeIfAbsent(zoneId, k -> new ArrayList<>());
        if (!robotList.contains(robotId)) {
            robotList.add(robotId);
        }
    }

    /**
     * 구역에서 로봇 제거
     */
    private void removeRobotFromZone(String robotId, String zoneId) {
        List<String> robotList = zoneRobots.get(zoneId);
        if (robotList != null) {
            robotList.remove(robotId);
        }
    }

    /**
     * 로봇 위치 업데이트
     */
    public void updateRobotLocation(String robotId, Location newLocation) {
        RobotWorker robot = robots.get(robotId);
        if (robot == null) return;

        String oldZoneId = robot.getCurrentZoneId();
        Zone newZone = map.findZoneAt(newLocation);
        String newZoneId = newZone != null ? newZone.getZoneId() : null;

        // 구역 변경 시 처리
        if (!java.util.Objects.equals(oldZoneId, newZoneId)) {
            if (oldZoneId != null) {
                removeRobotFromZone(robotId, oldZoneId);
            }
            if (newZoneId != null) {
                addRobotToZone(robotId, newZoneId);
            }
        }

        robot.updateLocation(newLocation, newZoneId);
        updatedAt = LocalDateTime.now();
    }

    /**
     * 세계 상태 변경
     */
    public void changeStatus(WorldStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 시작
     */
    public void start() {
        this.status = WorldStatus.RUNNING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 일시 정지
     */
    public void pause() {
        this.status = WorldStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 중지
     */
    public void stop() {
        this.status = WorldStatus.STOPPED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 긴급 상황 선언
     */
    public void declareEmergency() {
        this.status = WorldStatus.EMERGENCY;
        this.updatedAt = LocalDateTime.now();

        // 모든 로봇을 대피 상태로 전환
        robots.values().forEach(RobotWorker::startEvacuation);
    }

    /**
     * 통계 정보
     */
    public WorldStatistics getStatistics() {
        int totalRobots = robots.size();
        long workingRobots = robots.values().stream()
                .filter(r -> r.getState() == RobotWorker.RobotState.WORKING)
                .count();
        long emergencyRobots = robots.values().stream()
                .filter(RobotWorker::isInDanger)
                .count();

        return new WorldStatistics(totalRobots, (int) workingRobots, (int) emergencyRobots);
    }

    /**
     * 통계 정보 클래스
     */
    @Getter
    @lombok.AllArgsConstructor
    public static class WorldStatistics {
        private int totalRobots;
        private int workingRobots;
        private int emergencyRobots;
    }

    @Override
    public String toString() {
        return String.format("VirtualWorld[status=%s, robots=%d, zones=%d]",
                status, robots.size(), map.getZones().size());
    }
}
