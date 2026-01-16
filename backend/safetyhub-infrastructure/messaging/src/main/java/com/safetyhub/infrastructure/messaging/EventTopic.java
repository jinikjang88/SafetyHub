package com.safetyhub.infrastructure.messaging;

/**
 * Kafka 이벤트 토픽 정의
 *
 * 토픽 네이밍 규칙:
 * - 소문자 사용
 * - 도메인.이벤트타입 형식
 * - 예: robot.events, device.events
 *
 * 토픽 전략:
 * - 도메인별 분리 (확장성)
 * - 이벤트 타입별 분리 (필터링 용이)
 * - 파티션 키: 소스 ID (순서 보장)
 *
 * 보안:
 * - 토픽별 ACL 설정 가능
 * - 민감정보는 암호화하여 전송
 */
public enum EventTopic {
    /**
     * 로봇 이벤트
     * - 위치 업데이트
     * - 상태 변경
     * - 작업 완료
     * - 긴급 상황
     */
    ROBOT_EVENTS("robot.events"),

    /**
     * 장치 이벤트 (SafetyKit)
     * - 센서 데이터
     * - 전원 상태
     * - 긴급 정지
     * - 이상 감지
     */
    DEVICE_EVENTS("device.events"),

    /**
     * 작업자 이벤트 (LifeGuard)
     * - 생체 데이터
     * - 위치 추적
     * - 낙상 감지
     * - 건강 이상
     */
    WORKER_EVENTS("worker.events"),

    /**
     * 긴급 이벤트 (Hot Path)
     * - 화재
     * - 가스 누출
     * - 작업자 낙상
     * - 설비 이상
     *
     * 특징:
     * - 높은 우선순위
     * - 짧은 보존 기간 (1일)
     * - 실시간 처리
     */
    EMERGENCY_EVENTS("emergency.events"),

    /**
     * 시스템 이벤트
     * - 감사 로그
     * - 시스템 상태
     * - 성능 메트릭
     */
    SYSTEM_EVENTS("system.events"),

    /**
     * 분석용 이벤트 (Cold Path)
     * - 통계 데이터
     * - 집계 데이터
     * - 리포팅 데이터
     *
     * 특징:
     * - 배치 처리
     * - 긴 보존 기간 (30일)
     */
    ANALYTICS_EVENTS("analytics.events");

    private final String topicName;

    EventTopic(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    @Override
    public String toString() {
        return topicName;
    }
}
