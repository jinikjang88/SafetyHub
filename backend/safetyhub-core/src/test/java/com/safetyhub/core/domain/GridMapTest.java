package com.safetyhub.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GridMap 도메인 모델 테스트
 */
class GridMapTest {

    private GridMap gridMap;

    @BeforeEach
    void setUp() {
        gridMap = GridMap.createDefault();
    }

    @Test
    @DisplayName("기본 그리드 맵 생성 테스트")
    void testCreateDefaultGridMap() {
        assertNotNull(gridMap);
        assertEquals(50, gridMap.getWidth());
        assertEquals(50, gridMap.getHeight());
        assertEquals(1.0, gridMap.getCellSize());
        assertEquals(0, gridMap.getZones().size());
    }

    @Test
    @DisplayName("커스텀 그리드 맵 생성 테스트")
    void testCreateCustomGridMap() {
        GridMap customMap = new GridMap(100, 100, 2.0);

        assertEquals(100, customMap.getWidth());
        assertEquals(100, customMap.getHeight());
        assertEquals(2.0, customMap.getCellSize());
    }

    @Test
    @DisplayName("셀 타입 설정 및 가져오기 테스트")
    void testSetAndGetCellType() {
        gridMap.setCellType(10, 10, GridMap.CellType.OBSTACLE);
        assertEquals(GridMap.CellType.OBSTACLE, gridMap.getCellType(10, 10));

        gridMap.setCellType(20, 20, GridMap.CellType.DANGER_ZONE);
        assertEquals(GridMap.CellType.DANGER_ZONE, gridMap.getCellType(20, 20));
    }

    @Test
    @DisplayName("범위 밖 좌표 처리 테스트")
    void testOutOfBoundsCoordinates() {
        // 범위 밖 좌표는 EMPTY를 반환
        assertEquals(GridMap.CellType.EMPTY, gridMap.getCellType(-1, 0));
        assertEquals(GridMap.CellType.EMPTY, gridMap.getCellType(0, -1));
        assertEquals(GridMap.CellType.EMPTY, gridMap.getCellType(100, 0));
        assertEquals(GridMap.CellType.EMPTY, gridMap.getCellType(0, 100));
    }

    @Test
    @DisplayName("이동 가능 여부 테스트")
    void testIsWalkable() {
        // 기본적으로 EMPTY는 이동 가능
        assertTrue(gridMap.isWalkable(10, 10));

        // OBSTACLE은 이동 불가
        gridMap.setCellType(10, 10, GridMap.CellType.OBSTACLE);
        assertFalse(gridMap.isWalkable(10, 10));

        // CORRIDOR는 이동 가능
        gridMap.setCellType(15, 15, GridMap.CellType.CORRIDOR);
        assertTrue(gridMap.isWalkable(15, 15));

        // WORK_AREA는 이동 가능
        gridMap.setCellType(20, 20, GridMap.CellType.WORK_AREA);
        assertTrue(gridMap.isWalkable(20, 20));
    }

    @Test
    @DisplayName("위치를 그리드 좌표로 변환 테스트")
    void testToGridCoordinate() {
        Location location = Location.builder()
                .latitude(5.0)
                .longitude(10.0)
                .build();

        GridMap.GridCoordinate coord = gridMap.toGridCoordinate(location);

        assertNotNull(coord);
        assertEquals(5, coord.getX());
        assertEquals(10, coord.getY());
    }

    @Test
    @DisplayName("그리드 좌표를 위치로 변환 테스트")
    void testToLocation() {
        GridMap.GridCoordinate coord = new GridMap.GridCoordinate(10, 20);

        Location location = gridMap.toLocation(coord);

        assertNotNull(location);
        assertEquals(10.5, location.getLatitude(), 0.01);  // 셀 중심
        assertEquals(20.5, location.getLongitude(), 0.01);
    }

    @Test
    @DisplayName("구역 추가 테스트")
    void testAddZone() {
        Zone zone = Zone.builder()
                .zoneId("TEST_ZONE")
                .name("테스트 구역")
                .type(Zone.ZoneType.WORK_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .centerLocation(Location.builder().latitude(10.0).longitude(10.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build();

        gridMap.addZone(zone);

        assertEquals(1, gridMap.getZones().size());
        assertNotNull(gridMap.getZone("TEST_ZONE"));
        assertEquals("테스트 구역", gridMap.getZone("TEST_ZONE").getName());
    }

    @Test
    @DisplayName("위치에서 구역 찾기 테스트")
    void testFindZoneAt() {
        Zone zone = Zone.builder()
                .zoneId("TEST_ZONE")
                .name("테스트 구역")
                .type(Zone.ZoneType.WORK_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .centerLocation(Location.builder().latitude(10.0).longitude(10.0).build())
                .radius(3.0)
                .createdAt(LocalDateTime.now())
                .build();

        gridMap.addZone(zone);

        // 구역 중심에서 찾기
        Location centerLocation = Location.builder().latitude(10.0).longitude(10.0).build();
        Zone foundZone = gridMap.findZoneAt(centerLocation);
        assertNotNull(foundZone);
        assertEquals("TEST_ZONE", foundZone.getZoneId());

        // 구역 반경 내에서 찾기
        Location insideLocation = Location.builder().latitude(11.0).longitude(11.0).build();
        foundZone = gridMap.findZoneAt(insideLocation);
        assertNotNull(foundZone);
        assertEquals("TEST_ZONE", foundZone.getZoneId());

        // 구역 밖에서 찾기
        Location outsideLocation = Location.builder().latitude(20.0).longitude(20.0).build();
        foundZone = gridMap.findZoneAt(outsideLocation);
        assertNull(foundZone);
    }

    @Test
    @DisplayName("장애물 추가 테스트")
    void testAddObstacle() {
        gridMap.setCellType(10, 10, GridMap.CellType.OBSTACLE);
        gridMap.setCellType(10, 11, GridMap.CellType.OBSTACLE);
        gridMap.setCellType(11, 10, GridMap.CellType.OBSTACLE);

        assertFalse(gridMap.isWalkable(10, 10));
        assertFalse(gridMap.isWalkable(10, 11));
        assertFalse(gridMap.isWalkable(11, 10));
        assertTrue(gridMap.isWalkable(12, 12));
    }

    @Test
    @DisplayName("GridCoordinate equals 및 hashCode 테스트")
    void testGridCoordinateEqualsAndHashCode() {
        GridMap.GridCoordinate coord1 = new GridMap.GridCoordinate(10, 20);
        GridMap.GridCoordinate coord2 = new GridMap.GridCoordinate(10, 20);
        GridMap.GridCoordinate coord3 = new GridMap.GridCoordinate(15, 25);

        assertEquals(coord1, coord2);
        assertNotEquals(coord1, coord3);
        assertEquals(coord1.hashCode(), coord2.hashCode());
    }

    @Test
    @DisplayName("여러 구역 추가 및 관리 테스트")
    void testMultipleZones() {
        Zone zone1 = Zone.builder()
                .zoneId("ZONE_1")
                .name("구역 1")
                .type(Zone.ZoneType.WORK_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .centerLocation(Location.builder().latitude(10.0).longitude(10.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build();

        Zone zone2 = Zone.builder()
                .zoneId("ZONE_2")
                .name("구역 2")
                .type(Zone.ZoneType.REST_AREA)
                .status(Zone.ZoneStatus.NORMAL)
                .riskLevel(Zone.RiskLevel.LOW)
                .centerLocation(Location.builder().latitude(30.0).longitude(30.0).build())
                .radius(5.0)
                .createdAt(LocalDateTime.now())
                .build();

        gridMap.addZone(zone1);
        gridMap.addZone(zone2);

        assertEquals(2, gridMap.getZones().size());
        assertNotNull(gridMap.getZone("ZONE_1"));
        assertNotNull(gridMap.getZone("ZONE_2"));

        // 각 구역이 올바른 위치에 있는지 확인
        Location loc1 = Location.builder().latitude(10.0).longitude(10.0).build();
        Zone found1 = gridMap.findZoneAt(loc1);
        assertEquals("ZONE_1", found1.getZoneId());

        Location loc2 = Location.builder().latitude(30.0).longitude(30.0).build();
        Zone found2 = gridMap.findZoneAt(loc2);
        assertEquals("ZONE_2", found2.getZoneId());
    }

    @Test
    @DisplayName("셀 타입별 이동 가능 여부 테스트")
    void testWalkableForAllCellTypes() {
        // EMPTY - 이동 가능
        gridMap.setCellType(0, 0, GridMap.CellType.EMPTY);
        assertTrue(gridMap.isWalkable(0, 0));

        // OBSTACLE - 이동 불가
        gridMap.setCellType(1, 1, GridMap.CellType.OBSTACLE);
        assertFalse(gridMap.isWalkable(1, 1));

        // WORK_AREA - 이동 가능
        gridMap.setCellType(2, 2, GridMap.CellType.WORK_AREA);
        assertTrue(gridMap.isWalkable(2, 2));

        // REST_AREA - 이동 가능
        gridMap.setCellType(3, 3, GridMap.CellType.REST_AREA);
        assertTrue(gridMap.isWalkable(3, 3));

        // DANGER_ZONE - 이동 가능 (경고는 있지만 물리적으로는 이동 가능)
        gridMap.setCellType(4, 4, GridMap.CellType.DANGER_ZONE);
        assertTrue(gridMap.isWalkable(4, 4));

        // CORRIDOR - 이동 가능
        gridMap.setCellType(5, 5, GridMap.CellType.CORRIDOR);
        assertTrue(gridMap.isWalkable(5, 5));

        // ASSEMBLY_POINT - 이동 가능
        gridMap.setCellType(6, 6, GridMap.CellType.ASSEMBLY_POINT);
        assertTrue(gridMap.isWalkable(6, 6));
    }
}
