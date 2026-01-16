package com.safetyhub.application.path;

import com.safetyhub.core.domain.Location;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * PathResult의 간단한 구현체
 */
@Getter
@Builder
@ToString
public class SimplePathResult implements PathService.PathResult {

    private final List<Location> path;
    private final double totalDistance;
    private final int steps;
    private final boolean cached;
    private final long calculationTimeMs;

    @Override
    public boolean isValid() {
        return path != null && !path.isEmpty();
    }

    /**
     * 빈 결과 생성 (경로를 찾지 못한 경우)
     */
    public static SimplePathResult empty() {
        return SimplePathResult.builder()
                .path(Collections.emptyList())
                .totalDistance(0.0)
                .steps(0)
                .cached(false)
                .calculationTimeMs(0)
                .build();
    }

    /**
     * PathInfo를 SimplePathResult로 변환
     */
    public static SimplePathResult from(com.safetyhub.core.domain.PathFinder.PathInfo pathInfo,
                                       boolean cached,
                                       long calculationTimeMs) {
        if (pathInfo == null || !pathInfo.isValid()) {
            return empty();
        }

        return SimplePathResult.builder()
                .path(pathInfo.getPath())
                .totalDistance(pathInfo.getTotalDistance())
                .steps(pathInfo.getSteps())
                .cached(cached)
                .calculationTimeMs(calculationTimeMs)
                .build();
    }
}
