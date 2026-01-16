package com.safetyhub.core.domain;

import lombok.Getter;

/**
 * 작업 우선순위 정의
 * 숫자가 낮을수록 높은 우선순위
 */
@Getter
public enum TaskPriority {
    /**
     * 긴급: 즉시 처리 필요 (예: 긴급 대피, 119 신고)
     * Hot Path 처리 대상
     */
    CRITICAL(0, 0),

    /**
     * 높음: 빠른 처리 필요 (예: 안전 점검, 위험 상황 대응)
     * Hot Path 처리 대상
     */
    HIGH(1, 0),

    /**
     * 보통: 일반 작업 (예: 정기 순찰, 물품 배송)
     * Warm Path 처리 대상
     */
    NORMAL(2, 3600),  // 1시간 대기 시 승격

    /**
     * 낮음: 급하지 않은 작업 (예: 청소, 데이터 수집)
     * Cold Path 처리 대상
     */
    LOW(3, 7200);     // 2시간 대기 시 승격

    /**
     * 우선순위 레벨 (0이 가장 높음)
     */
    private final int level;

    /**
     * 승격 임계값 (초 단위)
     * 이 시간 이상 대기하면 상위 우선순위로 승격
     * 0이면 승격 없음
     */
    private final int promotionThresholdSeconds;

    TaskPriority(int level, int promotionThresholdSeconds) {
        this.level = level;
        this.promotionThresholdSeconds = promotionThresholdSeconds;
    }

    /**
     * 대기 시간에 따라 승격된 우선순위 계산
     * @param waitingSeconds 대기 시간 (초)
     * @return 승격된 우선순위
     */
    public TaskPriority getPromotedPriority(long waitingSeconds) {
        if (promotionThresholdSeconds == 0 || waitingSeconds < promotionThresholdSeconds) {
            return this;
        }

        // 한 단계 상위 우선순위로 승격
        switch (this) {
            case LOW:
                return NORMAL;
            case NORMAL:
                return HIGH;
            case HIGH:
            case CRITICAL:
            default:
                return this;  // 최고 우선순위는 더 이상 승격 안 됨
        }
    }

    /**
     * Hot Path 대상인지 확인
     * @return CRITICAL 또는 HIGH이면 true
     */
    public boolean isHotPath() {
        return this == CRITICAL || this == HIGH;
    }

    /**
     * Warm Path 대상인지 확인
     * @return NORMAL이면 true
     */
    public boolean isWarmPath() {
        return this == NORMAL;
    }

    /**
     * Cold Path 대상인지 확인
     * @return LOW이면 true
     */
    public boolean isColdPath() {
        return this == LOW;
    }

    /**
     * 다른 우선순위와 비교
     * @param other 비교할 우선순위
     * @return 이 우선순위가 더 높으면 음수, 같으면 0, 낮으면 양수
     */
    public int compareTo(TaskPriority other) {
        return Integer.compare(this.level, other.level);
    }
}
