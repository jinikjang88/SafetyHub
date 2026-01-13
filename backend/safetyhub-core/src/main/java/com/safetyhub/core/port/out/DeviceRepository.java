package com.safetyhub.core.port.out;

import com.safetyhub.core.domain.Device;

import java.util.List;
import java.util.Optional;

/**
 * 장치 저장소 출력 포트
 */
public interface DeviceRepository {

    Device save(Device device);

    Optional<Device> findById(Long id);

    Optional<Device> findByDeviceId(String deviceId);

    List<Device> findByZoneId(String zoneId);

    List<Device> findByStatus(Device.DeviceStatus status);

    List<Device> findByType(Device.DeviceType type);

    List<Device> findAll();

    void deleteById(Long id);

    boolean existsByDeviceId(String deviceId);
}
