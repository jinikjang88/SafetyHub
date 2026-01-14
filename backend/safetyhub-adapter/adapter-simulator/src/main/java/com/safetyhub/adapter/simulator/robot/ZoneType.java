package com.safetyhub.adapter.simulator.robot;

/**
 * 구역 타입 정의
 */
public enum ZoneType {
    WORK_AREA("작업장"),
    DANGER_ZONE("위험 구역"),
    REST_AREA("휴게실"),
    CAFETERIA("식당"),
    MEDICAL("의무실"),
    ASSEMBLY_POINT("대피소"),
    CORRIDOR("복도"),
    ENTRANCE("출입구");

    private final String description;

    ZoneType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
