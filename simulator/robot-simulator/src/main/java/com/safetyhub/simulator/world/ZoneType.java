package com.safetyhub.simulator.world;

/**
 * 가상 공장 구역 유형
 */
public enum ZoneType {
    WORK_AREA("작업 구역"),
    REST_AREA("휴게 구역"),
    DINING_AREA("식당"),
    MEDICAL("의무실"),
    SHELTER("대피소"),
    WAREHOUSE("창고"),
    ENTRANCE("출입구"),
    HALLWAY("복도"),
    CHARGING_STATION("충전소"),
    HAZARD("위험 구역");

    private final String description;

    ZoneType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
