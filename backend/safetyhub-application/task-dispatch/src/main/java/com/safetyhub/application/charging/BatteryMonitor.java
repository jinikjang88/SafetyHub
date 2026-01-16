package com.safetyhub.application.charging;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 배터리 모니터링 서비스
 * 로봇의 배터리 상태를 추적하고 충전 필요 여부를 판단
 */
@Slf4j
public class BatteryMonitor {

    /**
     * 로봇별 배터리 상태 (Robot ID -> BatteryStatus)
     */
    private final Map<String, BatteryStatus> batteryStatuses;

    /**
     * 저배터리 임계값 (%)
     */
    private static final int LOW_BATTERY_THRESHOLD = 20;

    /**
     * 주의 배터리 임계값 (%)
     */
    private static final int WARNING_BATTERY_THRESHOLD = 50;

    /**
     * 배터리 이벤트 리스너
     */
    private final List<BatteryEventListener> eventListeners;

    public BatteryMonitor() {
        this.batteryStatuses = new ConcurrentHashMap<>();
        this.eventListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    }

    /**
     * 배터리 상태 업데이트
     * @param robotId 로봇 ID
     * @param batteryLevel 배터리 레벨 (0-100)
     */
    public void updateBatteryStatus(String robotId, int batteryLevel) {
        if (robotId == null || robotId.isEmpty()) {
            throw new IllegalArgumentException("로봇 ID는 null이거나 비어있을 수 없습니다");
        }
        if (batteryLevel < 0 || batteryLevel > 100) {
            throw new IllegalArgumentException("배터리 레벨은 0-100 사이여야 합니다. 현재 값: " + batteryLevel);
        }

        // 이전 상태 조회
        BatteryStatus oldStatus = batteryStatuses.get(robotId);
        BatteryStatus.BatteryState oldState = oldStatus != null ? oldStatus.getState() : null;

        // 새 상태 생성
        BatteryStatus.BatteryState newState = BatteryStatus.determineState(batteryLevel);
        BatteryStatus newStatus = BatteryStatus.builder()
                .robotId(robotId)
                .batteryLevel(batteryLevel)
                .state(newState)
                .estimatedTimeToChargingNeeded(-1) // 추후 구현
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        batteryStatuses.put(robotId, newStatus);

        log.debug("배터리 상태 업데이트. Robot: {}, Level: {}%, State: {}",
                robotId, batteryLevel, newState);

        // 상태 변화가 있으면 이벤트 발행
        if (oldState != newState) {
            notifyStateChange(robotId, oldState, newState, batteryLevel);
        }
    }

    /**
     * 충전 시작 알림
     * @param robotId 로봇 ID
     */
    public void notifyChargingStarted(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            return;
        }

        BatteryStatus oldStatus = batteryStatuses.get(robotId);
        if (oldStatus == null) {
            return;
        }

        BatteryStatus chargingStatus = BatteryStatus.builder()
                .robotId(robotId)
                .batteryLevel(oldStatus.getBatteryLevel())
                .state(BatteryStatus.BatteryState.CHARGING)
                .estimatedTimeToChargingNeeded(-1)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        batteryStatuses.put(robotId, chargingStatus);
        log.info("충전 시작. Robot: {}, Battery: {}%", robotId, oldStatus.getBatteryLevel());
    }

    /**
     * 충전 완료 알림
     * @param robotId 로봇 ID
     * @param finalBatteryLevel 충전 후 배터리 레벨
     */
    public void notifyChargingCompleted(String robotId, int finalBatteryLevel) {
        if (robotId == null || robotId.isEmpty()) {
            return;
        }

        updateBatteryStatus(robotId, finalBatteryLevel);
        log.info("충전 완료. Robot: {}, Battery: {}%", robotId, finalBatteryLevel);
    }

    /**
     * 배터리 상태 조회
     * @param robotId 로봇 ID
     * @return 배터리 상태
     */
    public Optional<BatteryStatus> getBatteryStatus(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(batteryStatuses.get(robotId));
    }

    /**
     * 충전이 필요한 로봇 조회
     * @return 충전이 필요한 로봇 ID 목록
     */
    public List<String> getRobotsNeedingCharging() {
        return batteryStatuses.values().stream()
                .filter(BatteryStatus::needsCharging)
                .map(BatteryStatus::getRobotId)
                .collect(Collectors.toList());
    }

    /**
     * 긴급 충전이 필요한 로봇 조회
     * @return 긴급 충전이 필요한 로봇 ID 목록
     */
    public List<String> getRobotsNeedingUrgentCharging() {
        return batteryStatuses.values().stream()
                .filter(BatteryStatus::needsUrgentCharging)
                .map(BatteryStatus::getRobotId)
                .collect(Collectors.toList());
    }

    /**
     * 충전 중인 로봇 조회
     * @return 충전 중인 로봇 ID 목록
     */
    public List<String> getChargingRobots() {
        return batteryStatuses.values().stream()
                .filter(BatteryStatus::isCharging)
                .map(BatteryStatus::getRobotId)
                .collect(Collectors.toList());
    }

    /**
     * 모든 배터리 상태 조회
     * @return 모든 배터리 상태 목록
     */
    public List<BatteryStatus> getAllBatteryStatuses() {
        return List.copyOf(batteryStatuses.values());
    }

    /**
     * 배터리 이벤트 리스너 추가
     */
    public void addEventListener(BatteryEventListener listener) {
        if (listener != null) {
            eventListeners.add(listener);
        }
    }

    /**
     * 배터리 이벤트 리스너 제거
     */
    public void removeEventListener(BatteryEventListener listener) {
        eventListeners.remove(listener);
    }

    /**
     * 상태 변화 알림
     */
    private void notifyStateChange(String robotId, BatteryStatus.BatteryState oldState,
                                   BatteryStatus.BatteryState newState, int batteryLevel) {
        log.info("배터리 상태 변화. Robot: {}, {} -> {}, Level: {}%",
                robotId, oldState, newState, batteryLevel);

        // 리스너에게 알림
        BatteryEvent event = new BatteryEvent(robotId, oldState, newState, batteryLevel);
        for (BatteryEventListener listener : eventListeners) {
            try {
                listener.onBatteryStateChanged(event);
            } catch (Exception e) {
                log.error("배터리 이벤트 리스너 실행 중 오류. Robot: {}", robotId, e);
            }
        }
    }

    /**
     * 배터리 이벤트
     */
    public record BatteryEvent(
            String robotId,
            BatteryStatus.BatteryState oldState,
            BatteryStatus.BatteryState newState,
            int batteryLevel
    ) {
        public boolean isLowBatteryAlert() {
            return newState == BatteryStatus.BatteryState.CRITICAL ||
                   newState == BatteryStatus.BatteryState.WARNING;
        }

        public boolean isCriticalAlert() {
            return newState == BatteryStatus.BatteryState.CRITICAL;
        }
    }

    /**
     * 배터리 이벤트 리스너 인터페이스
     */
    @FunctionalInterface
    public interface BatteryEventListener {
        void onBatteryStateChanged(BatteryEvent event);
    }
}
