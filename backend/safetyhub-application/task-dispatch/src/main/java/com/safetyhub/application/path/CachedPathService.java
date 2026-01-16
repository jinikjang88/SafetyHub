package com.safetyhub.application.path;

import com.safetyhub.core.domain.Location;
import com.safetyhub.infrastructure.cache.CacheService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;

/**
 * 캐싱 기능이 추가된 경로 계획 서비스
 * 데코레이터 패턴을 사용하여 PathService에 캐싱 레이어 추가
 */
@Slf4j
public class CachedPathService implements PathService {

    private final PathService delegate;
    private final CacheService cacheService;

    /**
     * 캐시 TTL (5분)
     * 맵이 자주 변경되지 않는다고 가정
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    /**
     * 캐시 키 접두사
     */
    private static final String CACHE_KEY_PREFIX = "path";

    public CachedPathService(PathService delegate, CacheService cacheService) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate PathService는 null일 수 없습니다");
        }
        if (cacheService == null) {
            throw new IllegalArgumentException("cacheService는 null일 수 없습니다");
        }

        this.delegate = delegate;
        this.cacheService = cacheService;
    }

    @Override
    public Optional<PathResult> findPath(Location start, Location goal) {
        // Location 기반 경로는 캐싱하지 않음
        // (위치가 정확하게 일치하는 경우가 드물기 때문)
        return delegate.findPath(start, goal);
    }

    @Override
    public Optional<PathResult> findPath(String startZoneId, String goalZoneId) {
        // 입력 검증
        if (startZoneId == null || startZoneId.isEmpty() ||
            goalZoneId == null || goalZoneId.isEmpty()) {
            return Optional.empty();
        }

        // 캐시 키 생성
        String cacheKey = buildCacheKey(startZoneId, goalZoneId);

        try {
            // 1. 캐시에서 조회
            Optional<CachedPathResult> cachedResult = cacheService.get(cacheKey, CachedPathResult.class);

            if (cachedResult.isPresent() && cachedResult.get().isValid()) {
                log.debug("캐시 히트. Key: {}", cacheKey);
                // cached 플래그를 true로 설정하여 반환
                return Optional.of(cachedResult.get().withCached(true));
            }

            log.debug("캐시 미스. Key: {}", cacheKey);

            // 2. 캐시 미스 - delegate에서 경로 계산
            Optional<PathResult> result = delegate.findPath(startZoneId, goalZoneId);

            if (result.isEmpty()) {
                return Optional.empty();
            }

            // 3. 결과를 캐시에 저장
            CachedPathResult cacheableResult = CachedPathResult.from(result.get());
            cacheService.put(cacheKey, cacheableResult, CACHE_TTL);

            log.debug("경로를 캐시에 저장. Key: {}, TTL: {}분", cacheKey, CACHE_TTL.toMinutes());

            return result;

        } catch (Exception e) {
            log.error("캐시 처리 중 오류 발생. Key: {}", cacheKey, e);
            // 캐시 오류 시 원본 서비스로 fallback
            return delegate.findPath(startZoneId, goalZoneId);
        }
    }

    @Override
    public Optional<Double> estimateTravelTime(Location start, Location goal, double speedMeterPerSecond) {
        // 이동 시간 예측은 캐싱하지 않음 (경로 캐시를 재사용)
        return delegate.estimateTravelTime(start, goal, speedMeterPerSecond);
    }

    @Override
    public void invalidateCache(String startZoneId, String goalZoneId) {
        if (startZoneId == null || startZoneId.isEmpty() ||
            goalZoneId == null || goalZoneId.isEmpty()) {
            return;
        }

        String cacheKey = buildCacheKey(startZoneId, goalZoneId);

        try {
            cacheService.delete(cacheKey);
            log.info("경로 캐시 무효화. Key: {}", cacheKey);
        } catch (Exception e) {
            log.error("캐시 무효화 중 오류 발생. Key: {}", cacheKey, e);
        }

        // delegate의 캐시도 무효화
        delegate.invalidateCache(startZoneId, goalZoneId);
    }

    @Override
    public void invalidateAllCache() {
        try {
            // path:* 패턴으로 모든 경로 캐시 삭제
            String pattern = CACHE_KEY_PREFIX + ":*";
            cacheService.deleteByPattern(pattern);
            log.info("모든 경로 캐시 무효화. Pattern: {}", pattern);
        } catch (Exception e) {
            log.error("전체 캐시 무효화 중 오류 발생", e);
        }

        // delegate의 캐시도 무효화
        delegate.invalidateAllCache();
    }

    /**
     * 캐시 키 생성
     * 형식: path:{startZoneId}:{goalZoneId}
     */
    private String buildCacheKey(String startZoneId, String goalZoneId) {
        return String.format("%s:%s:%s", CACHE_KEY_PREFIX, startZoneId, goalZoneId);
    }

    /**
     * 캐시 통계 조회 (디버깅용)
     */
    public CacheStats getCacheStats(String startZoneId, String goalZoneId) {
        String cacheKey = buildCacheKey(startZoneId, goalZoneId);
        boolean exists = cacheService.exists(cacheKey);
        long ttl = exists ? cacheService.getExpire(cacheKey).getSeconds() : -1;

        return new CacheStats(exists, ttl);
    }

    /**
     * 캐시 통계 클래스
     */
    public record CacheStats(boolean exists, long ttlSeconds) {
    }
}
