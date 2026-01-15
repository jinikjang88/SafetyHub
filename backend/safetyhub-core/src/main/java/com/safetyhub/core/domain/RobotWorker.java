package com.safetyhub.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 로봇 작업자 도메인 모델
 * 실제 작업자를 시뮬레이션하는 가상 로봇 더미
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class RobotWorker {

    private Long id;
    private String robotId;              // 로봇 고유 ID
    private String name;                 // 로봇 이름
    private RobotState state;            // 로봇 상태
    private Location currentLocation;    // 현재 위치 (x, y 좌표)
    private String currentZoneId;        // 현재 구역 ID
    private String assignedZoneId;       // 할당된 작업 구역 ID
    private RobotSchedule schedule;      // 일일 스케줄
    private HealthSimulation health;     // 건강 시뮬레이션
    private BatterySimulation battery;   // 배터리 시뮬레이션
    private List<Location> plannedPath;  // 계획된 이동 경로
    private Double speed;                // 이동 속도 (m/s)
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 로봇 상태 정의
     */
    public enum RobotState {
        WORKING,      // 작업 중
        RESTING,      // 휴식 중
        EATING,       // 식사 중
        MOVING,       // 이동 중
        EMERGENCY,    // 긴급 상황
        EVACUATING,   // 대피 중
        OFFLINE       // 오프라인
    }

    /**
     * 일일 스케줄
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class RobotSchedule {
        private LocalTime workStartTime;    // 출근 시간
        private LocalTime workEndTime;      // 퇴근 시간
        private LocalTime morningBreakStart;  // 오전 휴식 시작
        private LocalTime morningBreakEnd;    // 오전 휴식 종료
        private LocalTime lunchBreakStart;    // 점심 시작
        private LocalTime lunchBreakEnd;      // 점심 종료
        private LocalTime afternoonBreakStart; // 오후 휴식 시작
        private LocalTime afternoonBreakEnd;   // 오후 휴식 종료

        /**
         * 현재 시간에 맞는 상태 결정
         */
        public RobotState getStateAtTime(LocalTime currentTime) {
            if (currentTime.isBefore(workStartTime) || currentTime.isAfter(workEndTime)) {
                return RobotState.OFFLINE;
            }

            if (isInTimeRange(currentTime, lunchBreakStart, lunchBreakEnd)) {
                return RobotState.EATING;
            }

            if (isInTimeRange(currentTime, morningBreakStart, morningBreakEnd) ||
                isInTimeRange(currentTime, afternoonBreakStart, afternoonBreakEnd)) {
                return RobotState.RESTING;
            }

            return RobotState.WORKING;
        }

        private boolean isInTimeRange(LocalTime time, LocalTime start, LocalTime end) {
            return !time.isBefore(start) && !time.isAfter(end);
        }

        /**
         * 기본 스케줄 생성
         */
        public static RobotSchedule createDefaultSchedule() {
            return RobotSchedule.builder()
                    .workStartTime(LocalTime.of(8, 0))
                    .workEndTime(LocalTime.of(17, 0))
                    .morningBreakStart(LocalTime.of(10, 0))
                    .morningBreakEnd(LocalTime.of(10, 15))
                    .lunchBreakStart(LocalTime.of(12, 0))
                    .lunchBreakEnd(LocalTime.of(13, 0))
                    .afternoonBreakStart(LocalTime.of(15, 0))
                    .afternoonBreakEnd(LocalTime.of(15, 15))
                    .build();
        }
    }

    /**
     * 건강 상태 시뮬레이션
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class HealthSimulation {
        private Integer heartRate;           // 심박수 (bpm)
        private Double bodyTemperature;      // 체온 (°C)
        private Integer oxygenSaturation;    // 산소포화도 (%)
        private HealthStatus status;         // 건강 상태

        public enum HealthStatus {
            NORMAL,          // 정상
            WARNING,         // 주의 (심박 이상 등)
            DANGER,          // 위험 (낙상 감지 등)
            SOS              // SOS 버튼 누름
        }

        /**
         * 정상 범위의 건강 상태 생성
         */
        public static HealthSimulation createNormal() {
            return HealthSimulation.builder()
                    .heartRate(70 + (int)(Math.random() * 30))  // 70-100 bpm
                    .bodyTemperature(36.0 + Math.random())       // 36.0-37.0°C
                    .oxygenSaturation(95 + (int)(Math.random() * 5)) // 95-100%
                    .status(HealthStatus.NORMAL)
                    .build();
        }

        /**
         * 위험 상태인지 확인
         */
        public boolean isDangerous() {
            return status == HealthStatus.DANGER || status == HealthStatus.SOS;
        }
    }

    /**
     * 배터리 시뮬레이션
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class BatterySimulation {
        private Integer level;           // 배터리 잔량 (%)
        private BatteryStatus status;    // 배터리 상태

        public enum BatteryStatus {
            FULL,      // 충전 완료 (90%~)
            GOOD,      // 양호 (50%~89%)
            LOW,       // 낮음 (20%~49%)
            CRITICAL   // 위험 (0%~19%)
        }

        /**
         * 배터리 상태에 따라 status 업데이트
         */
        public void updateStatus() {
            if (level >= 90) {
                status = BatteryStatus.FULL;
            } else if (level >= 50) {
                status = BatteryStatus.GOOD;
            } else if (level >= 20) {
                status = BatteryStatus.LOW;
            } else {
                status = BatteryStatus.CRITICAL;
            }
        }

        /**
         * 배터리 소모 (1% 감소)
         */
        public void discharge() {
            if (level > 0) {
                level--;
                updateStatus();
            }
        }

        /**
         * 배터리 충전 (10% 증가)
         */
        public void charge() {
            if (level < 100) {
                level = Math.min(100, level + 10);
                updateStatus();
            }
        }

        /**
         * 완전 충전 상태 생성
         */
        public static BatterySimulation createFull() {
            return BatterySimulation.builder()
                    .level(100)
                    .status(BatteryStatus.FULL)
                    .build();
        }
    }

    /**
     * 위치 업데이트
     */
    public void updateLocation(Location location, String zoneId) {
        this.currentLocation = location;
        this.currentZoneId = zoneId;
        this.lastActiveAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 상태 변경
     */
    public void changeState(RobotState newState) {
        this.state = newState;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 경로 설정
     */
    public void setPath(List<Location> path) {
        this.plannedPath = path;
        this.state = RobotState.MOVING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 긴급 상황 발생
     */
    public void triggerEmergency() {
        this.state = RobotState.EMERGENCY;
        this.health.setStatus(HealthSimulation.HealthStatus.DANGER);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 대피 시작
     */
    public void startEvacuation() {
        this.state = RobotState.EVACUATING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 위험 상태 확인
     */
    public boolean isInDanger() {
        return state == RobotState.EMERGENCY ||
               state == RobotState.EVACUATING ||
               health.isDangerous();
    }

    /**
     * 배터리 부족 확인
     */
    public boolean needsCharging() {
        return battery.getStatus() == BatterySimulation.BatteryStatus.LOW ||
               battery.getStatus() == BatterySimulation.BatteryStatus.CRITICAL;
    }

    /**
     * 이동 중 확인
     */
    public boolean isMoving() {
        return state == RobotState.MOVING && plannedPath != null && !plannedPath.isEmpty();
    }
}
