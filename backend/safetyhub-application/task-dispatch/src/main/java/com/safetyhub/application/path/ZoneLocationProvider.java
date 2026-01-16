package com.safetyhub.application.path;

import com.safetyhub.core.domain.Location;

import java.util.Optional;

/**
 * Zone ID로 위치를 조회하는 인터페이스
 * Zone Repository의 역할을 추상화
 */
public interface ZoneLocationProvider {

    /**
     * Zone ID로 중심 위치 조회
     * @param zoneId 구역 ID
     * @return 중심 위치, 없으면 Optional.empty()
     */
    Optional<Location> getZoneCenterLocation(String zoneId);
}
