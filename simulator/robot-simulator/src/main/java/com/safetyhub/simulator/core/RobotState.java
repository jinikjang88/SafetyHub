package com.safetyhub.simulator.core;

/**
 * 로봇 작업자의 상태를 나타내는 열거형
 */
public enum RobotState {
    WORKING("작업 중"),
    RESTING("휴식 중"),
    EATING("식사 중"),
    MOVING("이동 중"),
    EMERGENCY("긴급 상황"),
    EVACUATING("대피 중"),
    IDLE("대기 중"),
    CHARGING("충전 중");

    private final String description;

    RobotState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
