package com.safetyhub.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RobotWorker 도메인 모델 테스트
 */
class RobotWorkerTest {

    private RobotWorker robot;

    @BeforeEach
    void setUp() {
        robot = RobotWorker.builder()
                .robotId("ROBOT_001")
                .name("Test Robot")
                .state(RobotWorker.RobotState.OFFLINE)
                .assignedZoneId("ZONE_A")
                .currentLocation(Location.builder()
                        .latitude(10.0)
                        .longitude(20.0)
                        .build())
                .currentZoneId("ZONE_A")
                .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                .health(RobotWorker.HealthSimulation.createNormal())
                .battery(RobotWorker.BatterySimulation.createFull())
                .speed(1.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("로봇 생성 시 초기 상태가 올바른지 확인")
    void testRobotInitialization() {
        assertNotNull(robot);
        assertEquals("ROBOT_001", robot.getRobotId());
        assertEquals("Test Robot", robot.getName());
        assertEquals(RobotWorker.RobotState.OFFLINE, robot.getState());
        assertEquals("ZONE_A", robot.getAssignedZoneId());
        assertEquals(1.0, robot.getSpeed());
    }

    @Test
    @DisplayName("로봇 상태 변경 테스트")
    void testChangeState() {
        robot.changeState(RobotWorker.RobotState.WORKING);
        assertEquals(RobotWorker.RobotState.WORKING, robot.getState());

        robot.changeState(RobotWorker.RobotState.RESTING);
        assertEquals(RobotWorker.RobotState.RESTING, robot.getState());
    }

    @Test
    @DisplayName("로봇 위치 업데이트 테스트")
    void testUpdateLocation() {
        Location newLocation = Location.builder()
                .latitude(15.0)
                .longitude(25.0)
                .build();

        robot.updateLocation(newLocation, "ZONE_B");

        assertEquals(15.0, robot.getCurrentLocation().getLatitude());
        assertEquals(25.0, robot.getCurrentLocation().getLongitude());
        assertEquals("ZONE_B", robot.getCurrentZoneId());
        assertNotNull(robot.getLastActiveAt());
    }

    @Test
    @DisplayName("긴급 상황 발생 테스트")
    void testTriggerEmergency() {
        robot.triggerEmergency();

        assertEquals(RobotWorker.RobotState.EMERGENCY, robot.getState());
        assertEquals(RobotWorker.HealthSimulation.HealthStatus.DANGER,
                robot.getHealth().getStatus());
    }

    @Test
    @DisplayName("대피 시작 테스트")
    void testStartEvacuation() {
        robot.startEvacuation();
        assertEquals(RobotWorker.RobotState.EVACUATING, robot.getState());
    }

    @Test
    @DisplayName("위험 상태 확인 테스트")
    void testIsInDanger() {
        assertFalse(robot.isInDanger());

        robot.triggerEmergency();
        assertTrue(robot.isInDanger());

        robot.startEvacuation();
        assertTrue(robot.isInDanger());
    }

    @Test
    @DisplayName("경로 설정 테스트")
    void testSetPath() {
        List<Location> path = new ArrayList<>();
        path.add(Location.builder().latitude(10.0).longitude(10.0).build());
        path.add(Location.builder().latitude(20.0).longitude(20.0).build());

        robot.setPath(path);

        assertEquals(RobotWorker.RobotState.MOVING, robot.getState());
        assertEquals(2, robot.getPlannedPath().size());
    }

    @Test
    @DisplayName("이동 중 확인 테스트")
    void testIsMoving() {
        assertFalse(robot.isMoving());

        List<Location> path = new ArrayList<>();
        path.add(Location.builder().latitude(10.0).longitude(10.0).build());
        robot.setPath(path);

        assertTrue(robot.isMoving());
    }

    // === RobotSchedule 테스트 ===

    @Test
    @DisplayName("기본 스케줄 생성 테스트")
    void testDefaultScheduleCreation() {
        RobotWorker.RobotSchedule schedule = RobotWorker.RobotSchedule.createDefaultSchedule();

        assertNotNull(schedule);
        assertEquals(LocalTime.of(8, 0), schedule.getWorkStartTime());
        assertEquals(LocalTime.of(17, 0), schedule.getWorkEndTime());
        assertEquals(LocalTime.of(12, 0), schedule.getLunchBreakStart());
        assertEquals(LocalTime.of(13, 0), schedule.getLunchBreakEnd());
    }

    @Test
    @DisplayName("시간별 상태 결정 테스트 - 작업 시간")
    void testGetStateAtTime_Working() {
        RobotWorker.RobotSchedule schedule = RobotWorker.RobotSchedule.createDefaultSchedule();

        // 09:00 - 작업 시간
        RobotWorker.RobotState state = schedule.getStateAtTime(LocalTime.of(9, 0));
        assertEquals(RobotWorker.RobotState.WORKING, state);

        // 14:00 - 작업 시간
        state = schedule.getStateAtTime(LocalTime.of(14, 0));
        assertEquals(RobotWorker.RobotState.WORKING, state);
    }

    @Test
    @DisplayName("시간별 상태 결정 테스트 - 식사 시간")
    void testGetStateAtTime_Eating() {
        RobotWorker.RobotSchedule schedule = RobotWorker.RobotSchedule.createDefaultSchedule();

        // 12:30 - 식사 시간
        RobotWorker.RobotState state = schedule.getStateAtTime(LocalTime.of(12, 30));
        assertEquals(RobotWorker.RobotState.EATING, state);
    }

    @Test
    @DisplayName("시간별 상태 결정 테스트 - 휴식 시간")
    void testGetStateAtTime_Resting() {
        RobotWorker.RobotSchedule schedule = RobotWorker.RobotSchedule.createDefaultSchedule();

        // 10:05 - 오전 휴식
        RobotWorker.RobotState state = schedule.getStateAtTime(LocalTime.of(10, 5));
        assertEquals(RobotWorker.RobotState.RESTING, state);

        // 15:05 - 오후 휴식
        state = schedule.getStateAtTime(LocalTime.of(15, 5));
        assertEquals(RobotWorker.RobotState.RESTING, state);
    }

    @Test
    @DisplayName("시간별 상태 결정 테스트 - 퇴근 후")
    void testGetStateAtTime_Offline() {
        RobotWorker.RobotSchedule schedule = RobotWorker.RobotSchedule.createDefaultSchedule();

        // 18:00 - 퇴근 후
        RobotWorker.RobotState state = schedule.getStateAtTime(LocalTime.of(18, 0));
        assertEquals(RobotWorker.RobotState.OFFLINE, state);

        // 07:00 - 출근 전
        state = schedule.getStateAtTime(LocalTime.of(7, 0));
        assertEquals(RobotWorker.RobotState.OFFLINE, state);
    }

    // === HealthSimulation 테스트 ===

    @Test
    @DisplayName("정상 건강 상태 생성 테스트")
    void testCreateNormalHealth() {
        RobotWorker.HealthSimulation health = RobotWorker.HealthSimulation.createNormal();

        assertNotNull(health);
        assertTrue(health.getHeartRate() >= 70 && health.getHeartRate() <= 100);
        assertTrue(health.getBodyTemperature() >= 36.0 && health.getBodyTemperature() <= 37.0);
        assertTrue(health.getOxygenSaturation() >= 95 && health.getOxygenSaturation() <= 100);
        assertEquals(RobotWorker.HealthSimulation.HealthStatus.NORMAL, health.getStatus());
    }

    @Test
    @DisplayName("위험 상태 확인 테스트")
    void testIsDangerous() {
        RobotWorker.HealthSimulation health = RobotWorker.HealthSimulation.createNormal();
        assertFalse(health.isDangerous());

        health.setStatus(RobotWorker.HealthSimulation.HealthStatus.WARNING);
        assertFalse(health.isDangerous());

        health.setStatus(RobotWorker.HealthSimulation.HealthStatus.DANGER);
        assertTrue(health.isDangerous());

        health.setStatus(RobotWorker.HealthSimulation.HealthStatus.SOS);
        assertTrue(health.isDangerous());
    }

    // === BatterySimulation 테스트 ===

    @Test
    @DisplayName("완전 충전 배터리 생성 테스트")
    void testCreateFullBattery() {
        RobotWorker.BatterySimulation battery = RobotWorker.BatterySimulation.createFull();

        assertNotNull(battery);
        assertEquals(100, battery.getLevel());
        assertEquals(RobotWorker.BatterySimulation.BatteryStatus.FULL, battery.getStatus());
    }

    @Test
    @DisplayName("배터리 방전 테스트")
    void testBatteryDischarge() {
        RobotWorker.BatterySimulation battery = RobotWorker.BatterySimulation.createFull();

        battery.discharge();
        assertEquals(99, battery.getLevel());

        // 여러 번 방전
        for (int i = 0; i < 50; i++) {
            battery.discharge();
        }
        assertEquals(49, battery.getLevel());
        assertEquals(RobotWorker.BatterySimulation.BatteryStatus.LOW, battery.getStatus());
    }

    @Test
    @DisplayName("배터리 충전 테스트")
    void testBatteryCharge() {
        RobotWorker.BatterySimulation battery = RobotWorker.BatterySimulation.builder()
                .level(50)
                .status(RobotWorker.BatterySimulation.BatteryStatus.GOOD)
                .build();

        battery.charge();
        assertEquals(60, battery.getLevel());

        // 여러 번 충전
        for (int i = 0; i < 5; i++) {
            battery.charge();
        }
        assertEquals(100, battery.getLevel());
        assertEquals(RobotWorker.BatterySimulation.BatteryStatus.FULL, battery.getStatus());
    }

    @Test
    @DisplayName("배터리 상태 업데이트 테스트")
    void testBatteryStatusUpdate() {
        RobotWorker.BatterySimulation battery = RobotWorker.BatterySimulation.builder()
                .level(95)
                .status(RobotWorker.BatterySimulation.BatteryStatus.GOOD)
                .build();

        battery.updateStatus();
        assertEquals(RobotWorker.BatterySimulation.BatteryStatus.FULL, battery.getStatus());

        battery.setLevel(60);
        battery.updateStatus();
        assertEquals(RobotWorker.BatterySimulation.BatteryStatus.GOOD, battery.getStatus());

        battery.setLevel(30);
        battery.updateStatus();
        assertEquals(RobotWorker.BatterySimulation.BatteryStatus.LOW, battery.getStatus());

        battery.setLevel(10);
        battery.updateStatus();
        assertEquals(RobotWorker.BatterySimulation.BatteryStatus.CRITICAL, battery.getStatus());
    }

    @Test
    @DisplayName("배터리 충전 필요 확인 테스트")
    void testNeedsCharging() {
        robot.getBattery().setLevel(80);
        robot.getBattery().updateStatus();
        assertFalse(robot.needsCharging());

        robot.getBattery().setLevel(30);
        robot.getBattery().updateStatus();
        assertTrue(robot.needsCharging());

        robot.getBattery().setLevel(10);
        robot.getBattery().updateStatus();
        assertTrue(robot.needsCharging());
    }
}
