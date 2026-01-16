package com.safetyhub.application.dispatch;

import com.safetyhub.core.domain.Location;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * RobotInfo의 간단한 구현체
 * 작업 분배에 필요한 로봇 정보를 담는 DTO
 */
@Getter
@Builder
@ToString
public class SimpleRobotInfo implements DispatchStrategy.RobotInfo {

    private final String robotId;
    private final String name;
    private final String state;
    private final Location currentLocation;
    private final String currentZoneId;
    private final int batteryLevel;
    private final int assignedTaskCount;

    /**
     * 로봇이 작업 가능한 상태인지 확인
     * 기준:
     * - 상태가 WORKING 또는 RESTING
     * - 배터리가 20% 이상
     * - 할당된 작업이 5개 미만
     */
    @Override
    public boolean isAvailable() {
        boolean isActiveState = "WORKING".equals(state) || "RESTING".equals(state);
        boolean hasSufficientBattery = batteryLevel >= 20;
        boolean hasCapacity = assignedTaskCount < 5;

        return isActiveState && hasSufficientBattery && hasCapacity;
    }

    /**
     * 특정 위치까지의 유클리드 거리 계산
     * @param targetLocation 목표 위치
     * @return 거리 (미터)
     */
    @Override
    public double getDistanceTo(Location targetLocation) {
        if (currentLocation == null || targetLocation == null) {
            return Double.MAX_VALUE;
        }

        double dx = currentLocation.getX() - targetLocation.getX();
        double dy = currentLocation.getY() - targetLocation.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
