package com.safetyhub.infrastructure.persistence.repository;

import com.safetyhub.core.domain.Device;
import com.safetyhub.infrastructure.persistence.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 장치 JPA Repository
 */
@Repository
public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, Long> {

    Optional<DeviceEntity> findByDeviceId(String deviceId);

    List<DeviceEntity> findByZoneId(String zoneId);

    List<DeviceEntity> findByStatus(Device.DeviceStatus status);

    List<DeviceEntity> findByType(Device.DeviceType type);

    boolean existsByDeviceId(String deviceId);
}
