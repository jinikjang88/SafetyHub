package com.safetyhub.application.charging;

import com.safetyhub.core.domain.Location;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 충전 스케줄러
 * 충전 요청을 관리하고 충전소에 할당
 */
@Slf4j
public class ChargingScheduler {

    /**
     * 충전 대기열
     */
    private final ChargingQueue chargingQueue;

    /**
     * 충전소 목록 (Station ID -> ChargingStation)
     */
    private final Map<String, ChargingStation> chargingStations;

    /**
     * 활성 충전 세션 (Request ID -> ChargingRequest)
     */
    private final Map<String, ChargingRequest> activeChargingSessions;

    /**
     * 완료된 충전 요청 (Request ID -> ChargingRequest)
     */
    private final Map<String, ChargingRequest> completedRequests;

    /**
     * 통계
     */
    private final Statistics statistics;

    public ChargingScheduler() {
        this.chargingQueue = new ChargingQueue();
        this.chargingStations = new ConcurrentHashMap<>();
        this.activeChargingSessions = new ConcurrentHashMap<>();
        this.completedRequests = new ConcurrentHashMap<>();
        this.statistics = new Statistics();
    }

    /**
     * 충전소 등록
     */
    public void registerStation(ChargingStation station) {
        if (station == null) {
            throw new IllegalArgumentException("충전소는 null일 수 없습니다");
        }

        chargingStations.put(station.getStationId(), station);
        log.info("충전소 등록. Station: {}, Slots: {}, Location: {}",
                station.getStationId(), station.getTotalSlots(), station.getLocation());
    }

    /**
     * 충전소 제거
     */
    public void unregisterStation(String stationId) {
        ChargingStation removed = chargingStations.remove(stationId);
        if (removed != null) {
            log.info("충전소 제거. Station: {}", stationId);
        }
    }

    /**
     * 충전 요청 제출
     */
    public ChargingRequest requestCharging(String robotId, int currentBatteryLevel,
                                          Location currentLocation) {
        if (robotId == null || robotId.isEmpty()) {
            throw new IllegalArgumentException("로봇 ID는 필수입니다");
        }

        // 이미 대기 중이거나 충전 중인지 확인
        if (chargingQueue.containsRobot(robotId)) {
            throw new IllegalStateException("이미 충전 대기 중인 로봇입니다. Robot: " + robotId);
        }
        if (isRobotCharging(robotId)) {
            throw new IllegalStateException("이미 충전 중인 로봇입니다. Robot: " + robotId);
        }

        // 충전 요청 생성
        ChargingRequest request = ChargingRequest.builder()
                .robotId(robotId)
                .currentBatteryLevel(currentBatteryLevel)
                .targetBatteryLevel(100)
                .currentLocation(currentLocation)
                .build();

        chargingQueue.enqueue(request);
        statistics.incrementRequested();

        log.info("충전 요청 제출. Robot: {}, Battery: {}%, Priority: {}",
                robotId, currentBatteryLevel, request.getPriority());

        return request;
    }

    /**
     * 충전소 할당 시도
     * @return 할당된 요청, 할당 실패 시 Optional.empty()
     */
    public Optional<ChargingRequest> assignStation() {
        // 대기 중인 요청 확인
        Optional<ChargingRequest> requestOpt = chargingQueue.dequeue();
        if (requestOpt.isEmpty()) {
            return Optional.empty();
        }

        ChargingRequest request = requestOpt.get();

        // 사용 가능한 충전소 찾기
        Optional<ChargingStation> stationOpt = findAvailableStation(request.getCurrentLocation());

        if (stationOpt.isEmpty()) {
            // 사용 가능한 충전소가 없으면 다시 대기열에 추가
            chargingQueue.enqueue(request);
            log.warn("사용 가능한 충전소가 없습니다. Robot: {}, 대기열에 재추가", request.getRobotId());
            statistics.incrementFailed();
            return Optional.empty();
        }

        ChargingStation station = stationOpt.get();

        // 충전소 슬롯 점유
        ChargingStation occupiedStation = station.occupySlot(request.getRobotId());
        chargingStations.put(occupiedStation.getStationId(), occupiedStation);

        // 요청 상태 업데이트
        ChargingRequest assignedRequest = request.assignStation(station.getStationId());
        activeChargingSessions.put(assignedRequest.getRequestId(), assignedRequest);
        statistics.incrementAssigned();

        log.info("충전소 할당 성공. Robot: {}, Station: {}, Distance: {:.2f}m",
                request.getRobotId(), station.getStationId(),
                calculateDistance(request.getCurrentLocation(), station.getLocation()));

        return Optional.of(assignedRequest);
    }

    /**
     * 충전 시작
     */
    public Optional<ChargingRequest> startCharging(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("요청 ID는 필수입니다");
        }

        ChargingRequest request = activeChargingSessions.get(requestId);
        if (request == null) {
            log.warn("활성 충전 세션을 찾을 수 없습니다. Request ID: {}", requestId);
            return Optional.empty();
        }

        ChargingRequest chargingRequest = request.startCharging();
        activeChargingSessions.put(requestId, chargingRequest);

        log.info("충전 시작. Robot: {}, Station: {}, Battery: {}% -> {}%",
                chargingRequest.getRobotId(), chargingRequest.getAssignedStationId(),
                chargingRequest.getCurrentBatteryLevel(), chargingRequest.getTargetBatteryLevel());

        return Optional.of(chargingRequest);
    }

    /**
     * 충전 완료
     */
    public Optional<ChargingRequest> completeCharging(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("요청 ID는 필수입니다");
        }

        ChargingRequest request = activeChargingSessions.remove(requestId);
        if (request == null) {
            log.warn("활성 충전 세션을 찾을 수 없습니다. Request ID: {}", requestId);
            return Optional.empty();
        }

        // 충전소 슬롯 해제
        String stationId = request.getAssignedStationId();
        ChargingStation station = chargingStations.get(stationId);
        if (station != null) {
            ChargingStation releasedStation = station.releaseSlot(request.getRobotId());
            chargingStations.put(stationId, releasedStation);
        }

        // 요청 완료 처리
        ChargingRequest completedRequest = request.complete();
        completedRequests.put(requestId, completedRequest);
        statistics.incrementCompleted();

        log.info("충전 완료. Robot: {}, Station: {}",
                completedRequest.getRobotId(), stationId);

        return Optional.of(completedRequest);
    }

    /**
     * 충전 요청 취소
     */
    public Optional<ChargingRequest> cancelCharging(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("요청 ID는 필수입니다");
        }

        // 대기열에서 제거 시도
        Optional<ChargingRequest> queuedRequest = chargingQueue.remove(requestId);
        if (queuedRequest.isPresent()) {
            ChargingRequest cancelledRequest = queuedRequest.get().cancel();
            completedRequests.put(requestId, cancelledRequest);
            statistics.incrementCancelled();
            log.info("대기 중인 충전 요청 취소. Robot: {}", cancelledRequest.getRobotId());
            return Optional.of(cancelledRequest);
        }

        // 활성 세션에서 제거 시도
        ChargingRequest activeRequest = activeChargingSessions.remove(requestId);
        if (activeRequest != null) {
            // 충전소 슬롯 해제
            String stationId = activeRequest.getAssignedStationId();
            ChargingStation station = chargingStations.get(stationId);
            if (station != null) {
                ChargingStation releasedStation = station.releaseSlot(activeRequest.getRobotId());
                chargingStations.put(stationId, releasedStation);
            }

            ChargingRequest cancelledRequest = activeRequest.cancel();
            completedRequests.put(requestId, cancelledRequest);
            statistics.incrementCancelled();
            log.info("활성 충전 요청 취소. Robot: {}, Station: {}",
                    cancelledRequest.getRobotId(), stationId);
            return Optional.of(cancelledRequest);
        }

        log.warn("취소할 충전 요청을 찾을 수 없습니다. Request ID: {}", requestId);
        return Optional.empty();
    }

    /**
     * 사용 가능한 충전소 찾기 (거리 기반)
     */
    private Optional<ChargingStation> findAvailableStation(Location robotLocation) {
        return chargingStations.values().stream()
                .filter(ChargingStation::hasAvailableSlot)
                .min(Comparator.comparingDouble(station ->
                        calculateDistance(robotLocation, station.getLocation())
                ));
    }

    /**
     * 거리 계산 (유클리드 거리)
     */
    private double calculateDistance(Location from, Location to) {
        if (from == null || to == null) {
            return Double.MAX_VALUE;
        }
        return from.distanceTo(to);
    }

    /**
     * 로봇이 충전 중인지 확인
     */
    public boolean isRobotCharging(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            return false;
        }
        return activeChargingSessions.values().stream()
                .anyMatch(r -> r.getRobotId().equals(robotId));
    }

    /**
     * 대기 중인 요청 조회
     */
    public List<ChargingRequest> getPendingRequests() {
        return chargingQueue.getAllRequests();
    }

    /**
     * 활성 충전 세션 조회
     */
    public List<ChargingRequest> getActiveChargingSessions() {
        return List.copyOf(activeChargingSessions.values());
    }

    /**
     * 충전소 목록 조회
     */
    public List<ChargingStation> getChargingStations() {
        return List.copyOf(chargingStations.values());
    }

    /**
     * 사용 가능한 충전소 조회
     */
    public List<ChargingStation> getAvailableStations() {
        return chargingStations.values().stream()
                .filter(ChargingStation::hasAvailableSlot)
                .collect(Collectors.toList());
    }

    /**
     * 통계 조회
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * 통계 클래스
     */
    @Getter
    public static class Statistics {
        private final AtomicLong requested = new AtomicLong(0);
        private final AtomicLong assigned = new AtomicLong(0);
        private final AtomicLong completed = new AtomicLong(0);
        private final AtomicLong failed = new AtomicLong(0);
        private final AtomicLong cancelled = new AtomicLong(0);

        void incrementRequested() {
            requested.incrementAndGet();
        }

        void incrementAssigned() {
            assigned.incrementAndGet();
        }

        void incrementCompleted() {
            completed.incrementAndGet();
        }

        void incrementFailed() {
            failed.incrementAndGet();
        }

        void incrementCancelled() {
            cancelled.incrementAndGet();
        }

        /**
         * 완료율 계산
         */
        public double getCompletionRate() {
            long total = completed.get() + failed.get() + cancelled.get();
            if (total == 0) {
                return 0.0;
            }
            return (double) completed.get() / total * 100.0;
        }

        /**
         * 할당 성공률 계산
         */
        public double getAssignmentSuccessRate() {
            long totalAttempts = assigned.get() + failed.get();
            if (totalAttempts == 0) {
                return 0.0;
            }
            return (double) assigned.get() / totalAttempts * 100.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "Statistics{requested=%d, assigned=%d, completed=%d, failed=%d, cancelled=%d, " +
                    "completionRate=%.2f%%, assignmentSuccessRate=%.2f%%}",
                    requested.get(), assigned.get(), completed.get(), failed.get(), cancelled.get(),
                    getCompletionRate(), getAssignmentSuccessRate()
            );
        }
    }
}
