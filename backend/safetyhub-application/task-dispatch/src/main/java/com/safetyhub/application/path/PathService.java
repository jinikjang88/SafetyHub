package com.safetyhub.application.path;

import com.safetyhub.core.domain.Location;

import java.util.List;
import java.util.Optional;

/**
 * 경로 계획 서비스 인터페이스
 * PathFinder를 래핑하여 캐싱 및 최적화 기능 제공
 */
public interface PathService {

    /**
     * 두 위치 간의 최단 경로 찾기
     * @param start 시작 위치
     * @param goal 목표 위치
     * @return 경로 정보, 경로를 찾지 못하면 Optional.empty()
     */
    Optional<PathResult> findPath(Location start, Location goal);

    /**
     * 두 구역 간의 최단 경로 찾기 (구역 ID 사용)
     * @param startZoneId 시작 구역 ID
     * @param goalZoneId 목표 구역 ID
     * @return 경로 정보, 경로를 찾지 못하면 Optional.empty()
     */
    Optional<PathResult> findPath(String startZoneId, String goalZoneId);

    /**
     * 경로의 예상 이동 시간 계산
     * @param start 시작 위치
     * @param goal 목표 위치
     * @param speedMeterPerSecond 속도 (미터/초)
     * @return 예상 이동 시간 (초), 경로를 찾지 못하면 Optional.empty()
     */
    Optional<Double> estimateTravelTime(Location start, Location goal, double speedMeterPerSecond);

    /**
     * 경로 캐시 무효화
     * @param startZoneId 시작 구역 ID
     * @param goalZoneId 목표 구역 ID
     */
    void invalidateCache(String startZoneId, String goalZoneId);

    /**
     * 모든 경로 캐시 무효화
     * 맵이 변경되었을 때 사용
     */
    void invalidateAllCache();

    /**
     * 경로 계산 결과
     */
    interface PathResult {
        /**
         * 경로 (Location 리스트)
         */
        List<Location> getPath();

        /**
         * 총 거리 (미터)
         */
        double getTotalDistance();

        /**
         * 총 스텝 수 (경로상의 위치 개수)
         */
        int getSteps();

        /**
         * 경로가 유효한지 확인
         */
        boolean isValid();

        /**
         * 경로가 캐시에서 온 것인지 확인
         */
        boolean isCached();

        /**
         * 경로 계산 시간 (밀리초)
         */
        long getCalculationTimeMs();
    }
}
