package com.safetyhub.infrastructure.messaging;

/**
 * 이벤트 발행 예외
 *
 * 이벤트 발행 중 발생하는 예외
 *
 * 발생 상황:
 * - Kafka 브로커 연결 실패
 * - 직렬화 실패
 * - 타임아웃
 * - 토픽 권한 없음
 * - 메시지 크기 초과
 *
 * 보안 고려사항:
 * - 에러 메시지에 민감정보 노출 금지
 * - 스택 트레이스에 페이로드 내용 포함 금지
 */
public class EventPublishException extends RuntimeException {

    /**
     * 기본 생성자
     *
     * @param message 에러 메시지
     */
    public EventPublishException(String message) {
        super(message);
    }

    /**
     * 원인 예외를 포함하는 생성자
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public EventPublishException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 토픽 정보를 포함하는 생성자
     *
     * @param topic 토픽
     * @param message 에러 메시지
     */
    public EventPublishException(EventTopic topic, String message) {
        super(String.format("[%s] %s", topic.getTopicName(), message));
    }

    /**
     * 토픽 정보와 원인 예외를 포함하는 생성자
     *
     * @param topic 토픽
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public EventPublishException(EventTopic topic, String message, Throwable cause) {
        super(String.format("[%s] %s", topic.getTopicName(), message), cause);
    }
}
