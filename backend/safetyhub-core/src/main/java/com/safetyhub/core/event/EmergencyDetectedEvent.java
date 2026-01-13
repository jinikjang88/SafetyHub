package com.safetyhub.core.event;

import com.safetyhub.core.domain.Emergency;
import com.safetyhub.core.domain.Location;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 긴급 상황 감지 이벤트
 * Hot Path - 즉시 처리 필요
 */
@Getter
@Builder
public class EmergencyDetectedEvent implements DomainEvent {

    private final String eventId;
    private final String emergencyId;
    private final Emergency.EmergencyType emergencyType;
    private final Emergency.EmergencyLevel level;
    private final String zoneId;
    private final Location location;
    private final String reporterId;
    private final Emergency.ReporterType reporterType;
    private final String description;
    private final LocalDateTime occurredAt;

    public static EmergencyDetectedEvent create(
            Emergency.EmergencyType type,
            Emergency.EmergencyLevel level,
            String zoneId,
            Location location,
            String reporterId,
            Emergency.ReporterType reporterType,
            String description) {

        return EmergencyDetectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .emergencyId(UUID.randomUUID().toString())
                .emergencyType(type)
                .level(level)
                .zoneId(zoneId)
                .location(location)
                .reporterId(reporterId)
                .reporterType(reporterType)
                .description(description)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    public String getEventType() {
        return "EMERGENCY_DETECTED";
    }

    @Override
    public EventPriority getPriority() {
        return switch (level) {
            case CRITICAL -> EventPriority.CRITICAL;
            case HIGH -> EventPriority.HIGH;
            default -> EventPriority.NORMAL;
        };
    }
}
