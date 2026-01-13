package com.safetyhub.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * SafetyKit 장치 도메인 모델
 * 산업 현장 설비에 부착되는 IoT 안전 장치
 */
@Getter
@Builder
public class Device {

    private Long id;
    private String deviceId;          // 장치 고유 ID
    private String name;              // 장치명
    private DeviceType type;          // 장치 타입
    private DeviceStatus status;      // 장치 상태
    private String zoneId;            // 설치 구역 ID
    private Location location;        // 설치 위치
    private LocalDateTime lastHeartbeat;  // 마지막 하트비트
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum DeviceType {
        SAFETY_KIT,      // SafetyKit (설비 부착형)
        LIFE_GUARD,      // LifeGuard Band (작업자 착용형)
        ROBOT            // 자동화 로봇
    }

    public enum DeviceStatus {
        ACTIVE,          // 정상 동작
        INACTIVE,        // 비활성
        WARNING,         // 경고 상태
        ERROR,           // 오류 상태
        MAINTENANCE      // 유지보수 중
    }

    public boolean isOnline() {
        if (lastHeartbeat == null) return false;
        return lastHeartbeat.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
