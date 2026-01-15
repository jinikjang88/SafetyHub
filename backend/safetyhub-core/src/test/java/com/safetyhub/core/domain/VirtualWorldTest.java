package com.safetyhub.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VirtualWorld 도메인 모델 테스트
 */
class VirtualWorldTest {

    private VirtualWorld world;
    private RobotWorker testRobot;

    @BeforeEach
    void setUp() {
        world = VirtualWorld.createDefault();

        testRobot = RobotWorker.builder()
                .robotId("ROBOT_TEST_001")
                .name("Test Robot")
                .state(RobotWorker.RobotState.WORKING)
                .assignedZoneId("ZONE_A")
                .currentLocation(Location.builder().latitude(5.0).longitude(5.0).build())
                .currentZoneId("ZONE_A")
                .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                .health(RobotWorker.HealthSimulation.createNormal())
                .battery(RobotWorker.BatterySimulation.createFull())
                .speed(1.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("기본 가상 세계 생성 테스트")
    void testCreateDefaultWorld() {
        assertNotNull(world);
        assertNotNull(world.getMap());
        assertEquals(VirtualWorld.WorldStatus.IDLE, world.getStatus());
        assertEquals(8, world.getMap().getZones().size()); // 기본 8개 구역
    }

    @Test
    @DisplayName("로봇 추가 테스트")
    void testAddRobot() {
        world.addRobot(testRobot);

        assertEquals(1, world.getAllRobots().size());
        assertNotNull(world.getRobot("ROBOT_TEST_001"));
        assertEquals("Test Robot", world.getRobot("ROBOT_TEST_001").getName());
    }

    @Test
    @DisplayName("로봇 제거 테스트")
    void testRemoveRobot() {
        world.addRobot(testRobot);
        assertEquals(1, world.getAllRobots().size());

        world.removeRobot("ROBOT_TEST_001");
        assertEquals(0, world.getAllRobots().size());
        assertNull(world.getRobot("ROBOT_TEST_001"));
    }

    @Test
    @DisplayName("여러 로봇 추가 테스트")
    void testAddMultipleRobots() {
        for (int i = 0; i < 10; i++) {
            RobotWorker robot = RobotWorker.builder()
                    .robotId("ROBOT_" + i)
                    .name("Robot " + i)
                    .state(RobotWorker.RobotState.WORKING)
                    .assignedZoneId("ZONE_A")
                    .currentZoneId("ZONE_A")
                    .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                    .health(RobotWorker.HealthSimulation.createNormal())
                    .battery(RobotWorker.BatterySimulation.createFull())
                    .createdAt(LocalDateTime.now())
                    .build();
            world.addRobot(robot);
        }

        assertEquals(10, world.getAllRobots().size());
    }

    @Test
    @DisplayName("특정 구역의 로봇 가져오기 테스트")
    void testGetRobotsInZone() {
        // ZONE_A에 로봇 3개 추가
        for (int i = 0; i < 3; i++) {
            RobotWorker robot = RobotWorker.builder()
                    .robotId("ROBOT_A_" + i)
                    .name("Robot A " + i)
                    .state(RobotWorker.RobotState.WORKING)
                    .currentZoneId("ZONE_A")
                    .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                    .health(RobotWorker.HealthSimulation.createNormal())
                    .battery(RobotWorker.BatterySimulation.createFull())
                    .createdAt(LocalDateTime.now())
                    .build();
            world.addRobot(robot);
        }

        // ZONE_B에 로봇 2개 추가
        for (int i = 0; i < 2; i++) {
            RobotWorker robot = RobotWorker.builder()
                    .robotId("ROBOT_B_" + i)
                    .name("Robot B " + i)
                    .state(RobotWorker.RobotState.WORKING)
                    .currentZoneId("ZONE_B")
                    .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                    .health(RobotWorker.HealthSimulation.createNormal())
                    .battery(RobotWorker.BatterySimulation.createFull())
                    .createdAt(LocalDateTime.now())
                    .build();
            world.addRobot(robot);
        }

        List<RobotWorker> robotsInZoneA = world.getRobotsInZone("ZONE_A");
        List<RobotWorker> robotsInZoneB = world.getRobotsInZone("ZONE_B");

        assertEquals(3, robotsInZoneA.size());
        assertEquals(2, robotsInZoneB.size());
    }

    @Test
    @DisplayName("로봇 위치 업데이트 및 구역 자동 변경 테스트")
    void testUpdateRobotLocationWithZoneChange() {
        world.addRobot(testRobot);

        // 초기 상태 확인
        assertEquals("ZONE_A", testRobot.getCurrentZoneId());
        assertEquals(1, world.getRobotsInZone("ZONE_A").size());
        assertEquals(0, world.getRobotsInZone("ZONE_B").size());

        // ZONE_B 위치로 이동
        Location newLocation = Location.builder().latitude(5.0).longitude(15.0).build();
        world.updateRobotLocation("ROBOT_TEST_001", newLocation);

        // 구역이 자동으로 변경되었는지 확인
        RobotWorker updatedRobot = world.getRobot("ROBOT_TEST_001");
        assertEquals("ZONE_B", updatedRobot.getCurrentZoneId());
        assertEquals(0, world.getRobotsInZone("ZONE_A").size());
        assertEquals(1, world.getRobotsInZone("ZONE_B").size());
    }

    @Test
    @DisplayName("세계 상태 변경 테스트")
    void testChangeWorldStatus() {
        assertEquals(VirtualWorld.WorldStatus.IDLE, world.getStatus());

        world.start();
        assertEquals(VirtualWorld.WorldStatus.RUNNING, world.getStatus());

        world.pause();
        assertEquals(VirtualWorld.WorldStatus.PAUSED, world.getStatus());

        world.stop();
        assertEquals(VirtualWorld.WorldStatus.STOPPED, world.getStatus());
    }

    @Test
    @DisplayName("긴급 상황 선언 테스트")
    void testDeclareEmergency() {
        // 여러 로봇 추가
        for (int i = 0; i < 5; i++) {
            RobotWorker robot = RobotWorker.builder()
                    .robotId("ROBOT_" + i)
                    .name("Robot " + i)
                    .state(RobotWorker.RobotState.WORKING)
                    .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                    .health(RobotWorker.HealthSimulation.createNormal())
                    .battery(RobotWorker.BatterySimulation.createFull())
                    .createdAt(LocalDateTime.now())
                    .build();
            world.addRobot(robot);
        }

        // 긴급 상황 선언
        world.declareEmergency();

        // 세계 상태가 EMERGENCY로 변경되었는지 확인
        assertEquals(VirtualWorld.WorldStatus.EMERGENCY, world.getStatus());

        // 모든 로봇이 대피 상태로 변경되었는지 확인
        for (RobotWorker robot : world.getAllRobots()) {
            assertEquals(RobotWorker.RobotState.EVACUATING, robot.getState());
        }
    }

    @Test
    @DisplayName("통계 정보 테스트")
    void testGetStatistics() {
        // 작업 중인 로봇 3개
        for (int i = 0; i < 3; i++) {
            RobotWorker robot = RobotWorker.builder()
                    .robotId("WORKING_" + i)
                    .name("Working Robot " + i)
                    .state(RobotWorker.RobotState.WORKING)
                    .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                    .health(RobotWorker.HealthSimulation.createNormal())
                    .battery(RobotWorker.BatterySimulation.createFull())
                    .createdAt(LocalDateTime.now())
                    .build();
            world.addRobot(robot);
        }

        // 휴식 중인 로봇 2개
        for (int i = 0; i < 2; i++) {
            RobotWorker robot = RobotWorker.builder()
                    .robotId("RESTING_" + i)
                    .name("Resting Robot " + i)
                    .state(RobotWorker.RobotState.RESTING)
                    .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                    .health(RobotWorker.HealthSimulation.createNormal())
                    .battery(RobotWorker.BatterySimulation.createFull())
                    .createdAt(LocalDateTime.now())
                    .build();
            world.addRobot(robot);
        }

        // 긴급 상황 로봇 1개
        RobotWorker emergencyRobot = RobotWorker.builder()
                .robotId("EMERGENCY_1")
                .name("Emergency Robot")
                .state(RobotWorker.RobotState.EMERGENCY)
                .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                .health(RobotWorker.HealthSimulation.builder()
                        .heartRate(150)
                        .bodyTemperature(38.5)
                        .oxygenSaturation(85)
                        .status(RobotWorker.HealthSimulation.HealthStatus.DANGER)
                        .build())
                .battery(RobotWorker.BatterySimulation.createFull())
                .createdAt(LocalDateTime.now())
                .build();
        world.addRobot(emergencyRobot);

        VirtualWorld.WorldStatistics stats = world.getStatistics();

        assertEquals(6, stats.getTotalRobots());
        assertEquals(3, stats.getWorkingRobots());
        assertEquals(1, stats.getEmergencyRobots());
    }

    @Test
    @DisplayName("기본 8개 구역 생성 확인 테스트")
    void testDefaultZonesCreation() {
        assertEquals(8, world.getMap().getZones().size());

        // 각 구역이 제대로 생성되었는지 확인
        assertNotNull(world.getMap().getZone("ZONE_A"));
        assertNotNull(world.getMap().getZone("ZONE_B"));
        assertNotNull(world.getMap().getZone("ZONE_C"));
        assertNotNull(world.getMap().getZone("ZONE_D"));
        assertNotNull(world.getMap().getZone("ZONE_E"));
        assertNotNull(world.getMap().getZone("ZONE_F"));
        assertNotNull(world.getMap().getZone("ZONE_G"));
        assertNotNull(world.getMap().getZone("ZONE_H"));

        // 구역 타입 확인
        assertEquals(Zone.ZoneType.WORK_AREA, world.getMap().getZone("ZONE_A").getType());
        assertEquals(Zone.ZoneType.DANGER_ZONE, world.getMap().getZone("ZONE_C").getType());
        assertEquals(Zone.ZoneType.REST_AREA, world.getMap().getZone("ZONE_E").getType());
        assertEquals(Zone.ZoneType.ASSEMBLY_POINT, world.getMap().getZone("ZONE_H").getType());
    }

    @Test
    @DisplayName("null 로봇 추가 시도 테스트")
    void testAddNullRobot() {
        int initialSize = world.getAllRobots().size();
        world.addRobot(null);
        assertEquals(initialSize, world.getAllRobots().size());
    }

    @Test
    @DisplayName("존재하지 않는 로봇 제거 테스트")
    void testRemoveNonExistentRobot() {
        int initialSize = world.getAllRobots().size();
        world.removeRobot("NON_EXISTENT_ROBOT");
        assertEquals(initialSize, world.getAllRobots().size());
    }

    @Test
    @DisplayName("존재하지 않는 구역의 로봇 가져오기 테스트")
    void testGetRobotsInNonExistentZone() {
        List<RobotWorker> robots = world.getRobotsInZone("NON_EXISTENT_ZONE");
        assertNotNull(robots);
        assertEquals(0, robots.size());
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        String worldString = world.toString();
        assertNotNull(worldString);
        assertTrue(worldString.contains("VirtualWorld"));
        assertTrue(worldString.contains("status="));
        assertTrue(worldString.contains("robots="));
        assertTrue(worldString.contains("zones="));
    }
}
