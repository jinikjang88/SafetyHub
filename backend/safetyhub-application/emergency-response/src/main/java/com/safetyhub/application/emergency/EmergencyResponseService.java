package com.safetyhub.application.emergency;

import com.safetyhub.core.domain.Emergency;
import com.safetyhub.core.event.EmergencyDetectedEvent;
import com.safetyhub.core.port.out.EmergencyRepository;
import com.safetyhub.core.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 긴급 대응 서비스 구현
 * Hot Path - 최우선 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyResponseService implements EmergencyResponseUseCase {

    private final EmergencyRepository emergencyRepository;
    private final EventPublisher eventPublisher;

    @Override
    public Emergency createEmergency(CreateEmergencyCommand command) {
        String emergencyId = UUID.randomUUID().toString();

        Emergency emergency = Emergency.builder()
                .emergencyId(emergencyId)
                .type(command.type())
                .level(command.level())
                .status(Emergency.EmergencyStatus.DETECTED)
                .description(command.description())
                .zoneId(command.zoneId())
                .location(command.location())
                .reporterId(command.reporterId())
                .reporterType(command.reporterType())
                .occurredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Emergency savedEmergency = emergencyRepository.save(emergency);

        // 긴급 상황 감지 이벤트 발행 (Hot Path)
        eventPublisher.publish(EmergencyDetectedEvent.create(
                command.type(),
                command.level(),
                command.zoneId(),
                command.location(),
                command.reporterId(),
                command.reporterType(),
                command.description()
        ));

        log.warn("Emergency created: {} - {} - Level: {}",
                emergencyId, command.type(), command.level());

        // CRITICAL 레벨인 경우 즉시 119 신고
        if (command.level() == Emergency.EmergencyLevel.CRITICAL) {
            call119(emergencyId);
        }

        return savedEmergency;
    }

    @Override
    public Emergency updateEmergencyStatus(String emergencyId, Emergency.EmergencyStatus status) {
        Emergency emergency = emergencyRepository.findByEmergencyId(emergencyId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency not found: " + emergencyId));

        Emergency updatedEmergency = Emergency.builder()
                .id(emergency.getId())
                .emergencyId(emergency.getEmergencyId())
                .type(emergency.getType())
                .level(emergency.getLevel())
                .status(status)
                .description(emergency.getDescription())
                .zoneId(emergency.getZoneId())
                .location(emergency.getLocation())
                .reporterId(emergency.getReporterId())
                .reporterType(emergency.getReporterType())
                .affectedWorkerIds(emergency.getAffectedWorkerIds())
                .occurredAt(emergency.getOccurredAt())
                .resolvedAt(emergency.getResolvedAt())
                .resolvedBy(emergency.getResolvedBy())
                .resolution(emergency.getResolution())
                .createdAt(emergency.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        return emergencyRepository.save(updatedEmergency);
    }

    @Override
    public Emergency resolveEmergency(String emergencyId, String resolvedBy, String resolution) {
        Emergency emergency = emergencyRepository.findByEmergencyId(emergencyId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency not found: " + emergencyId));

        Emergency resolvedEmergency = Emergency.builder()
                .id(emergency.getId())
                .emergencyId(emergency.getEmergencyId())
                .type(emergency.getType())
                .level(emergency.getLevel())
                .status(Emergency.EmergencyStatus.RESOLVED)
                .description(emergency.getDescription())
                .zoneId(emergency.getZoneId())
                .location(emergency.getLocation())
                .reporterId(emergency.getReporterId())
                .reporterType(emergency.getReporterType())
                .affectedWorkerIds(emergency.getAffectedWorkerIds())
                .occurredAt(emergency.getOccurredAt())
                .resolvedAt(LocalDateTime.now())
                .resolvedBy(resolvedBy)
                .resolution(resolution)
                .createdAt(emergency.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("Emergency resolved: {} by {}", emergencyId, resolvedBy);

        return emergencyRepository.save(resolvedEmergency);
    }

    @Override
    public Optional<Emergency> getEmergency(String emergencyId) {
        return emergencyRepository.findByEmergencyId(emergencyId);
    }

    @Override
    public List<Emergency> getActiveEmergencies() {
        return emergencyRepository.findActiveEmergencies();
    }

    @Override
    public List<Emergency> getEmergenciesByZone(String zoneId) {
        return emergencyRepository.findByZoneId(zoneId);
    }

    @Override
    public void issueEvacuationOrder(String emergencyId, List<String> zoneIds) {
        log.warn("EVACUATION ORDER issued for emergency: {} - Zones: {}", emergencyId, zoneIds);
        // TODO: 대피 명령 이벤트 발행
        // TODO: 해당 구역 작업자들에게 알림 전송
        // TODO: 대피 경로 안내
    }

    @Override
    public void call119(String emergencyId) {
        log.error("119 EMERGENCY CALL for: {}", emergencyId);
        // TODO: 119 API 연동
        // TODO: 위치 정보 전송
        // TODO: 상황 정보 전송
    }
}
