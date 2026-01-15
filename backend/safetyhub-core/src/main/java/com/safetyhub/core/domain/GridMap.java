package com.safetyhub.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 2D 그리드 맵
 * 가상 공장의 물리적 공간을 그리드로 표현
 */
@Getter
@Builder
@AllArgsConstructor
public class GridMap {

    private Integer width;           // 맵 가로 크기 (그리드 단위)
    private Integer height;          // 맵 세로 크기 (그리드 단위)
    private Double cellSize;         // 각 셀의 크기 (미터)
    private CellType[][] cells;      // 셀 타입 배열
    private Map<String, Zone> zones; // 구역 맵 (zoneId -> Zone)

    /**
     * 셀 타입 정의
     */
    public enum CellType {
        EMPTY,          // 빈 공간 (이동 가능)
        OBSTACLE,       // 장애물 (이동 불가)
        WORK_AREA,      // 작업 구역
        REST_AREA,      // 휴게 구역
        DANGER_ZONE,    // 위험 구역
        CORRIDOR,       // 복도
        ASSEMBLY_POINT  // 집결지
    }

    /**
     * 그리드 좌표
     */
    @Getter
    @AllArgsConstructor
    public static class GridCoordinate {
        private int x;
        private int y;

        public boolean isValid(int width, int height) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GridCoordinate that = (GridCoordinate) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    /**
     * 기본 맵 생성 (50x50 그리드, 1미터 셀)
     */
    public static GridMap createDefault() {
        int width = 50;
        int height = 50;
        CellType[][] cells = new CellType[height][width];

        // 모든 셀을 빈 공간으로 초기화
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = CellType.EMPTY;
            }
        }

        return GridMap.builder()
                .width(width)
                .height(height)
                .cellSize(1.0)
                .cells(cells)
                .zones(new HashMap<>())
                .build();
    }

    /**
     * 위치를 그리드 좌표로 변환
     */
    public GridCoordinate toGridCoordinate(Location location) {
        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            return null;
        }

        // 간단한 변환 (실제로는 좌표계 변환 필요)
        int x = (int) (location.getLongitude() / cellSize);
        int y = (int) (location.getLatitude() / cellSize);

        // 범위 체크
        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));

        return new GridCoordinate(x, y);
    }

    /**
     * 그리드 좌표를 위치로 변환
     */
    public Location toLocation(GridCoordinate coord) {
        if (coord == null) {
            return null;
        }

        return Location.builder()
                .latitude(coord.getY() * cellSize)
                .longitude(coord.getX() * cellSize)
                .altitude(0.0)
                .build();
    }

    /**
     * 특정 좌표의 셀 타입 가져오기
     */
    public CellType getCellType(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return CellType.OBSTACLE;
        }
        return cells[y][x];
    }

    /**
     * 특정 좌표의 셀 타입 설정
     */
    public void setCellType(int x, int y, CellType type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[y][x] = type;
        }
    }

    /**
     * 특정 좌표가 이동 가능한지 확인
     */
    public boolean isWalkable(int x, int y) {
        CellType type = getCellType(x, y);
        return type != CellType.OBSTACLE;
    }

    /**
     * 구역 추가
     */
    public void addZone(Zone zone) {
        if (zone != null && zone.getZoneId() != null) {
            zones.put(zone.getZoneId(), zone);

            // 구역 영역을 맵에 표시
            if (zone.getCenterLocation() != null && zone.getRadius() != null) {
                markZoneArea(zone);
            }
        }
    }

    /**
     * 구역 영역을 맵에 표시
     */
    private void markZoneArea(Zone zone) {
        GridCoordinate center = toGridCoordinate(zone.getCenterLocation());
        if (center == null) return;

        int radiusCells = (int) (zone.getRadius() / cellSize);
        CellType cellType = getCellTypeForZone(zone.getType());

        for (int y = center.getY() - radiusCells; y <= center.getY() + radiusCells; y++) {
            for (int x = center.getX() - radiusCells; x <= center.getX() + radiusCells; x++) {
                // 원형 범위 체크
                double distance = Math.sqrt(
                    Math.pow(x - center.getX(), 2) + Math.pow(y - center.getY(), 2)
                );
                if (distance <= radiusCells) {
                    setCellType(x, y, cellType);
                }
            }
        }
    }

    /**
     * Zone 타입에 따른 Cell 타입 매핑
     */
    private CellType getCellTypeForZone(Zone.ZoneType zoneType) {
        switch (zoneType) {
            case WORK_AREA:
                return CellType.WORK_AREA;
            case REST_AREA:
                return CellType.REST_AREA;
            case DANGER_ZONE:
                return CellType.DANGER_ZONE;
            case EVACUATION_ROUTE:
                return CellType.CORRIDOR;
            case ASSEMBLY_POINT:
                return CellType.ASSEMBLY_POINT;
            default:
                return CellType.EMPTY;
        }
    }

    /**
     * 특정 위치가 속한 구역 찾기
     */
    public Zone findZoneAt(Location location) {
        for (Zone zone : zones.values()) {
            if (isLocationInZone(location, zone)) {
                return zone;
            }
        }
        return null;
    }

    /**
     * 위치가 구역 내에 있는지 확인
     */
    private boolean isLocationInZone(Location location, Zone zone) {
        if (location == null || zone.getCenterLocation() == null || zone.getRadius() == null) {
            return false;
        }

        double distance = location.distanceTo(zone.getCenterLocation());
        return distance <= zone.getRadius();
    }

    /**
     * 구역 가져오기
     */
    public Zone getZone(String zoneId) {
        return zones.get(zoneId);
    }

    /**
     * 맵 정보 출력
     */
    @Override
    public String toString() {
        return String.format("GridMap[%dx%d, cellSize=%.1fm, zones=%d]",
                width, height, cellSize, zones.size());
    }
}
