package com.safetyhub.adapter.simulator.robot;

/**
 * 로봇 작업자 상태 정의
 */
public enum RobotState {
    WORKING("작업 중"),
    RESTING("휴식 중"),
    EATING("식사 중"),
    MOVING("이동 중"),
    EMERGENCY("긴급 상황"),
    EVACUATING("대피 중"),
    OFFLINE("오프라인");

    private final String description;

    RobotState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
