package com.safetyhub.application.emergency;

import com.safetyhub.core.domain.Emergency;
import com.safetyhub.core.domain.Location;

import java.util.List;
import java.util.Optional;

/**
 * 긴급 대응 UseCase 인터페이스
 * Hot Path - 최우선 처리
 */
public interface EmergencyResponseUseCase {

    /**
     * 긴급 상황 생성
     */
    Emergency createEmergency(CreateEmergencyCommand command);

    /**
     * 긴급 상황 상태 업데이트
     */
    Emergency updateEmergencyStatus(String emergencyId, Emergency.EmergencyStatus status);

    /**
     * 긴급 상황 해결
     */
    Emergency resolveEmergency(String emergencyId, String resolvedBy, String resolution);

    /**
     * 긴급 상황 조회
     */
    Optional<Emergency> getEmergency(String emergencyId);

    /**
     * 활성 긴급 상황 목록 조회
     */
    List<Emergency> getActiveEmergencies();

    /**
     * 구역별 긴급 상황 조회
     */
    List<Emergency> getEmergenciesByZone(String zoneId);

    /**
     * 대피 명령 발행
     */
    void issueEvacuationOrder(String emergencyId, List<String> zoneIds);

    /**
     * 119 신고
     */
    void call119(String emergencyId);

    /**
     * 긴급 상황 생성 커맨드
     */
    record CreateEmergencyCommand(
            Emergency.EmergencyType type,
            Emergency.EmergencyLevel level,
            String description,
            String zoneId,
            Location location,
            String reporterId,
            Emergency.ReporterType reporterType
    ) {}
}
