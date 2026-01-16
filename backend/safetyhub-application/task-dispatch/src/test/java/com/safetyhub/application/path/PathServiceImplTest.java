package com.safetyhub.application.path;

import com.safetyhub.core.domain.GridMap;
import com.safetyhub.core.domain.Location;
import com.safetyhub.core.domain.PathFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PathServiceImpl 테스트")
class PathServiceImplTest {

    private PathService pathService;
    private GridMap gridMap;
    private TestZoneLocationProvider zoneLocationProvider;

    @BeforeEach
    void setUp() {
        // 10x10 그리드 맵 생성
        gridMap = createTestGridMap();
        zoneLocationProvider = new TestZoneLocationProvider();

        // Zone 위치 설정
        zoneLocationProvider.addZone("zone-001", new Location(1.0, 1.0));
        zoneLocationProvider.addZone("zone-002", new Location(8.0, 8.0));
        zoneLocationProvider.addZone("zone-003", new Location(5.0, 5.0));

        pathService = new PathServiceImpl(gridMap, zoneLocationProvider);
    }

    @Test
    @DisplayName("경로 찾기 - Location 기반")
    void findPathByLocation() {
        // given
        Location start = new Location(1.0, 1.0);
        Location goal = new Location(8.0, 8.0);

        // when
        Optional<PathService.PathResult> result = pathService.findPath(start, goal);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertTrue(result.get().getTotalDistance() > 0);
        assertTrue(result.get().getSteps() > 0);
        assertFalse(result.get().isCached());
    }

    @Test
    @DisplayName("경로 찾기 - Zone ID 기반")
    void findPathByZoneId() {
        // given
        String startZoneId = "zone-001";
        String goalZoneId = "zone-002";

        // when
        Optional<PathService.PathResult> result = pathService.findPath(startZoneId, goalZoneId);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertTrue(result.get().getTotalDistance() > 0);
    }

    @Test
    @DisplayName("경로 찾기 실패 - null 위치")
    void findPathWithNullLocation() {
        // when
        Optional<PathService.PathResult> result = pathService.findPath(null, new Location(5.0, 5.0));

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("경로 찾기 실패 - 존재하지 않는 Zone")
    void findPathWithNonExistentZone() {
        // when
        Optional<PathService.PathResult> result = pathService.findPath("zone-001", "zone-999");

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("경로 찾기 실패 - 같은 Zone")
    void findPathWithSameZone() {
        // when
        Optional<PathService.PathResult> result = pathService.findPath("zone-001", "zone-001");

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("이동 시간 예측")
    void estimateTravelTime() {
        // given
        Location start = new Location(1.0, 1.0);
        Location goal = new Location(8.0, 8.0);
        double speed = 2.0; // 2 m/s

        // when
        Optional<Double> travelTime = pathService.estimateTravelTime(start, goal, speed);

        // then
        assertTrue(travelTime.isPresent());
        assertTrue(travelTime.get() > 0);
    }

    @Test
    @DisplayName("이동 시간 예측 실패 - 잘못된 속도")
    void estimateTravelTimeWithInvalidSpeed() {
        // given
        Location start = new Location(1.0, 1.0);
        Location goal = new Location(8.0, 8.0);
        double invalidSpeed = 0.0;

        // when
        Optional<Double> travelTime = pathService.estimateTravelTime(start, goal, invalidSpeed);

        // then
        assertFalse(travelTime.isPresent());
    }

    @Test
    @DisplayName("캐시 무효화 - 기본 구현은 아무것도 하지 않음")
    void invalidateCache() {
        // when & then
        assertDoesNotThrow(() -> pathService.invalidateCache("zone-001", "zone-002"));
        assertDoesNotThrow(() -> pathService.invalidateAllCache());
    }

    @Test
    @DisplayName("경로 계산 시간 측정")
    void measureCalculationTime() {
        // given
        Location start = new Location(1.0, 1.0);
        Location goal = new Location(8.0, 8.0);

        // when
        Optional<PathService.PathResult> result = pathService.findPath(start, goal);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().getCalculationTimeMs() >= 0);
    }

    @Test
    @DisplayName("PathResult 유효성 검증")
    void validatePathResult() {
        // given
        Location start = new Location(1.0, 1.0);
        Location goal = new Location(8.0, 8.0);

        // when
        Optional<PathService.PathResult> result = pathService.findPath(start, goal);

        // then
        assertTrue(result.isPresent());
        PathService.PathResult pathResult = result.get();

        assertNotNull(pathResult.getPath());
        assertFalse(pathResult.getPath().isEmpty());
        assertTrue(pathResult.getTotalDistance() > 0);
        assertEquals(pathResult.getPath().size(), pathResult.getSteps());
        assertTrue(pathResult.isValid());
    }

    // 테스트용 GridMap 생성
    private GridMap createTestGridMap() {
        // 10x10 그리드, 모두 이동 가능
        boolean[][] walkable = new boolean[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                walkable[i][j] = true;
            }
        }

        return new GridMap(10, 10, walkable, 0.0, 0.0, 10.0, 10.0);
    }

    // 테스트용 ZoneLocationProvider 구현
    private static class TestZoneLocationProvider implements ZoneLocationProvider {
        private final Map<String, Location> zoneLocations = new HashMap<>();

        public void addZone(String zoneId, Location location) {
            zoneLocations.put(zoneId, location);
        }

        @Override
        public Optional<Location> getZoneCenterLocation(String zoneId) {
            return Optional.ofNullable(zoneLocations.get(zoneId));
        }
    }
}
