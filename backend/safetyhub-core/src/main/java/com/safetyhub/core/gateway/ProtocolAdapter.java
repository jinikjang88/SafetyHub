package com.safetyhub.core.gateway;

/**
 * 프로토콜 어댑터 인터페이스
 *
 * Port & Adapter 패턴의 핵심 인터페이스
 * - 각 프로토콜별 메시지를 통합 MessageEnvelope로 변환
 * - 반대로 MessageEnvelope를 프로토콜별 메시지로 변환
 *
 * 구현체:
 * - MqttProtocolAdapter: MQTT 메시지 변환
 * - WebSocketProtocolAdapter: WebSocket (STOMP) 메시지 변환
 * - RestProtocolAdapter: REST API 요청/응답 변환
 * - SimulatorProtocolAdapter: 시뮬레이터 메시지 변환
 *
 * 설계 원칙:
 * - 단방향 의존성: Adapter → Core (Core는 Adapter를 모름)
 * - 느슨한 결합: 프로토콜 변경이 Core에 영향을 주지 않음
 * - 확장 가능: 새로운 프로토콜 추가 시 인터페이스만 구현
 *
 * @param <T> 프로토콜별 메시지 타입 (예: MqttMessage, StompMessage 등)
 * @see MessageEnvelope
 * @see Protocol
 */
public interface ProtocolAdapter<T> {

    /**
     * 프로토콜별 메시지를 통합 MessageEnvelope로 변환
     *
     * 보안 고려사항:
     * - 입력 메시지 null 검증 필수
     * - 페이로드 크기 검증 (1MB 제한)
     * - 악의적인 메시지 필터링 (XSS, SQL Injection 등)
     * - 잘못된 형식의 메시지 처리
     *
     * @param message 프로토콜별 원본 메시지
     * @return 통합 MessageEnvelope
     * @throws IllegalArgumentException 메시지가 null이거나 유효하지 않은 경우
     * @throws MessageConversionException 변환 중 에러 발생 시
     */
    MessageEnvelope toEnvelope(T message);

    /**
     * 통합 MessageEnvelope를 프로토콜별 메시지로 변환
     *
     * @param envelope 통합 MessageEnvelope
     * @return 프로토콜별 메시지
     * @throws IllegalArgumentException envelope이 null이거나 유효하지 않은 경우
     * @throws MessageConversionException 변환 중 에러 발생 시
     */
    T fromEnvelope(MessageEnvelope envelope);

    /**
     * 이 어댑터가 지원하는 프로토콜 타입 반환
     *
     * @return 프로토콜 타입
     */
    Protocol getSupportedProtocol();

    /**
     * 메시지 타입이 지원되는지 확인
     *
     * @param message 확인할 메시지
     * @return 지원 여부
     */
    boolean supports(T message);
}
