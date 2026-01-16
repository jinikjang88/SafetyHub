package com.safetyhub.core.domain;

/**
 * 작업 상태 정의
 */
public enum TaskStatus {
    /**
     * 대기 중: 작업이 큐에 들어와 있지만 아직 할당되지 않음
     */
    PENDING,

    /**
     * 할당됨: 로봇에 할당되었지만 아직 시작하지 않음
     */
    ASSIGNED,

    /**
     * 진행 중: 로봇이 작업을 수행 중
     */
    IN_PROGRESS,

    /**
     * 완료: 작업이 성공적으로 완료됨
     */
    COMPLETED,

    /**
     * 실패: 작업 수행 중 실패
     */
    FAILED,

    /**
     * 취소됨: 작업이 취소됨
     */
    CANCELLED;

    /**
     * 작업이 종료 상태인지 확인
     * @return 종료 상태(완료/실패/취소)이면 true
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    /**
     * 작업이 활성 상태인지 확인
     * @return 활성 상태(할당됨/진행중)이면 true
     */
    public boolean isActive() {
        return this == ASSIGNED || this == IN_PROGRESS;
    }

    /**
     * 작업이 대기 중인지 확인
     * @return 대기 상태이면 true
     */
    public boolean isPending() {
        return this == PENDING;
    }
}
