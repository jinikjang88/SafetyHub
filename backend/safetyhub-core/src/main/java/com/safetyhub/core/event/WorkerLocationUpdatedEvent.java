package com.safetyhub.core.event;

import com.safetyhub.core.domain.Location;
import com.safetyhub.core.domain.Worker;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 작업자 위치 업데이트 이벤트
 */
@Getter
@Builder
public class WorkerLocationUpdatedEvent implements DomainEvent {

    private final String eventId;
    private final String workerId;
    private final Location previousLocation;
    private final Location currentLocation;
    private final String previousZoneId;
    private final String currentZoneId;
    private final Worker.HealthStatus healthStatus;
    private final LocalDateTime occurredAt;

    public static WorkerLocationUpdatedEvent create(
            String workerId,
            Location previousLocation,
            Location currentLocation,
            String previousZoneId,
            String currentZoneId,
            Worker.HealthStatus healthStatus) {

        return WorkerLocationUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .workerId(workerId)
                .previousLocation(previousLocation)
                .currentLocation(currentLocation)
                .previousZoneId(previousZoneId)
                .currentZoneId(currentZoneId)
                .healthStatus(healthStatus)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    public String getEventType() {
        return "WORKER_LOCATION_UPDATED";
    }

    public boolean hasZoneChanged() {
        if (previousZoneId == null) return currentZoneId != null;
        return !previousZoneId.equals(currentZoneId);
    }
}
