package com.safetyhub.core.port.out;

import com.safetyhub.core.domain.Zone;

import java.util.List;
import java.util.Optional;

/**
 * 구역 저장소 출력 포트
 */
public interface ZoneRepository {

    Zone save(Zone zone);

    Optional<Zone> findById(Long id);

    Optional<Zone> findByZoneId(String zoneId);

    List<Zone> findByStatus(Zone.ZoneStatus status);

    List<Zone> findByType(Zone.ZoneType type);

    List<Zone> findByRiskLevel(Zone.RiskLevel riskLevel);

    List<Zone> findAll();

    void deleteById(Long id);

    List<Zone> findDangerZones();

    List<Zone> findEvacuationRoutes();
}
