package com.safetyhub.application.path;

import com.safetyhub.core.domain.GridMap;
import com.safetyhub.core.domain.Location;
import com.safetyhub.core.domain.PathFinder;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 경로 계획 서비스 구현
 * PathFinder를 래핑하여 서비스 레이어 제공
 */
@Slf4j
public class PathServiceImpl implements PathService {

    private final PathFinder pathFinder;
    private final ZoneLocationProvider zoneLocationProvider;

    /**
     * 생성자
     * @param gridMap 그리드 맵
     * @param zoneLocationProvider Zone 위치 제공자
     */
    public PathServiceImpl(GridMap gridMap, ZoneLocationProvider zoneLocationProvider) {
        if (gridMap == null) {
            throw new IllegalArgumentException("gridMap은 null일 수 없습니다");
        }
        if (zoneLocationProvider == null) {
            throw new IllegalArgumentException("zoneLocationProvider는 null일 수 없습니다");
        }

        this.pathFinder = new PathFinder(gridMap);
        this.zoneLocationProvider = zoneLocationProvider;
    }

    /**
     * PathFinder로 직접 생성하는 생성자 (테스트용)
     */
    public PathServiceImpl(PathFinder pathFinder, ZoneLocationProvider zoneLocationProvider) {
        if (pathFinder == null) {
            throw new IllegalArgumentException("pathFinder는 null일 수 없습니다");
        }
        if (zoneLocationProvider == null) {
            throw new IllegalArgumentException("zoneLocationProvider는 null일 수 없습니다");
        }

        this.pathFinder = pathFinder;
        this.zoneLocationProvider = zoneLocationProvider;
    }

    @Override
    public Optional<PathResult> findPath(Location start, Location goal) {
        // 입력 검증
        if (start == null || goal == null) {
            log.warn("시작 위치 또는 목표 위치가 null입니다");
            return Optional.empty();
        }

        // 경로 계산 시간 측정 시작
        long startTime = System.currentTimeMillis();

        try {
            // PathFinder로 경로 찾기
            PathFinder.PathInfo pathInfo = pathFinder.findPathWithInfo(start, goal);

            // 경로 계산 시간 측정 종료
            long calculationTime = System.currentTimeMillis() - startTime;

            if (!pathInfo.isValid()) {
                log.warn("경로를 찾을 수 없습니다. Start: {}, Goal: {}", start, goal);
                return Optional.empty();
            }

            // PathResult로 변환
            PathResult result = SimplePathResult.from(pathInfo, false, calculationTime);

            log.debug("경로 계산 완료. Distance: {:.2f}m, Steps: {}, Time: {}ms",
                    result.getTotalDistance(), result.getSteps(), calculationTime);

            return Optional.of(result);

        } catch (Exception e) {
            log.error("경로 계산 중 오류 발생. Start: {}, Goal: {}", start, goal, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PathResult> findPath(String startZoneId, String goalZoneId) {
        // 입력 검증
        if (startZoneId == null || startZoneId.isEmpty()) {
            log.warn("시작 구역 ID가 null이거나 비어있습니다");
            return Optional.empty();
        }
        if (goalZoneId == null || goalZoneId.isEmpty()) {
            log.warn("목표 구역 ID가 null이거나 비어있습니다");
            return Optional.empty();
        }

        // 같은 구역이면 빈 경로 반환
        if (startZoneId.equals(goalZoneId)) {
            log.debug("시작 구역과 목표 구역이 같습니다. Zone ID: {}", startZoneId);
            return Optional.empty();
        }

        // Zone ID를 Location으로 변환
        Optional<Location> startLocation = zoneLocationProvider.getZoneCenterLocation(startZoneId);
        Optional<Location> goalLocation = zoneLocationProvider.getZoneCenterLocation(goalZoneId);

        if (startLocation.isEmpty()) {
            log.warn("시작 구역을 찾을 수 없습니다. Zone ID: {}", startZoneId);
            return Optional.empty();
        }
        if (goalLocation.isEmpty()) {
            log.warn("목표 구역을 찾을 수 없습니다. Zone ID: {}", goalZoneId);
            return Optional.empty();
        }

        // 경로 찾기
        return findPath(startLocation.get(), goalLocation.get());
    }

    @Override
    public Optional<Double> estimateTravelTime(Location start, Location goal, double speedMeterPerSecond) {
        // 입력 검증
        if (speedMeterPerSecond <= 0) {
            log.warn("속도는 0보다 커야 합니다. Speed: {}", speedMeterPerSecond);
            return Optional.empty();
        }

        // 경로 찾기
        Optional<PathResult> pathResult = findPath(start, goal);

        if (pathResult.isEmpty()) {
            return Optional.empty();
        }

        // 이동 시간 계산 (거리 / 속도)
        double travelTime = pathResult.get().getTotalDistance() / speedMeterPerSecond;

        log.debug("이동 시간 계산. Distance: {:.2f}m, Speed: {:.2f}m/s, Time: {:.2f}s",
                pathResult.get().getTotalDistance(), speedMeterPerSecond, travelTime);

        return Optional.of(travelTime);
    }

    @Override
    public void invalidateCache(String startZoneId, String goalZoneId) {
        // 기본 구현에서는 캐시가 없으므로 아무것도 하지 않음
        log.debug("PathServiceImpl은 캐시를 사용하지 않습니다. Cache invalidation 무시");
    }

    @Override
    public void invalidateAllCache() {
        // 기본 구현에서는 캐시가 없으므로 아무것도 하지 않음
        log.debug("PathServiceImpl은 캐시를 사용하지 않습니다. Cache invalidation 무시");
    }
}
