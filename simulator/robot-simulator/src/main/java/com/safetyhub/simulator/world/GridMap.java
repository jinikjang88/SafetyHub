package com.safetyhub.simulator.world;

import com.safetyhub.simulator.core.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * 2D 그리드 맵
 * 각 셀은 이동 가능 여부와 구역 정보를 포함
 */
public class GridMap {
    private final int width;
    private final int height;
    private final CellType[][] cells;
    private final String[][] zoneIds;
    private final Map<String, Zone> zones;

    public enum CellType {
        WALKABLE,   // 이동 가능
        OBSTACLE,   // 장애물
        WALL,       // 벽
        HAZARD      // 위험 구역
    }

    public GridMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new CellType[width][height];
        this.zoneIds = new String[width][height];
        this.zones = new HashMap<>();

        // 기본적으로 모든 셀을 이동 가능으로 초기화
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = CellType.WALKABLE;
            }
        }
    }

    public void addZone(Zone zone) {
        zones.put(zone.getId(), zone);
        // 구역에 해당하는 셀들에 구역 ID 설정
        for (int x = zone.getStartX(); x < zone.getStartX() + zone.getWidth() && x < width; x++) {
            for (int y = zone.getStartY(); y < zone.getStartY() + zone.getHeight() && y < height; y++) {
                if (x >= 0 && y >= 0) {
                    zoneIds[x][y] = zone.getId();
                    if (zone.isHazardous()) {
                        cells[x][y] = CellType.HAZARD;
                    }
                }
            }
        }
    }

    public void setCell(int x, int y, CellType type) {
        if (isValid(x, y)) {
            cells[x][y] = type;
        }
    }

    public void setWall(int x1, int y1, int x2, int y2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                setCell(x, y, CellType.WALL);
            }
        }
    }

    public boolean isWalkable(int x, int y) {
        if (!isValid(x, y)) return false;
        return cells[x][y] == CellType.WALKABLE;
    }

    public boolean isWalkable(Location location) {
        return isWalkable(location.getX(), location.getY());
    }

    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public CellType getCell(int x, int y) {
        if (!isValid(x, y)) return CellType.WALL;
        return cells[x][y];
    }

    public String getZoneId(int x, int y) {
        if (!isValid(x, y)) return null;
        return zoneIds[x][y];
    }

    public String getZoneId(Location location) {
        return getZoneId(location.getX(), location.getY());
    }

    public Zone getZone(String zoneId) {
        return zones.get(zoneId);
    }

    public Zone getZoneAt(Location location) {
        String zoneId = getZoneId(location);
        return zoneId != null ? zones.get(zoneId) : null;
    }

    public Map<String, Zone> getAllZones() {
        return new HashMap<>(zones);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void printMap() {
        System.out.println("Grid Map (" + width + "x" + height + "):");
        for (int y = 0; y < height; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < width; x++) {
                char c = switch (cells[x][y]) {
                    case WALKABLE -> '.';
                    case OBSTACLE -> 'O';
                    case WALL -> '#';
                    case HAZARD -> '!';
                };
                row.append(c);
            }
            System.out.println(row);
        }
    }
}
