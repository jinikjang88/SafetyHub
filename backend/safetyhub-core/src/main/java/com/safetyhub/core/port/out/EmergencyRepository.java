package com.safetyhub.core.port.out;

import com.safetyhub.core.domain.Emergency;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 긴급 상황 저장소 출력 포트
 */
public interface EmergencyRepository {

    Emergency save(Emergency emergency);

    Optional<Emergency> findById(Long id);

    Optional<Emergency> findByEmergencyId(String emergencyId);

    List<Emergency> findByZoneId(String zoneId);

    List<Emergency> findByStatus(Emergency.EmergencyStatus status);

    List<Emergency> findByLevel(Emergency.EmergencyLevel level);

    List<Emergency> findActiveEmergencies();

    List<Emergency> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);

    void deleteById(Long id);
}
