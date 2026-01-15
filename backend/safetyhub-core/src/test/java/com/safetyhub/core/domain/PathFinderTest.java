package com.safetyhub.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PathFinder (A* 알고리즘) 테스트
 */
class PathFinderTest {

    private GridMap gridMap;
    private PathFinder pathFinder;

    @BeforeEach
    void setUp() {
        gridMap = GridMap.createDefault();
        pathFinder = new PathFinder(gridMap);
    }

    @Test
    @DisplayName("직선 경로 찾기 테스트")
    void testFindStraightPath() {
        Location start = Location.builder().latitude(5.0).longitude(5.0).build();
        Location goal = Location.builder().latitude(10.0).longitude(5.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertTrue(path.size() >= 2); // 최소한 시작점과 끝점

        // 시작점과 끝점 확인
        Location firstLocation = path.get(0);
        Location lastLocation = path.get(path.size() - 1);

        // 그리드 변환 후 비교하므로 정확히 같지 않을 수 있음
        assertTrue(Math.abs(firstLocation.getLatitude() - start.getLatitude()) < 1.0);
        assertTrue(Math.abs(lastLocation.getLatitude() - goal.getLatitude()) < 1.0);
    }

    @Test
    @DisplayName("장애물 회피 경로 찾기 테스트")
    void testFindPathAroundObstacle() {
        // 장애물 설치 (벽 만들기)
        for (int y = 3; y < 8; y++) {
            gridMap.setCellType(5, y, GridMap.CellType.OBSTACLE);
        }

        Location start = Location.builder().latitude(3.0).longitude(5.0).build();
        Location goal = Location.builder().latitude(7.0).longitude(5.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertFalse(path.isEmpty());

        // 경로가 장애물을 통과하지 않는지 확인
        for (Location loc : path) {
            GridMap.GridCoordinate coord = gridMap.toGridCoordinate(loc);
            if (coord != null) {
                assertNotEquals(GridMap.CellType.OBSTACLE, gridMap.getCellType(coord.getX(), coord.getY()));
            }
        }
    }

    @Test
    @DisplayName("경로를 찾을 수 없는 경우 테스트")
    void testNoPathFound() {
        // 시작점 주변을 완전히 막음
        for (int x = 4; x <= 6; x++) {
            for (int y = 4; y <= 6; y++) {
                if (x != 5 || y != 5) { // 시작점은 제외
                    gridMap.setCellType(x, y, GridMap.CellType.OBSTACLE);
                }
            }
        }

        Location start = Location.builder().latitude(5.0).longitude(5.0).build();
        Location goal = Location.builder().latitude(10.0).longitude(10.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertTrue(path.isEmpty()); // 경로를 찾을 수 없음
    }

    @Test
    @DisplayName("null 입력 처리 테스트")
    void testNullInput() {
        Location start = Location.builder().latitude(5.0).longitude(5.0).build();

        List<Location> path1 = pathFinder.findPath(null, start);
        assertTrue(path1.isEmpty());

        List<Location> path2 = pathFinder.findPath(start, null);
        assertTrue(path2.isEmpty());

        List<Location> path3 = pathFinder.findPath(null, null);
        assertTrue(path3.isEmpty());
    }

    @Test
    @DisplayName("시작점과 끝점이 같은 경우 테스트")
    void testSameStartAndGoal() {
        Location location = Location.builder().latitude(5.0).longitude(5.0).build();

        List<Location> path = pathFinder.findPath(location, location);

        assertNotNull(path);
        // 같은 위치면 경로가 1개 (자기 자신) 또는 비어있을 수 있음
        assertTrue(path.size() <= 1);
    }

    @Test
    @DisplayName("대각선 경로 찾기 테스트")
    void testFindDiagonalPath() {
        Location start = Location.builder().latitude(5.0).longitude(5.0).build();
        Location goal = Location.builder().latitude(10.0).longitude(10.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    @DisplayName("복잡한 미로 경로 찾기 테스트")
    void testComplexMazePath() {
        // 복잡한 장애물 패턴 생성
        for (int i = 2; i < 10; i++) {
            if (i % 2 == 0) {
                gridMap.setCellType(i, 5, GridMap.CellType.OBSTACLE);
            } else {
                gridMap.setCellType(i, 7, GridMap.CellType.OBSTACLE);
            }
        }

        Location start = Location.builder().latitude(1.0).longitude(6.0).build();
        Location goal = Location.builder().latitude(11.0).longitude(6.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        // 복잡한 경로지만 찾을 수 있어야 함
        assertFalse(path.isEmpty());
    }

    @Test
    @DisplayName("PathInfo 정보와 함께 경로 찾기 테스트")
    void testFindPathWithInfo() {
        Location start = Location.builder().latitude(5.0).longitude(5.0).build();
        Location goal = Location.builder().latitude(10.0).longitude(10.0).build();

        PathFinder.PathInfo pathInfo = pathFinder.findPathWithInfo(start, goal);

        assertNotNull(pathInfo);
        assertTrue(pathInfo.isValid());
        assertFalse(pathInfo.getPath().isEmpty());
        assertTrue(pathInfo.getTotalDistance() > 0);
        assertTrue(pathInfo.getSteps() > 0);
    }

    @Test
    @DisplayName("PathInfo - 경로를 찾을 수 없는 경우")
    void testPathInfoNoPath() {
        // 시작점 주변을 완전히 막음
        for (int x = 4; x <= 6; x++) {
            for (int y = 4; y <= 6; y++) {
                if (x != 5 || y != 5) {
                    gridMap.setCellType(x, y, GridMap.CellType.OBSTACLE);
                }
            }
        }

        Location start = Location.builder().latitude(5.0).longitude(5.0).build();
        Location goal = Location.builder().latitude(10.0).longitude(10.0).build();

        PathFinder.PathInfo pathInfo = pathFinder.findPathWithInfo(start, goal);

        assertNotNull(pathInfo);
        assertFalse(pathInfo.isValid());
        assertTrue(pathInfo.getPath().isEmpty());
        assertEquals(0.0, pathInfo.getTotalDistance());
        assertEquals(0, pathInfo.getSteps());
    }

    @Test
    @DisplayName("긴 거리 경로 찾기 테스트")
    void testLongDistancePath() {
        Location start = Location.builder().latitude(1.0).longitude(1.0).build();
        Location goal = Location.builder().latitude(45.0).longitude(45.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    @DisplayName("여러 구역을 통과하는 경로 테스트")
    void testPathThroughMultipleZones() {
        // 여러 구역 타입 설정
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                gridMap.setCellType(x, y, GridMap.CellType.WORK_AREA);
            }
        }

        for (int x = 10; x < 20; x++) {
            for (int y = 0; y < 10; y++) {
                gridMap.setCellType(x, y, GridMap.CellType.CORRIDOR);
            }
        }

        for (int x = 20; x < 30; x++) {
            for (int y = 0; y < 10; y++) {
                gridMap.setCellType(x, y, GridMap.CellType.REST_AREA);
            }
        }

        Location start = Location.builder().latitude(5.0).longitude(5.0).build();
        Location goal = Location.builder().latitude(25.0).longitude(5.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    @DisplayName("좁은 통로를 통과하는 경로 테스트")
    void testPathThroughNarrowPassage() {
        // 좁은 통로 만들기
        for (int y = 0; y < 20; y++) {
            if (y != 10) { // y=10만 통과 가능
                gridMap.setCellType(10, y, GridMap.CellType.OBSTACLE);
            }
        }

        Location start = Location.builder().latitude(5.0).longitude(10.0).build();
        Location goal = Location.builder().latitude(15.0).longitude(10.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertFalse(path.isEmpty());

        // 경로가 통로(y=10)를 통과하는지 확인
        boolean passedThroughPassage = false;
        for (Location loc : path) {
            GridMap.GridCoordinate coord = gridMap.toGridCoordinate(loc);
            if (coord != null && coord.getX() == 10) {
                passedThroughPassage = true;
                assertEquals(10, coord.getY());
            }
        }
        assertTrue(passedThroughPassage);
    }

    @Test
    @DisplayName("맵 경계 근처 경로 테스트")
    void testPathNearMapBoundary() {
        Location start = Location.builder().latitude(1.0).longitude(1.0).build();
        Location goal = Location.builder().latitude(1.0).longitude(48.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    @DisplayName("위험 구역을 통과하는 경로 테스트")
    void testPathThroughDangerZone() {
        // 위험 구역 설정 (이동은 가능하지만 경고)
        for (int x = 8; x < 12; x++) {
            for (int y = 8; y < 12; y++) {
                gridMap.setCellType(x, y, GridMap.CellType.DANGER_ZONE);
            }
        }

        Location start = Location.builder().latitude(5.0).longitude(10.0).build();
        Location goal = Location.builder().latitude(15.0).longitude(10.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        assertNotNull(path);
        assertFalse(path.isEmpty());
        // 위험 구역도 이동 가능하므로 경로를 찾아야 함
    }

    @Test
    @DisplayName("범위 밖 좌표로 경로 찾기 테스트")
    void testPathWithOutOfBoundsCoordinates() {
        Location start = Location.builder().latitude(5.0).longitude(5.0).build();
        Location goal = Location.builder().latitude(100.0).longitude(100.0).build();

        List<Location> path = pathFinder.findPath(start, goal);

        // 범위 밖 좌표는 경로를 찾을 수 없음
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }
}
