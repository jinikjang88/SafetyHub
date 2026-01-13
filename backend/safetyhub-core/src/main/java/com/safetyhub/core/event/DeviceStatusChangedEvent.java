package com.safetyhub.core.event;

import com.safetyhub.core.domain.Device;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 장치 상태 변경 이벤트
 */
@Getter
@Builder
public class DeviceStatusChangedEvent implements DomainEvent {

    private final String eventId;
    private final String deviceId;
    private final Device.DeviceType deviceType;
    private final Device.DeviceStatus previousStatus;
    private final Device.DeviceStatus currentStatus;
    private final String zoneId;
    private final String reason;
    private final LocalDateTime occurredAt;

    public static DeviceStatusChangedEvent create(
            String deviceId,
            Device.DeviceType deviceType,
            Device.DeviceStatus previousStatus,
            Device.DeviceStatus currentStatus,
            String zoneId,
            String reason) {

        return DeviceStatusChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .deviceType(deviceType)
                .previousStatus(previousStatus)
                .currentStatus(currentStatus)
                .zoneId(zoneId)
                .reason(reason)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    public String getEventType() {
        return "DEVICE_STATUS_CHANGED";
    }

    @Override
    public EventPriority getPriority() {
        if (currentStatus == Device.DeviceStatus.ERROR) {
            return EventPriority.HIGH;
        }
        return EventPriority.NORMAL;
    }
}
