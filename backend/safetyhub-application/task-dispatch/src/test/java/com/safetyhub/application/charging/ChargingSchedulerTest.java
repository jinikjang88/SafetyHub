package com.safetyhub.application.charging;

import com.safetyhub.core.domain.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChargingScheduler 통합 테스트")
class ChargingSchedulerTest {

    private ChargingScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ChargingScheduler();

        // 테스트용 충전소 등록
        ChargingStation station1 = ChargingStation.builder()
                .stationId("station-001")
                .name("Station 1")
                .location(new Location(10.0, 10.0))
                .zoneId("zone-001")
                .totalSlots(2)
                .chargingSpeed(10.0)
                .build();

        ChargingStation station2 = ChargingStation.builder()
                .stationId("station-002")
                .name("Station 2")
                .location(new Location(50.0, 50.0))
                .zoneId("zone-002")
                .totalSlots(1)
                .chargingSpeed(10.0)
                .build();

        scheduler.registerStation(station1);
        scheduler.registerStation(station2);
    }

    @Test
    @DisplayName("충전 요청 제출")
    void requestCharging() {
        // when
        ChargingRequest request = scheduler.requestCharging("robot-001", 30,
                new Location(10.0, 10.0));

        // then
        assertNotNull(request);
        assertEquals("robot-001", request.getRobotId());
        assertEquals(30, request.getCurrentBatteryLevel());
        assertEquals(ChargingRequest.ChargingPriority.HIGH, request.getPriority());
        assertEquals(ChargingRequest.RequestStatus.PENDING, request.getStatus());
        assertEquals(1, scheduler.getPendingRequests().size());
    }

    @Test
    @DisplayName("충전소 할당")
    void assignStation() {
        // given
        scheduler.requestCharging("robot-001", 30, new Location(10.0, 10.0));

        // when
        Optional<ChargingRequest> assigned = scheduler.assignStation();

        // then
        assertTrue(assigned.isPresent());
        assertEquals(ChargingRequest.RequestStatus.ASSIGNED, assigned.get().getStatus());
        assertNotNull(assigned.get().getAssignedStationId());
        assertEquals(1, scheduler.getActiveChargingSessions().size());
        assertEquals(0, scheduler.getPendingRequests().size());
    }

    @Test
    @DisplayName("거리 기반 충전소 선택")
    void assignStationBasedOnDistance() {
        // given
        scheduler.requestCharging("robot-001", 30, new Location(10.0, 10.0)); // station-001에 가까움
        scheduler.requestCharging("robot-002", 30, new Location(50.0, 50.0)); // station-002에 가까움

        // when
        Optional<ChargingRequest> assigned1 = scheduler.assignStation();
        Optional<ChargingRequest> assigned2 = scheduler.assignStation();

        // then
        assertTrue(assigned1.isPresent());
        assertTrue(assigned2.isPresent());
        assertEquals("station-001", assigned1.get().getAssignedStationId());
        assertEquals("station-002", assigned2.get().getAssignedStationId());
    }

    @Test
    @DisplayName("우선순위 순서대로 할당")
    void assignByPriority() {
        // given
        scheduler.requestCharging("robot-001", 70, new Location(10.0, 10.0)); // NORMAL
        scheduler.requestCharging("robot-002", 15, new Location(10.0, 10.0)); // URGENT
        scheduler.requestCharging("robot-003", 40, new Location(10.0, 10.0)); // HIGH

        // when
        Optional<ChargingRequest> first = scheduler.assignStation();
        Optional<ChargingRequest> second = scheduler.assignStation();

        // then
        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertEquals("robot-002", first.get().getRobotId()); // URGENT 우선
        assertEquals("robot-003", second.get().getRobotId()); // HIGH 다음
    }

    @Test
    @DisplayName("충전 시작")
    void startCharging() {
        // given
        scheduler.requestCharging("robot-001", 30, new Location(10.0, 10.0));
        Optional<ChargingRequest> assigned = scheduler.assignStation();
        assertTrue(assigned.isPresent());

        // when
        Optional<ChargingRequest> charging = scheduler.startCharging(assigned.get().getRequestId());

        // then
        assertTrue(charging.isPresent());
        assertEquals(ChargingRequest.RequestStatus.CHARGING, charging.get().getStatus());
    }

    @Test
    @DisplayName("충전 완료")
    void completeCharging() {
        // given
        scheduler.requestCharging("robot-001", 30, new Location(10.0, 10.0));
        Optional<ChargingRequest> assigned = scheduler.assignStation();
        assertTrue(assigned.isPresent());
        scheduler.startCharging(assigned.get().getRequestId());

        // when
        Optional<ChargingRequest> completed = scheduler.completeCharging(assigned.get().getRequestId());

        // then
        assertTrue(completed.isPresent());
        assertEquals(ChargingRequest.RequestStatus.COMPLETED, completed.get().getStatus());
        assertEquals(0, scheduler.getActiveChargingSessions().size());

        // 충전소 슬롯이 해제되었는지 확인
        List<ChargingStation> availableStations = scheduler.getAvailableStations();
        assertTrue(availableStations.stream()
                .anyMatch(s -> s.getAvailableSlots() == s.getTotalSlots()));
    }

    @Test
    @DisplayName("대기 중인 요청 취소")
    void cancelPendingRequest() {
        // given
        ChargingRequest request = scheduler.requestCharging("robot-001", 30,
                new Location(10.0, 10.0));

        // when
        Optional<ChargingRequest> cancelled = scheduler.cancelCharging(request.getRequestId());

        // then
        assertTrue(cancelled.isPresent());
        assertEquals(ChargingRequest.RequestStatus.CANCELLED, cancelled.get().getStatus());
        assertEquals(0, scheduler.getPendingRequests().size());
    }

    @Test
    @DisplayName("활성 충전 요청 취소")
    void cancelActiveRequest() {
        // given
        scheduler.requestCharging("robot-001", 30, new Location(10.0, 10.0));
        Optional<ChargingRequest> assigned = scheduler.assignStation();
        assertTrue(assigned.isPresent());

        // when
        Optional<ChargingRequest> cancelled = scheduler.cancelCharging(assigned.get().getRequestId());

        // then
        assertTrue(cancelled.isPresent());
        assertEquals(ChargingRequest.RequestStatus.CANCELLED, cancelled.get().getStatus());
        assertEquals(0, scheduler.getActiveChargingSessions().size());

        // 충전소 슬롯이 해제되었는지 확인
        List<ChargingStation> availableStations = scheduler.getAvailableStations();
        assertEquals(2, availableStations.size());
    }

    @Test
    @DisplayName("사용 가능한 충전소가 없을 때 할당 실패")
    void assignStationFails_NoAvailableStation() {
        // given - 모든 슬롯을 점유
        scheduler.requestCharging("robot-001", 30, new Location(10.0, 10.0));
        scheduler.requestCharging("robot-002", 30, new Location(10.0, 10.0));
        scheduler.requestCharging("robot-003", 30, new Location(50.0, 50.0));
        scheduler.assignStation(); // station-001 slot 1
        scheduler.assignStation(); // station-001 slot 2
        scheduler.assignStation(); // station-002 slot 1

        // 추가 요청
        scheduler.requestCharging("robot-004", 30, new Location(10.0, 10.0));

        // when
        Optional<ChargingRequest> assigned = scheduler.assignStation();

        // then
        assertFalse(assigned.isPresent());
        assertEquals(1, scheduler.getPendingRequests().size()); // 다시 대기열에 추가됨
    }

    @Test
    @DisplayName("이미 충전 중인 로봇 중복 요청 불가")
    void duplicateRequestFails() {
        // given
        scheduler.requestCharging("robot-001", 30, new Location(10.0, 10.0));

        // when & then
        assertThrows(IllegalStateException.class, () ->
                scheduler.requestCharging("robot-001", 25, new Location(10.0, 10.0))
        );
    }

    @Test
    @DisplayName("통계 - 완료율")
    void statisticsCompletionRate() {
        // given
        for (int i = 1; i <= 10; i++) {
            scheduler.requestCharging("robot-" + String.format("%03d", i), 30,
                    new Location(10.0, 10.0));
        }

        // 7개 완료, 3개 취소
        for (int i = 0; i < 7; i++) {
            Optional<ChargingRequest> assigned = scheduler.assignStation();
            if (assigned.isPresent()) {
                scheduler.startCharging(assigned.get().getRequestId());
                scheduler.completeCharging(assigned.get().getRequestId());
            }
        }

        for (int i = 0; i < 3; i++) {
            Optional<ChargingRequest> pending = scheduler.getPendingRequests().stream().findFirst();
            pending.ifPresent(r -> scheduler.cancelCharging(r.getRequestId()));
        }

        // when
        double completionRate = scheduler.getStatistics().getCompletionRate();

        // then
        assertEquals(70.0, completionRate, 0.01);
    }

    @Test
    @DisplayName("ChargingStation 시간 예측")
    void chargingStationEstimateTime() {
        // given
        ChargingStation station = ChargingStation.builder()
                .stationId("station-test")
                .totalSlots(1)
                .chargingSpeed(10.0) // 10% per minute
                .build();

        // when
        double time = station.estimateChargingTime(30, 80);

        // then
        assertEquals(5.0, time, 0.01); // (80-30) / 10 = 5 minutes
    }
}
