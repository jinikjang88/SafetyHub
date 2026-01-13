package com.safetyhub.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 구역 도메인 모델
 * 작업 현장의 물리적 구역
 */
@Getter
@Builder
public class Zone {

    private Long id;
    private String zoneId;            // 구역 고유 ID
    private String name;              // 구역명
    private ZoneType type;            // 구역 타입
    private ZoneStatus status;        // 구역 상태
    private RiskLevel riskLevel;      // 위험 수준
    private Integer maxCapacity;      // 최대 수용 인원
    private Integer currentWorkerCount; // 현재 작업자 수
    private List<String> deviceIds;   // 구역 내 장치 ID 목록
    private Location centerLocation;  // 중심 위치
    private Double radius;            // 반경 (미터)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ZoneType {
        WORK_AREA,       // 작업 구역
        REST_AREA,       // 휴게 구역
        DANGER_ZONE,     // 위험 구역
        EVACUATION_ROUTE,// 대피 경로
        ASSEMBLY_POINT   // 집결지
    }

    public enum ZoneStatus {
        NORMAL,          // 정상
        WARNING,         // 주의
        EMERGENCY,       // 긴급
        RESTRICTED,      // 출입 제한
        EVACUATING       // 대피 중
    }

    public enum RiskLevel {
        LOW,             // 저위험
        MEDIUM,          // 중위험
        HIGH,            // 고위험
        CRITICAL         // 극고위험
    }

    public boolean isOverCapacity() {
        return currentWorkerCount != null && maxCapacity != null
                && currentWorkerCount > maxCapacity;
    }

    public boolean isDangerZone() {
        return type == ZoneType.DANGER_ZONE || riskLevel == RiskLevel.CRITICAL;
    }
}
