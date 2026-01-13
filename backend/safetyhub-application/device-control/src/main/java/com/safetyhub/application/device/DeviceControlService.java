package com.safetyhub.application.device;

import com.safetyhub.core.domain.Device;
import com.safetyhub.core.domain.Location;
import com.safetyhub.core.event.DeviceStatusChangedEvent;
import com.safetyhub.core.port.out.DeviceRepository;
import com.safetyhub.core.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 설비 제어 서비스 구현
 */
@Service
@RequiredArgsConstructor
public class DeviceControlService implements DeviceControlUseCase {

    private final DeviceRepository deviceRepository;
    private final EventPublisher eventPublisher;

    @Override
    public Device registerDevice(RegisterDeviceCommand command) {
        if (deviceRepository.existsByDeviceId(command.deviceId())) {
            throw new IllegalArgumentException("Device already exists: " + command.deviceId());
        }

        Device device = Device.builder()
                .deviceId(command.deviceId())
                .name(command.name())
                .type(command.type())
                .status(Device.DeviceStatus.INACTIVE)
                .zoneId(command.zoneId())
                .location(Location.builder()
                        .latitude(command.latitude())
                        .longitude(command.longitude())
                        .build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return deviceRepository.save(device);
    }

    @Override
    public Device updateDeviceStatus(String deviceId, Device.DeviceStatus status) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

        Device.DeviceStatus previousStatus = device.getStatus();

        Device updatedDevice = Device.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .name(device.getName())
                .type(device.getType())
                .status(status)
                .zoneId(device.getZoneId())
                .location(device.getLocation())
                .lastHeartbeat(device.getLastHeartbeat())
                .createdAt(device.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        Device savedDevice = deviceRepository.save(updatedDevice);

        // 상태 변경 이벤트 발행
        if (previousStatus != status) {
            eventPublisher.publish(DeviceStatusChangedEvent.create(
                    deviceId,
                    device.getType(),
                    previousStatus,
                    status,
                    device.getZoneId(),
                    "Status updated"
            ));
        }

        return savedDevice;
    }

    @Override
    public Optional<Device> getDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    @Override
    public List<Device> getDevicesByZone(String zoneId) {
        return deviceRepository.findByZoneId(zoneId);
    }

    @Override
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Override
    public void processHeartbeat(String deviceId) {
        deviceRepository.findByDeviceId(deviceId).ifPresent(device -> {
            device.updateHeartbeat();
            deviceRepository.save(device);
        });
    }
}
