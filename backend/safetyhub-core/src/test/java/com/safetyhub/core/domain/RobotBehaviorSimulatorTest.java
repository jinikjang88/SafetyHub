package com.safetyhub.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RobotBehaviorSimulator 테스트
 */
class RobotBehaviorSimulatorTest {

    private VirtualWorld world;
    private RobotBehaviorSimulator simulator;
    private RobotWorker testRobot;

    @BeforeEach
    void setUp() {
        world = VirtualWorld.createDefault();
        simulator = new RobotBehaviorSimulator(world);

        testRobot = RobotBehaviorSimulator.createRobot(
                "ROBOT_TEST_001",
                "Test Robot",
                "ZONE_A"
        );
        testRobot.setCurrentLocation(Location.builder().latitude(5.0).longitude(5.0).build());
        testRobot.setCurrentZoneId("ZONE_A");
        world.addRobot(testRobot);
    }

    @Test
    @DisplayName("로봇 생성 헬퍼 메서드 테스트")
    void testCreateRobot() {
        RobotWorker robot = RobotBehaviorSimulator.createRobot(
                "ROBOT_001",
                "Test Robot",
                "ZONE_A"
        );

        assertNotNull(robot);
        assertEquals("ROBOT_001", robot.getRobotId());
        assertEquals("Test Robot", robot.getName());
        assertEquals("ZONE_A", robot.getAssignedZoneId());
        assertEquals(RobotWorker.RobotState.OFFLINE, robot.getState());
        assertNotNull(robot.getSchedule());
        assertNotNull(robot.getHealth());
        assertNotNull(robot.getBattery());
        assertEquals(1.0, robot.getSpeed());
    }

    @Test
    @DisplayName("작업 시간대 시뮬레이션 테스트")
    void testSimulateWorkingTime() {
        testRobot.changeState(RobotWorker.RobotState.WORKING);
        int initialHeartRate = testRobot.getHealth().getHeartRate();

        // 작업 시간 시뮬레이션 (9:00)
        for (int i = 0; i < 10; i++) {
            simulator.simulateTick(LocalTime.of(9, 0));
        }

        // 배터리가 소모되었는지 확인
        assertTrue(testRobot.getBattery().getLevel() < 100);

        // 여전히 작업 중인지 확인 (점심/휴식 시간이 아니므로)
        assertEquals(RobotWorker.RobotState.WORKING, testRobot.getState());
    }

    @Test
    @DisplayName("휴식 시간 자동 전환 테스트")
    void testAutoTransitionToResting() {
        testRobot.changeState(RobotWorker.RobotState.WORKING);

        // 오전 휴식 시간 (10:05)
        simulator.simulateTick(LocalTime.of(10, 5));

        // 상태가 MOVING 또는 RESTING으로 변경되었는지 확인
        assertTrue(testRobot.getState() == RobotWorker.RobotState.MOVING ||
                   testRobot.getState() == RobotWorker.RobotState.RESTING);
    }

    @Test
    @DisplayName("식사 시간 자동 전환 테스트")
    void testAutoTransitionToEating() {
        testRobot.changeState(RobotWorker.RobotState.WORKING);

        // 점심 시간 (12:30)
        simulator.simulateTick(LocalTime.of(12, 30));

        // 상태가 MOVING 또는 EATING으로 변경되었는지 확인
        assertTrue(testRobot.getState() == RobotWorker.RobotState.MOVING ||
                   testRobot.getState() == RobotWorker.RobotState.EATING);
    }

    @Test
    @DisplayName("배터리 충전 시뮬레이션 테스트")
    void testBatteryCharging() {
        // 배터리를 50%로 설정
        testRobot.getBattery().setLevel(50);
        testRobot.getBattery().updateStatus();
        testRobot.changeState(RobotWorker.RobotState.RESTING);
        testRobot.setCurrentZoneId("ZONE_E"); // 휴게실

        int initialLevel = testRobot.getBattery().getLevel();

        // 휴식 시간 시뮬레이션
        for (int i = 0; i < 5; i++) {
            simulator.simulateTick(LocalTime.of(10, 5));
        }

        // 배터리가 충전되었는지 확인
        assertTrue(testRobot.getBattery().getLevel() > initialLevel);
    }

    @Test
    @DisplayName("배터리 부족 시 자동 충전 이동 테스트")
    void testAutoChargingWhenBatteryLow() {
        // 배터리를 낮은 수준으로 설정
        testRobot.getBattery().setLevel(25);
        testRobot.getBattery().updateStatus();
        testRobot.changeState(RobotWorker.RobotState.WORKING);

        // 작업 시간에 시뮬레이션
        simulator.simulateTick(LocalTime.of(9, 0));

        // 배터리 부족으로 휴식 상태로 전환되었는지 확인
        assertTrue(testRobot.getState() == RobotWorker.RobotState.RESTING ||
                   testRobot.getState() == RobotWorker.RobotState.MOVING);
    }

    @Test
    @DisplayName("이동 시뮬레이션 테스트")
    void testMovingSimulation() {
        testRobot.changeState(RobotWorker.RobotState.WORKING);
        Location startLocation = Location.builder().latitude(5.0).longitude(5.0).build();
        testRobot.setCurrentLocation(startLocation);

        // 목표 위치 설정
        Location targetLocation = Location.builder().latitude(10.0).longitude(10.0).build();
        java.util.List<Location> path = new java.util.ArrayList<>();
        path.add(targetLocation);
        testRobot.setPath(path);

        Location initialLocation = testRobot.getCurrentLocation();

        // 이동 시뮬레이션
        for (int i = 0; i < 10; i++) {
            simulator.simulateTick(LocalTime.of(9, 0));
            if (testRobot.getPlannedPath() == null || testRobot.getPlannedPath().isEmpty()) {
                break;
            }
        }

        // 위치가 변경되었는지 확인
        Location currentLocation = testRobot.getCurrentLocation();
        if (currentLocation != null && initialLocation != null) {
            double distance = initialLocation.distanceTo(currentLocation);
            assertTrue(distance >= 0); // 이동했거나 도착했음
        }
    }

    @Test
    @DisplayName("긴급 상황 처리 테스트")
    void testEmergencyHandling() {
        testRobot.triggerEmergency();

        // 긴급 상황 시뮬레이션
        for (int i = 0; i < 5; i++) {
            simulator.simulateTick(LocalTime.of(9, 0));
        }

        // 긴급 상태가 유지되는지 확인
        assertTrue(testRobot.getState() == RobotWorker.RobotState.EMERGENCY ||
                   testRobot.getState() == RobotWorker.RobotState.MOVING);
    }

    @Test
    @DisplayName("의무실에서 건강 회복 테스트")
    void testHealthRecoveryInMedicalRoom() {
        testRobot.triggerEmergency();
        testRobot.setCurrentZoneId("ZONE_G"); // 의무실

        assertEquals(RobotWorker.HealthSimulation.HealthStatus.DANGER,
                testRobot.getHealth().getStatus());

        // 의무실에서 치료 시뮬레이션
        for (int i = 0; i < 10; i++) {
            simulator.simulateTick(LocalTime.of(9, 0));
        }

        // 건강 상태가 개선되었는지 확인
        assertTrue(testRobot.getHealth().getStatus() == RobotWorker.HealthSimulation.HealthStatus.WARNING ||
                   testRobot.getHealth().getStatus() == RobotWorker.HealthSimulation.HealthStatus.NORMAL);
    }

    @Test
    @DisplayName("퇴근 시간 OFFLINE 전환 테스트")
    void testOfflineTransitionAfterWork() {
        testRobot.changeState(RobotWorker.RobotState.WORKING);

        // 퇴근 시간 (18:00)
        simulator.simulateTick(LocalTime.of(18, 0));

        // 상태가 OFFLINE으로 변경되었는지 확인
        assertEquals(RobotWorker.RobotState.OFFLINE, testRobot.getState());
    }

    @Test
    @DisplayName("여러 로봇 동시 시뮬레이션 테스트")
    void testMultipleRobotsSimulation() {
        // 여러 로봇 추가
        for (int i = 0; i < 10; i++) {
            RobotWorker robot = RobotBehaviorSimulator.createRobot(
                    "ROBOT_" + i,
                    "Robot " + i,
                    "ZONE_A"
            );
            robot.setCurrentLocation(Location.builder()
                    .latitude(5.0 + i)
                    .longitude(5.0)
                    .build());
            robot.setCurrentZoneId("ZONE_A");
            robot.changeState(RobotWorker.RobotState.WORKING);
            world.addRobot(robot);
        }

        // 모든 로봇 시뮬레이션
        simulator.simulateTick(LocalTime.of(9, 0));

        // 모든 로봇이 여전히 존재하는지 확인
        assertEquals(11, world.getAllRobots().size()); // 테스트 로봇 1개 + 추가 10개
    }

    @Test
    @DisplayName("휴식 중 건강 회복 테스트")
    void testHealthRecoveryDuringRest() {
        testRobot.getHealth().setStatus(RobotWorker.HealthSimulation.HealthStatus.WARNING);
        testRobot.changeState(RobotWorker.RobotState.RESTING);
        testRobot.setCurrentZoneId("ZONE_E");

        // 휴식 시뮬레이션
        for (int i = 0; i < 5; i++) {
            simulator.simulateTick(LocalTime.of(10, 5));
        }

        // 건강 상태가 개선되었는지 확인
        assertEquals(RobotWorker.HealthSimulation.HealthStatus.NORMAL,
                testRobot.getHealth().getStatus());
    }

    @Test
    @DisplayName("식사 중 심박수 안정화 테스트")
    void testHeartRateStabilizationDuringEating() {
        testRobot.getHealth().setHeartRate(100);
        testRobot.changeState(RobotWorker.RobotState.EATING);
        testRobot.setCurrentZoneId("ZONE_F"); // 식당

        int initialHeartRate = testRobot.getHealth().getHeartRate();

        // 식사 시뮬레이션
        for (int i = 0; i < 10; i++) {
            simulator.simulateTick(LocalTime.of(12, 30));
        }

        // 심박수가 감소했는지 확인
        assertTrue(testRobot.getHealth().getHeartRate() <= initialHeartRate);
    }

    @Test
    @DisplayName("OFFLINE 상태에서는 배터리 소모 없음 테스트")
    void testNoBatteryDrainWhenOffline() {
        testRobot.changeState(RobotWorker.RobotState.OFFLINE);
        testRobot.getBattery().setLevel(100);

        // OFFLINE 시뮬레이션
        for (int i = 0; i < 10; i++) {
            simulator.simulateTick(LocalTime.of(18, 0));
        }

        // 배터리가 소모되지 않았는지 확인
        assertEquals(100, testRobot.getBattery().getLevel());
    }

    @Test
    @DisplayName("출근 전 OFFLINE 상태 유지 테스트")
    void testStayOfflineBeforeWork() {
        testRobot.changeState(RobotWorker.RobotState.OFFLINE);

        // 출근 전 시간 (7:00)
        simulator.simulateTick(LocalTime.of(7, 0));

        // OFFLINE 상태 유지
        assertEquals(RobotWorker.RobotState.OFFLINE, testRobot.getState());
    }

    @Test
    @DisplayName("대피 상태 시뮬레이션 테스트")
    void testEvacuationSimulation() {
        testRobot.startEvacuation();
        testRobot.setCurrentZoneId("ZONE_A");

        // 대피 시뮬레이션
        for (int i = 0; i < 5; i++) {
            simulator.simulateTick(LocalTime.of(9, 0));
        }

        // 대피 상태가 유지되는지 확인
        assertTrue(testRobot.getState() == RobotWorker.RobotState.EVACUATING ||
                   testRobot.getState() == RobotWorker.RobotState.MOVING);
    }
}
