package com.safetyhub.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 위치 정보 Value Object
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private Double latitude;   // 위도
    private Double longitude;  // 경도
    private Double altitude;   // 고도 (층수 또는 높이)
    private String floor;      // 층 정보
    private String section;    // 구역 정보

    /**
     * 두 위치 간 거리 계산 (미터 단위)
     */
    public double distanceTo(Location other) {
        if (other == null) return Double.MAX_VALUE;

        final int R = 6371000; // 지구 반경 (미터)
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
