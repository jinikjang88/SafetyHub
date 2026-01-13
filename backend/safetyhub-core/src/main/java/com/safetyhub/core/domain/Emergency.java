package com.safetyhub.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 긴급 상황 도메인 모델
 */
@Getter
@Builder
public class Emergency {

    private Long id;
    private String emergencyId;       // 긴급 상황 고유 ID
    private EmergencyType type;       // 긴급 상황 유형
    private EmergencyLevel level;     // 긴급 수준
    private EmergencyStatus status;   // 처리 상태
    private String description;       // 상황 설명
    private String zoneId;            // 발생 구역 ID
    private Location location;        // 발생 위치
    private String reporterId;        // 신고자 ID (작업자 또는 장치)
    private ReporterType reporterType;// 신고자 유형
    private List<String> affectedWorkerIds; // 영향받은 작업자 ID 목록
    private LocalDateTime occurredAt; // 발생 시각
    private LocalDateTime resolvedAt; // 해결 시각
    private String resolvedBy;        // 처리자
    private String resolution;        // 처리 내용
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum EmergencyType {
        FIRE,            // 화재
        GAS_LEAK,        // 가스 누출
        FALL,            // 낙상
        COLLISION,       // 충돌
        EQUIPMENT_FAILURE,// 설비 고장
        MEDICAL,         // 의료 응급
        SOS_BUTTON,      // SOS 버튼 호출
        UNAUTHORIZED_ACCESS, // 비인가 접근
        OTHER            // 기타
    }

    public enum EmergencyLevel {
        LOW,             // 저위험 - 모니터링
        MEDIUM,          // 중위험 - 현장 대응
        HIGH,            // 고위험 - 대피 필요
        CRITICAL         // 극고위험 - 119 신고
    }

    public enum EmergencyStatus {
        DETECTED,        // 감지됨
        CONFIRMED,       // 확인됨
        RESPONDING,      // 대응 중
        EVACUATING,      // 대피 중
        RESOLVED,        // 해결됨
        FALSE_ALARM      // 오보
    }

    public enum ReporterType {
        DEVICE,          // 장치 자동 감지
        WORKER,          // 작업자 신고
        SYSTEM,          // 시스템 감지
        MANUAL           // 수동 등록
    }

    public boolean requiresEvacuation() {
        return level == EmergencyLevel.HIGH || level == EmergencyLevel.CRITICAL;
    }

    public boolean requires119Call() {
        return level == EmergencyLevel.CRITICAL;
    }

    public boolean isActive() {
        return status != EmergencyStatus.RESOLVED && status != EmergencyStatus.FALSE_ALARM;
    }
}
