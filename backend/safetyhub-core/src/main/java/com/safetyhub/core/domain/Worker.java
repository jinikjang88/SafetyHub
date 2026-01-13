package com.safetyhub.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 작업자 도메인 모델
 * LifeGuard Band를 착용한 현장 작업자
 */
@Getter
@Builder
public class Worker {

    private Long id;
    private String workerId;          // 작업자 고유 ID
    private String name;              // 작업자명
    private String department;        // 소속 부서
    private String phoneNumber;       // 연락처
    private String deviceId;          // LifeGuard Band 장치 ID
    private WorkerStatus status;      // 작업자 상태
    private HealthStatus healthStatus; // 건강 상태
    private Location currentLocation; // 현재 위치
    private String currentZoneId;     // 현재 구역 ID
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum WorkerStatus {
        ON_DUTY,         // 근무 중
        OFF_DUTY,        // 퇴근
        BREAK,           // 휴식 중
        EMERGENCY,       // 긴급 상황
        EVACUATING       // 대피 중
    }

    public enum HealthStatus {
        NORMAL,          // 정상
        WARNING,         // 주의 (심박 이상 등)
        DANGER,          // 위험 (낙상 감지 등)
        SOS              // SOS 버튼 누름
    }

    public boolean isInDanger() {
        return healthStatus == HealthStatus.DANGER || healthStatus == HealthStatus.SOS;
    }

    public boolean needsAttention() {
        return healthStatus != HealthStatus.NORMAL || status == WorkerStatus.EMERGENCY;
    }

    public void updateLocation(Location location, String zoneId) {
        this.currentLocation = location;
        this.currentZoneId = zoneId;
        this.lastActiveAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
