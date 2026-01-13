package com.safetyhub.infrastructure.persistence.repository;

import com.safetyhub.core.domain.Device;
import com.safetyhub.core.port.out.DeviceRepository;
import com.safetyhub.infrastructure.persistence.entity.DeviceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 장치 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository {

    private final DeviceJpaRepository jpaRepository;

    @Override
    public Device save(Device device) {
        DeviceEntity entity = DeviceEntity.fromDomain(device);
        DeviceEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Device> findById(Long id) {
        return jpaRepository.findById(id)
                .map(DeviceEntity::toDomain);
    }

    @Override
    public Optional<Device> findByDeviceId(String deviceId) {
        return jpaRepository.findByDeviceId(deviceId)
                .map(DeviceEntity::toDomain);
    }

    @Override
    public List<Device> findByZoneId(String zoneId) {
        return jpaRepository.findByZoneId(zoneId).stream()
                .map(DeviceEntity::toDomain)
                .toList();
    }

    @Override
    public List<Device> findByStatus(Device.DeviceStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(DeviceEntity::toDomain)
                .toList();
    }

    @Override
    public List<Device> findByType(Device.DeviceType type) {
        return jpaRepository.findByType(type).stream()
                .map(DeviceEntity::toDomain)
                .toList();
    }

    @Override
    public List<Device> findAll() {
        return jpaRepository.findAll().stream()
                .map(DeviceEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByDeviceId(String deviceId) {
        return jpaRepository.existsByDeviceId(deviceId);
    }
}
