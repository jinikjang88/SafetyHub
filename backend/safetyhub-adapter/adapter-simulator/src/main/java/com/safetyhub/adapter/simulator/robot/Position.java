package com.safetyhub.adapter.simulator.robot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 2D 그리드 좌표
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Position {
    private int x;
    private int y;

    /**
     * 두 위치 간 맨해튼 거리 계산
     */
    public int manhattanDistance(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /**
     * 두 위치 간 유클리드 거리 계산
     */
    public double euclideanDistance(Position other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 인접 위치인지 확인 (상하좌우)
     */
    public boolean isAdjacentTo(Position other) {
        return manhattanDistance(other) == 1;
    }

    /**
     * 위도/경도로 변환 (시뮬레이션용 가상 좌표)
     * 기준점: 서울 (37.5665, 126.9780)
     * 1 그리드 = 약 10m
     */
    public double toLatitude() {
        return 37.5665 + (y * 0.00009);  // 약 10m per grid
    }

    public double toLongitude() {
        return 126.9780 + (x * 0.00011);  // 약 10m per grid
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    public static Position of(int x, int y) {
        return new Position(x, y);
    }
}
