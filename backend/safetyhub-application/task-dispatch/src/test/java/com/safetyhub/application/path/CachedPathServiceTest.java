package com.safetyhub.application.path;

import com.safetyhub.core.domain.Location;
import com.safetyhub.infrastructure.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CachedPathService 테스트")
class CachedPathServiceTest {

    private PathService delegateService;
    private CacheService cacheService;
    private CachedPathService cachedPathService;

    @BeforeEach
    void setUp() {
        delegateService = mock(PathService.class);
        cacheService = mock(CacheService.class);
        cachedPathService = new CachedPathService(delegateService, cacheService);
    }

    @Test
    @DisplayName("캐시 미스 - 경로 계산 후 캐시에 저장")
    void cacheMiss() {
        // given
        String startZoneId = "zone-001";
        String goalZoneId = "zone-002";
        PathService.PathResult mockResult = createMockResult();

        when(cacheService.get(anyString(), eq(CachedPathResult.class)))
                .thenReturn(Optional.empty());
        when(delegateService.findPath(startZoneId, goalZoneId))
                .thenReturn(Optional.of(mockResult));

        // when
        Optional<PathService.PathResult> result = cachedPathService.findPath(startZoneId, goalZoneId);

        // then
        assertTrue(result.isPresent());
        verify(cacheService).get(eq("path:zone-001:zone-002"), eq(CachedPathResult.class));
        verify(delegateService).findPath(startZoneId, goalZoneId);
        verify(cacheService).put(eq("path:zone-001:zone-002"), any(CachedPathResult.class), eq(Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("캐시 히트 - 캐시에서 결과 반환")
    void cacheHit() {
        // given
        String startZoneId = "zone-001";
        String goalZoneId = "zone-002";
        CachedPathResult cachedResult = createCachedResult();

        when(cacheService.get(eq("path:zone-001:zone-002"), eq(CachedPathResult.class)))
                .thenReturn(Optional.of(cachedResult));

        // when
        Optional<PathService.PathResult> result = cachedPathService.findPath(startZoneId, goalZoneId);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isCached());
        verify(cacheService).get(eq("path:zone-001:zone-002"), eq(CachedPathResult.class));
        verify(delegateService, never()).findPath(anyString(), anyString());
        verify(cacheService, never()).put(anyString(), any(), any());
    }

    @Test
    @DisplayName("Location 기반 경로는 캐싱하지 않음")
    void locationBasedPathNotCached() {
        // given
        Location start = new Location(1.0, 1.0);
        Location goal = new Location(8.0, 8.0);
        PathService.PathResult mockResult = createMockResult();

        when(delegateService.findPath(start, goal))
                .thenReturn(Optional.of(mockResult));

        // when
        Optional<PathService.PathResult> result = cachedPathService.findPath(start, goal);

        // then
        assertTrue(result.isPresent());
        verify(delegateService).findPath(start, goal);
        verify(cacheService, never()).get(anyString(), any());
        verify(cacheService, never()).put(anyString(), any(), any());
    }

    @Test
    @DisplayName("캐시 무효화 - 특정 경로")
    void invalidateSpecificCache() {
        // given
        String startZoneId = "zone-001";
        String goalZoneId = "zone-002";

        // when
        cachedPathService.invalidateCache(startZoneId, goalZoneId);

        // then
        verify(cacheService).delete("path:zone-001:zone-002");
        verify(delegateService).invalidateCache(startZoneId, goalZoneId);
    }

    @Test
    @DisplayName("캐시 무효화 - 전체")
    void invalidateAllCache() {
        // when
        cachedPathService.invalidateAllCache();

        // then
        verify(cacheService).deleteByPattern("path:*");
        verify(delegateService).invalidateAllCache();
    }

    @Test
    @DisplayName("캐시 오류 시 delegate로 fallback")
    void cacheErrorFallback() {
        // given
        String startZoneId = "zone-001";
        String goalZoneId = "zone-002";
        PathService.PathResult mockResult = createMockResult();

        when(cacheService.get(anyString(), eq(CachedPathResult.class)))
                .thenThrow(new RuntimeException("캐시 오류"));
        when(delegateService.findPath(startZoneId, goalZoneId))
                .thenReturn(Optional.of(mockResult));

        // when
        Optional<PathService.PathResult> result = cachedPathService.findPath(startZoneId, goalZoneId);

        // then
        assertTrue(result.isPresent());
        verify(delegateService).findPath(startZoneId, goalZoneId);
    }

    @Test
    @DisplayName("빈 결과는 캐시하지 않음")
    void emptyResultNotCached() {
        // given
        String startZoneId = "zone-001";
        String goalZoneId = "zone-002";

        when(cacheService.get(anyString(), eq(CachedPathResult.class)))
                .thenReturn(Optional.empty());
        when(delegateService.findPath(startZoneId, goalZoneId))
                .thenReturn(Optional.empty());

        // when
        Optional<PathService.PathResult> result = cachedPathService.findPath(startZoneId, goalZoneId);

        // then
        assertFalse(result.isPresent());
        verify(delegateService).findPath(startZoneId, goalZoneId);
        verify(cacheService, never()).put(anyString(), any(), any());
    }

    @Test
    @DisplayName("캐시 통계 조회")
    void getCacheStats() {
        // given
        String startZoneId = "zone-001";
        String goalZoneId = "zone-002";

        when(cacheService.exists("path:zone-001:zone-002")).thenReturn(true);
        when(cacheService.getExpire("path:zone-001:zone-002")).thenReturn(Duration.ofMinutes(3));

        // when
        CachedPathService.CacheStats stats = cachedPathService.getCacheStats(startZoneId, goalZoneId);

        // then
        assertTrue(stats.exists());
        assertEquals(180, stats.ttlSeconds());
    }

    @Test
    @DisplayName("이동 시간 예측은 delegate 호출")
    void estimateTravelTime() {
        // given
        Location start = new Location(1.0, 1.0);
        Location goal = new Location(8.0, 8.0);
        double speed = 2.0;

        when(delegateService.estimateTravelTime(start, goal, speed))
                .thenReturn(Optional.of(5.0));

        // when
        Optional<Double> travelTime = cachedPathService.estimateTravelTime(start, goal, speed);

        // then
        assertTrue(travelTime.isPresent());
        assertEquals(5.0, travelTime.get());
        verify(delegateService).estimateTravelTime(start, goal, speed);
    }

    @Test
    @DisplayName("null 입력 처리")
    void handleNullInput() {
        // when & then
        assertFalse(cachedPathService.findPath((String) null, "zone-002").isPresent());
        assertFalse(cachedPathService.findPath("zone-001", (String) null).isPresent());
        assertFalse(cachedPathService.findPath("", "zone-002").isPresent());
        assertFalse(cachedPathService.findPath("zone-001", "").isPresent());
    }

    @Test
    @DisplayName("캐시 키 형식 검증")
    void verifyCacheKeyFormat() {
        // given
        String startZoneId = "zone-001";
        String goalZoneId = "zone-002";
        CachedPathResult cachedResult = createCachedResult();

        when(cacheService.get(anyString(), eq(CachedPathResult.class)))
                .thenReturn(Optional.of(cachedResult));

        // when
        cachedPathService.findPath(startZoneId, goalZoneId);

        // then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(cacheService).get(keyCaptor.capture(), eq(CachedPathResult.class));
        assertEquals("path:zone-001:zone-002", keyCaptor.getValue());
    }

    // 테스트용 mock 결과 생성
    private PathService.PathResult createMockResult() {
        List<Location> path = Arrays.asList(
                new Location(1.0, 1.0),
                new Location(2.0, 2.0),
                new Location(3.0, 3.0)
        );

        return SimplePathResult.builder()
                .path(path)
                .totalDistance(10.0)
                .steps(3)
                .cached(false)
                .calculationTimeMs(50)
                .build();
    }

    // 테스트용 cached 결과 생성
    private CachedPathResult createCachedResult() {
        List<Location> path = Arrays.asList(
                new Location(1.0, 1.0),
                new Location(2.0, 2.0),
                new Location(3.0, 3.0)
        );

        return CachedPathResult.builder()
                .path(path)
                .totalDistance(10.0)
                .steps(3)
                .cached(false)
                .calculationTimeMs(50)
                .build();
    }
}
