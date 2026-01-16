package com.safetyhub.application.charging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BatteryMonitor 테스트")
class BatteryMonitorTest {

    private BatteryMonitor batteryMonitor;

    @BeforeEach
    void setUp() {
        batteryMonitor = new BatteryMonitor();
    }

    @Test
    @DisplayName("배터리 상태 업데이트")
    void updateBatteryStatus() {
        // when
        batteryMonitor.updateBatteryStatus("robot-001", 80);

        // then
        Optional<BatteryStatus> status = batteryMonitor.getBatteryStatus("robot-001");
        assertTrue(status.isPresent());
        assertEquals(80, status.get().getBatteryLevel());
        assertEquals(BatteryStatus.BatteryState.HEALTHY, status.get().getState());
    }

    @Test
    @DisplayName("배터리 상태 변화 - HEALTHY -> WARNING")
    void batteryStateChange_HealthyToWarning() {
        // given
        batteryMonitor.updateBatteryStatus("robot-001", 60);

        // when
        batteryMonitor.updateBatteryStatus("robot-001", 40);

        // then
        Optional<BatteryStatus> status = batteryMonitor.getBatteryStatus("robot-001");
        assertTrue(status.isPresent());
        assertEquals(BatteryStatus.BatteryState.WARNING, status.get().getState());
    }

    @Test
    @DisplayName("배터리 상태 변화 - WARNING -> CRITICAL")
    void batteryStateChange_WarningToCritical() {
        // given
        batteryMonitor.updateBatteryStatus("robot-001", 40);

        // when
        batteryMonitor.updateBatteryStatus("robot-001", 15);

        // then
        Optional<BatteryStatus> status = batteryMonitor.getBatteryStatus("robot-001");
        assertTrue(status.isPresent());
        assertEquals(BatteryStatus.BatteryState.CRITICAL, status.get().getState());
    }

    @Test
    @DisplayName("이벤트 리스너 - 상태 변화 알림")
    void eventListener_StateChange() {
        // given
        AtomicInteger eventCount = new AtomicInteger(0);
        batteryMonitor.addEventListener(event -> {
            eventCount.incrementAndGet();
            assertEquals("robot-001", event.robotId());
        });

        batteryMonitor.updateBatteryStatus("robot-001", 60);

        // when - 상태 변화
        batteryMonitor.updateBatteryStatus("robot-001", 40);

        // then
        assertEquals(2, eventCount.get()); // HEALTHY, WARNING 두 번 발생
    }

    @Test
    @DisplayName("충전 시작 알림")
    void notifyChargingStarted() {
        // given
        batteryMonitor.updateBatteryStatus("robot-001", 30);

        // when
        batteryMonitor.notifyChargingStarted("robot-001");

        // then
        Optional<BatteryStatus> status = batteryMonitor.getBatteryStatus("robot-001");
        assertTrue(status.isPresent());
        assertEquals(BatteryStatus.BatteryState.CHARGING, status.get().getState());
        assertTrue(status.get().isCharging());
    }

    @Test
    @DisplayName("충전 완료 알림")
    void notifyChargingCompleted() {
        // given
        batteryMonitor.updateBatteryStatus("robot-001", 30);
        batteryMonitor.notifyChargingStarted("robot-001");

        // when
        batteryMonitor.notifyChargingCompleted("robot-001", 100);

        // then
        Optional<BatteryStatus> status = batteryMonitor.getBatteryStatus("robot-001");
        assertTrue(status.isPresent());
        assertEquals(100, status.get().getBatteryLevel());
        assertEquals(BatteryStatus.BatteryState.HEALTHY, status.get().getState());
    }

    @Test
    @DisplayName("충전이 필요한 로봇 조회")
    void getRobotsNeedingCharging() {
        // given
        batteryMonitor.updateBatteryStatus("robot-001", 80); // HEALTHY
        batteryMonitor.updateBatteryStatus("robot-002", 40); // WARNING
        batteryMonitor.updateBatteryStatus("robot-003", 15); // CRITICAL
        batteryMonitor.updateBatteryStatus("robot-004", 30); // WARNING

        // when
        List<String> robotsNeedingCharging = batteryMonitor.getRobotsNeedingCharging();

        // then
        assertEquals(3, robotsNeedingCharging.size());
        assertTrue(robotsNeedingCharging.contains("robot-002"));
        assertTrue(robotsNeedingCharging.contains("robot-003"));
        assertTrue(robotsNeedingCharging.contains("robot-004"));
    }

    @Test
    @DisplayName("긴급 충전이 필요한 로봇 조회")
    void getRobotsNeedingUrgentCharging() {
        // given
        batteryMonitor.updateBatteryStatus("robot-001", 80); // HEALTHY
        batteryMonitor.updateBatteryStatus("robot-002", 40); // WARNING
        batteryMonitor.updateBatteryStatus("robot-003", 15); // CRITICAL
        batteryMonitor.updateBatteryStatus("robot-004", 10); // CRITICAL

        // when
        List<String> urgentRobots = batteryMonitor.getRobotsNeedingUrgentCharging();

        // then
        assertEquals(2, urgentRobots.size());
        assertTrue(urgentRobots.contains("robot-003"));
        assertTrue(urgentRobots.contains("robot-004"));
    }

    @Test
    @DisplayName("충전 중인 로봇 조회")
    void getChargingRobots() {
        // given
        batteryMonitor.updateBatteryStatus("robot-001", 30);
        batteryMonitor.updateBatteryStatus("robot-002", 20);
        batteryMonitor.notifyChargingStarted("robot-001");
        batteryMonitor.notifyChargingStarted("robot-002");

        // when
        List<String> chargingRobots = batteryMonitor.getChargingRobots();

        // then
        assertEquals(2, chargingRobots.size());
        assertTrue(chargingRobots.contains("robot-001"));
        assertTrue(chargingRobots.contains("robot-002"));
    }

    @Test
    @DisplayName("잘못된 배터리 레벨 - 예외 발생")
    void invalidBatteryLevel_ThrowsException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                batteryMonitor.updateBatteryStatus("robot-001", -10)
        );

        assertThrows(IllegalArgumentException.class, () ->
                batteryMonitor.updateBatteryStatus("robot-001", 150)
        );
    }

    @Test
    @DisplayName("null 로봇 ID - 예외 발생")
    void nullRobotId_ThrowsException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                batteryMonitor.updateBatteryStatus(null, 50)
        );

        assertThrows(IllegalArgumentException.class, () ->
                batteryMonitor.updateBatteryStatus("", 50)
        );
    }

    @Test
    @DisplayName("BatteryStatus 상태 판별")
    void batteryStatusDetermineState() {
        // when & then
        assertEquals(BatteryStatus.BatteryState.HEALTHY, BatteryStatus.determineState(80));
        assertEquals(BatteryStatus.BatteryState.HEALTHY, BatteryStatus.determineState(51));
        assertEquals(BatteryStatus.BatteryState.WARNING, BatteryStatus.determineState(50));
        assertEquals(BatteryStatus.BatteryState.WARNING, BatteryStatus.determineState(30));
        assertEquals(BatteryStatus.BatteryState.WARNING, BatteryStatus.determineState(20));
        assertEquals(BatteryStatus.BatteryState.CRITICAL, BatteryStatus.determineState(19));
        assertEquals(BatteryStatus.BatteryState.CRITICAL, BatteryStatus.determineState(5));
        assertEquals(BatteryStatus.BatteryState.UNKNOWN, BatteryStatus.determineState(-1));
        assertEquals(BatteryStatus.BatteryState.UNKNOWN, BatteryStatus.determineState(101));
    }
}
