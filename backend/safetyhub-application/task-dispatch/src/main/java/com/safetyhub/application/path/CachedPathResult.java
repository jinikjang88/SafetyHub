package com.safetyhub.application.path;

import com.safetyhub.core.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 직렬화 가능한 PathResult 구현 (Redis 캐시용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedPathResult implements PathService.PathResult, Serializable {

    private static final long serialVersionUID = 1L;

    private List<Location> path;
    private double totalDistance;
    private int steps;
    private boolean cached;
    private long calculationTimeMs;

    @Override
    public boolean isValid() {
        return path != null && !path.isEmpty();
    }

    /**
     * PathResult를 CachedPathResult로 변환
     */
    public static CachedPathResult from(PathService.PathResult result) {
        if (result == null) {
            return null;
        }

        return CachedPathResult.builder()
                .path(result.getPath())
                .totalDistance(result.getTotalDistance())
                .steps(result.getSteps())
                .cached(true)  // 캐시에서 나온 것으로 표시
                .calculationTimeMs(result.getCalculationTimeMs())
                .build();
    }

    /**
     * 캐시된 결과를 반환할 때 cached 플래그를 true로 설정
     */
    public CachedPathResult withCached(boolean cached) {
        return CachedPathResult.builder()
                .path(this.path)
                .totalDistance(this.totalDistance)
                .steps(this.steps)
                .cached(cached)
                .calculationTimeMs(this.calculationTimeMs)
                .build();
    }
}
