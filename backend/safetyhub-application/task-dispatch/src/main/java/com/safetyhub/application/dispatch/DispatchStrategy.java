package com.safetyhub.application.dispatch;

import com.safetyhub.core.domain.Task;

import java.util.List;
import java.util.Optional;

/**
 * 작업 분배 전략 인터페이스
 * 전략 패턴(Strategy Pattern)을 사용하여 다양한 분배 알고리즘 구현 가능
 */
public interface DispatchStrategy {

    /**
     * 전략 이름
     * @return 분배 전략 이름
     */
    String getName();

    /**
     * 전략 설명
     * @return 분배 전략 설명
     */
    String getDescription();

    /**
     * 작업을 로봇에 할당
     * @param task 할당할 작업
     * @param availableRobots 사용 가능한 로봇 목록
     * @return 할당된 로봇 ID, 할당할 로봇이 없으면 Optional.empty()
     */
    Optional<String> assignTask(Task task, List<RobotInfo> availableRobots);

    /**
     * 로봇 정보 DTO
     * 분배 전략에서 로봇 선택 시 필요한 정보
     */
    interface RobotInfo {
        /**
         * 로봇 ID
         */
        String getRobotId();

        /**
         * 로봇 이름
         */
        String getName();

        /**
         * 로봇 상태 (WORKING, RESTING, etc.)
         */
        String getState();

        /**
         * 현재 위치
         */
        com.safetyhub.core.domain.Location getCurrentLocation();

        /**
         * 현재 구역 ID
         */
        String getCurrentZoneId();

        /**
         * 배터리 레벨 (0-100)
         */
        int getBatteryLevel();

        /**
         * 현재 할당된 작업 수
         */
        int getAssignedTaskCount();

        /**
         * 로봇이 작업 가능한 상태인지 확인
         * @return 작업 가능하면 true
         */
        boolean isAvailable();

        /**
         * 특정 위치까지의 거리 계산 (유클리드 거리)
         * @param targetLocation 목표 위치
         * @return 거리 (미터)
         */
        double getDistanceTo(com.safetyhub.core.domain.Location targetLocation);
    }
}
