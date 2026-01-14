package com.safetyhub.adapter.simulator.world;

import com.safetyhub.adapter.simulator.robot.Position;
import lombok.Getter;

/**
 * 2D 그리드 맵
 * 시뮬레이션 공간을 그리드로 표현
 */
@Getter
public class GridMap {

    private final int width;
    private final int height;
    private final CellType[][] cells;

    public enum CellType {
        FLOOR,      // 이동 가능
        WALL,       // 이동 불가 (벽)
        OBSTACLE,   // 이동 불가 (장애물)
        DOOR,       // 이동 가능 (출입구)
        DANGER      // 이동 가능 (위험 구역)
    }

    public GridMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new CellType[width][height];

        // 기본적으로 모든 셀을 FLOOR로 초기화
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = CellType.FLOOR;
            }
        }
    }

    /**
     * 특정 위치의 셀 타입 설정
     */
    public void setCell(int x, int y, CellType type) {
        if (isValidPosition(x, y)) {
            cells[x][y] = type;
        }
    }

    /**
     * 특정 위치의 셀 타입 반환
     */
    public CellType getCell(int x, int y) {
        if (isValidPosition(x, y)) {
            return cells[x][y];
        }
        return CellType.WALL;
    }

    public CellType getCell(Position pos) {
        return getCell(pos.getX(), pos.getY());
    }

    /**
     * 특정 위치가 유효한지 확인
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isValidPosition(Position pos) {
        return isValidPosition(pos.getX(), pos.getY());
    }

    /**
     * 특정 위치로 이동 가능한지 확인
     */
    public boolean isWalkable(int x, int y) {
        if (!isValidPosition(x, y)) {
            return false;
        }
        CellType cell = cells[x][y];
        return cell == CellType.FLOOR || cell == CellType.DOOR || cell == CellType.DANGER;
    }

    public boolean isWalkable(Position pos) {
        return isWalkable(pos.getX(), pos.getY());
    }

    /**
     * 사각형 영역을 특정 타입으로 채우기
     */
    public void fillRect(int x1, int y1, int x2, int y2, CellType type) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                setCell(x, y, type);
            }
        }
    }

    /**
     * 수평 벽 생성
     */
    public void createHorizontalWall(int x1, int x2, int y) {
        for (int x = x1; x <= x2; x++) {
            setCell(x, y, CellType.WALL);
        }
    }

    /**
     * 수직 벽 생성
     */
    public void createVerticalWall(int x, int y1, int y2) {
        for (int y = y1; y <= y2; y++) {
            setCell(x, y, CellType.WALL);
        }
    }

    /**
     * 맵을 ASCII 문자열로 출력 (디버깅용)
     */
    public String toAscii() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = switch (cells[x][y]) {
                    case WALL -> '#';
                    case OBSTACLE -> 'X';
                    case DOOR -> 'D';
                    case DANGER -> '!';
                    default -> '.';
                };
                sb.append(c);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
