package com.safetyhub.adapter.simulator.world;

import com.safetyhub.adapter.simulator.robot.Position;
import com.safetyhub.adapter.simulator.robot.ZoneType;
import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 시뮬레이션 구역 정의
 */
@Getter
@Builder
public class SimulationZone {

    private final String zoneId;
    private final String name;
    private final ZoneType type;
    private final DangerLevel dangerLevel;
    private final int maxCapacity;

    // 구역 경계 (사각형)
    private final Position topLeft;
    private final Position bottomRight;

    // 현재 구역 내 로봇 ID 목록
    @Builder.Default
    private final Set<String> robotIds = new HashSet<>();

    private static final Random random = new Random();

    public enum DangerLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * 구역 내 랜덤 위치 반환
     */
    public Position getRandomPosition() {
        int x = topLeft.getX() + random.nextInt(getWidth());
        int y = topLeft.getY() + random.nextInt(getHeight());
        return Position.of(x, y);
    }

    /**
     * 구역 중심 위치 반환
     */
    public Position getCenterPosition() {
        int x = topLeft.getX() + getWidth() / 2;
        int y = topLeft.getY() + getHeight() / 2;
        return Position.of(x, y);
    }

    /**
     * 특정 위치가 구역 내에 있는지 확인
     */
    public boolean contains(Position position) {
        return position.getX() >= topLeft.getX()
                && position.getX() <= bottomRight.getX()
                && position.getY() >= topLeft.getY()
                && position.getY() <= bottomRight.getY();
    }

    /**
     * 구역 너비
     */
    public int getWidth() {
        return bottomRight.getX() - topLeft.getX() + 1;
    }

    /**
     * 구역 높이
     */
    public int getHeight() {
        return bottomRight.getY() - topLeft.getY() + 1;
    }

    /**
     * 현재 인원 수
     */
    public int getCurrentOccupancy() {
        return robotIds.size();
    }

    /**
     * 여유 공간 확인
     */
    public boolean hasCapacity() {
        return getCurrentOccupancy() < maxCapacity;
    }

    /**
     * 로봇 입장
     */
    public void addRobot(String robotId) {
        robotIds.add(robotId);
    }

    /**
     * 로봇 퇴장
     */
    public void removeRobot(String robotId) {
        robotIds.remove(robotId);
    }

    @Override
    public String toString() {
        return String.format("Zone[%s, type=%s, occupancy=%d/%d]",
                zoneId, type, getCurrentOccupancy(), maxCapacity);
    }
}
