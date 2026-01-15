package com.safetyhub.simulator.world;

import com.safetyhub.simulator.core.Location;
import com.safetyhub.simulator.core.RobotState;
import com.safetyhub.simulator.core.RobotWorker;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 가상 공장 월드
 * 로봇들이 동작하는 시뮬레이션 환경
 */
public class VirtualWorld {
    private final String id;
    private final String name;
    private final GridMap gridMap;
    private final PathFinder pathFinder;
    private final Map<String, RobotWorker> robots;
    private final Map<String, Set<String>> zoneOccupants; // zoneId -> robotIds
    private LocalTime simulationTime;
    private boolean emergencyMode;
    private String emergencyZoneId;

    public VirtualWorld(String id, String name, int width, int height) {
        this.id = id;
        this.name = name;
        this.gridMap = new GridMap(width, height);
        this.pathFinder = new PathFinder(gridMap);
        this.robots = new ConcurrentHashMap<>();
        this.zoneOccupants = new ConcurrentHashMap<>();
        this.simulationTime = LocalTime.of(8, 0);
        this.emergencyMode = false;
    }

    /**
     * 표준 공장 레이아웃 생성
     */
    public static VirtualWorld createStandardFactory(String id, String name) {
        VirtualWorld world = new VirtualWorld(id, name, 100, 100);

        // Zone A-D: 작업 구역 (각 20x20)
        world.addZone(new Zone("ZONE_A", "작업장 A", ZoneType.WORK_AREA, 5, 5, 20, 20, 50));
        world.addZone(new Zone("ZONE_B", "작업장 B", ZoneType.WORK_AREA, 30, 5, 20, 20, 50));
        world.addZone(new Zone("ZONE_C", "작업장 C", ZoneType.WORK_AREA, 55, 5, 20, 20, 50));
        world.addZone(new Zone("ZONE_D", "작업장 D", ZoneType.WORK_AREA, 80, 5, 15, 20, 30));

        // Zone E: 휴게실 (15x15)
        world.addZone(new Zone("ZONE_E", "휴게실", ZoneType.REST_AREA, 5, 50, 15, 15, 30));

        // Zone F: 식당 (20x15)
        world.addZone(new Zone("ZONE_F", "식당", ZoneType.DINING_AREA, 25, 50, 20, 15, 100));

        // Zone G: 의무실 (10x10)
        world.addZone(new Zone("ZONE_G", "의무실", ZoneType.MEDICAL, 50, 50, 10, 10, 10));

        // Zone H: 대피소 (25x20)
        world.addZone(new Zone("ZONE_H", "대피소", ZoneType.SHELTER, 70, 50, 25, 20, 200));

        // 충전소
        world.addZone(new Zone("CHARGING", "충전소", ZoneType.CHARGING_STATION, 5, 75, 10, 10, 20));

        // 출입구
        world.addZone(new Zone("ENTRANCE", "출입구", ZoneType.ENTRANCE, 45, 90, 10, 10, 50));

        // 벽 설정 (외벽)
        world.getGridMap().setWall(0, 0, 99, 0);   // 상단 벽
        world.getGridMap().setWall(0, 99, 99, 99); // 하단 벽
        world.getGridMap().setWall(0, 0, 0, 99);   // 좌측 벽
        world.getGridMap().setWall(99, 0, 99, 99); // 우측 벽

        return world;
    }

    public void addZone(Zone zone) {
        gridMap.addZone(zone);
        zoneOccupants.put(zone.getId(), ConcurrentHashMap.newKeySet());
    }

    public void addRobot(RobotWorker robot) {
        robots.put(robot.getId(), robot);
        updateRobotZone(robot);
    }

    public void removeRobot(String robotId) {
        RobotWorker robot = robots.remove(robotId);
        if (robot != null) {
            String zoneId = robot.getLocation().getZoneId();
            if (zoneId != null && zoneOccupants.containsKey(zoneId)) {
                zoneOccupants.get(zoneId).remove(robotId);
            }
        }
    }

    public void spawnRobots(int count) {
        List<Zone> workZones = gridMap.getAllZones().values().stream()
                .filter(z -> z.getType() == ZoneType.WORK_AREA)
                .collect(Collectors.toList());

        for (int i = 0; i < count; i++) {
            Zone zone = workZones.get(i % workZones.size());
            Location location = zone.getRandomLocation();
            RobotWorker robot = RobotWorker.create("Robot-" + (i + 1), location);
            addRobot(robot);
        }
    }

    public void tick() {
        simulationTime = simulationTime.plusSeconds(1);

        for (RobotWorker robot : robots.values()) {
            if (!emergencyMode) {
                robot.updateState(simulationTime);
            }

            // 이동이 필요한 경우 경로 설정
            if (robot.getState() == RobotState.MOVING && !robot.isMoving()) {
                String targetZone = robot.getSchedule().getScheduledZone(simulationTime);
                if (targetZone != null) {
                    List<Location> path = pathFinder.findPathToZone(robot.getLocation(), targetZone);
                    if (!path.isEmpty()) {
                        robot.setPath(path);
                    }
                }
            }

            robot.tick();
            updateRobotZone(robot);
        }
    }

    private void updateRobotZone(RobotWorker robot) {
        Location loc = robot.getLocation();
        String newZoneId = gridMap.getZoneId(loc);

        // 이전 구역에서 제거
        for (Map.Entry<String, Set<String>> entry : zoneOccupants.entrySet()) {
            if (!entry.getKey().equals(newZoneId)) {
                entry.getValue().remove(robot.getId());
            }
        }

        // 새 구역에 추가
        if (newZoneId != null && zoneOccupants.containsKey(newZoneId)) {
            zoneOccupants.get(newZoneId).add(robot.getId());
            robot.setLocation(new Location(loc.getX(), loc.getY(), newZoneId));
        }
    }

    public void triggerEmergency(String zoneId) {
        emergencyMode = true;
        emergencyZoneId = zoneId;

        Zone hazardZone = gridMap.getZone(zoneId);
        if (hazardZone != null) {
            hazardZone.setHazardous(true);
        }

        // 모든 로봇에게 대피 명령
        for (RobotWorker robot : robots.values()) {
            List<Location> evacuationPath = pathFinder.findEvacuationPath(robot.getLocation());
            if (!evacuationPath.isEmpty()) {
                robot.startEvacuation(evacuationPath);
            }
        }
    }

    public void clearEmergency() {
        emergencyMode = false;
        if (emergencyZoneId != null) {
            Zone zone = gridMap.getZone(emergencyZoneId);
            if (zone != null) {
                zone.setHazardous(false);
            }
        }
        emergencyZoneId = null;
    }

    public List<RobotWorker> getRobotsInZone(String zoneId) {
        Set<String> robotIds = zoneOccupants.get(zoneId);
        if (robotIds == null) {
            return Collections.emptyList();
        }
        return robotIds.stream()
                .map(robots::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<RobotWorker> getRobotsInEmergency() {
        return robots.values().stream()
                .filter(RobotWorker::isInEmergency)
                .collect(Collectors.toList());
    }

    public int getEvacuatedCount() {
        Zone shelter = gridMap.getZone("ZONE_H");
        if (shelter == null) return 0;
        return getRobotsInZone("ZONE_H").size();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public GridMap getGridMap() { return gridMap; }
    public PathFinder getPathFinder() { return pathFinder; }
    public Collection<RobotWorker> getAllRobots() { return robots.values(); }
    public RobotWorker getRobot(String robotId) { return robots.get(robotId); }
    public int getRobotCount() { return robots.size(); }
    public LocalTime getSimulationTime() { return simulationTime; }
    public boolean isEmergencyMode() { return emergencyMode; }
    public String getEmergencyZoneId() { return emergencyZoneId; }

    public void setSimulationTime(LocalTime time) { this.simulationTime = time; }

    public Map<String, Integer> getZoneOccupancy() {
        Map<String, Integer> occupancy = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : zoneOccupants.entrySet()) {
            occupancy.put(entry.getKey(), entry.getValue().size());
        }
        return occupancy;
    }
}
