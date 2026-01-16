package com.safetyhub.core.gateway;

import com.safetyhub.core.event.EventPriority;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 통합 메시지 봉투 (Envelope)
 *
 * 모든 프로토콜의 메시지를 통합된 형태로 변환하는 공통 구조
 * - Port & Adapter 패턴의 핵심 데이터 구조
 * - 불변 객체로 설계하여 스레드 안전성 보장
 *
 * 보안 고려사항:
 * - 모든 필드는 final로 불변성 보장
 * - 빌더 패턴으로 필수 필드 강제
 * - payload 크기 제한 (1MB)
 * - 민감정보는 payload에 암호화하여 저장
 *
 * @see Protocol
 * @see MessageType
 */
@Getter
@Builder
@ToString(exclude = "payload") // payload는 로그에 노출 방지
public class MessageEnvelope {

    /**
     * 메시지 최대 크기 (1MB)
     * DoS 공격 방지를 위한 제한
     */
    public static final int MAX_PAYLOAD_SIZE = 1024 * 1024;

    /**
     * 메시지 고유 ID
     * - 멱등성 보장 및 추적을 위한 식별자
     * - 필수 필드
     */
    private final String messageId;

    /**
     * 상관 ID (Correlation ID)
     * - 요청-응답 매칭
     * - 분산 추적
     * - 선택 필드 (null 가능)
     */
    private final String correlationId;

    /**
     * 메시지 타입
     * - 필수 필드
     */
    private final MessageType messageType;

    /**
     * 프로토콜
     * - 어느 프로토콜에서 왔는지 추적
     * - 필수 필드
     */
    private final Protocol protocol;

    /**
     * 소스 (송신자)
     * - 장치 ID, 클라이언트 ID 등
     * - 필수 필드
     */
    private final String source;

    /**
     * 대상 (수신자)
     * - null일 경우 브로드캐스트
     * - 선택 필드
     */
    private final String target;

    /**
     * 우선순위
     * - Hot/Warm/Cold Path 분기에 사용
     * - 필수 필드
     */
    private final EventPriority priority;

    /**
     * 타임스탬프
     * - 메시지 생성 시간 (UTC)
     * - 필수 필드
     */
    private final Instant timestamp;

    /**
     * 페이로드 (실제 데이터)
     * - 프로토콜별 원본 데이터
     * - JSON, Protobuf 등 다양한 형식 지원
     * - 필수 필드
     */
    private final byte[] payload;

    /**
     * 메타데이터
     * - 추가 정보 (헤더, 속성 등)
     * - 선택 필드
     */
    private final Map<String, String> metadata;

    /**
     * 빌더 내부 클래스
     * 필수 필드 검증 로직 포함
     */
    public static class MessageEnvelopeBuilder {

        /**
         * 빌드 전 검증
         *
         * @throws IllegalArgumentException 필수 필드 누락 또는 유효하지 않은 값
         */
        public MessageEnvelope build() {
            // 필수 필드 검증
            Objects.requireNonNull(messageType, "messageType은 필수입니다");
            Objects.requireNonNull(protocol, "protocol은 필수입니다");
            Objects.requireNonNull(source, "source는 필수입니다");
            Objects.requireNonNull(priority, "priority는 필수입니다");
            Objects.requireNonNull(payload, "payload는 필수입니다");

            // 문자열 필드 검증
            validateNotBlank(source, "source");

            // 페이로드 크기 검증 (DoS 방지)
            if (payload.length > MAX_PAYLOAD_SIZE) {
                throw new IllegalArgumentException(
                    String.format("payload 크기가 제한을 초과했습니다. (현재: %d, 최대: %d)",
                        payload.length, MAX_PAYLOAD_SIZE)
                );
            }

            // messageId 자동 생성 (없을 경우)
            if (messageId == null) {
                messageId = UUID.randomUUID().toString();
            }

            // timestamp 자동 생성 (없을 경우)
            if (timestamp == null) {
                timestamp = Instant.now();
            }

            return new MessageEnvelope(
                messageId, correlationId, messageType, protocol,
                source, target, priority, timestamp, payload, metadata
            );
        }

        /**
         * 문자열 필드 공백 검증
         */
        private void validateNotBlank(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + "은(는) 비어있을 수 없습니다");
            }
        }
    }

    /**
     * 응답 메시지 생성 헬퍼
     *
     * @param responsePayload 응답 페이로드
     * @return 응답 메시지 봉투
     */
    public MessageEnvelope createResponse(byte[] responsePayload) {
        return MessageEnvelope.builder()
            .messageId(UUID.randomUUID().toString())
            .correlationId(this.messageId) // 원본 messageId를 correlationId로
            .messageType(MessageType.RESPONSE)
            .protocol(this.protocol)
            .source(this.target) // 원본 target이 응답의 source
            .target(this.source) // 원본 source가 응답의 target
            .priority(this.priority)
            .payload(responsePayload)
            .metadata(this.metadata)
            .build();
    }

    /**
     * 페이로드를 문자열로 변환
     *
     * @return UTF-8 문자열
     */
    public String getPayloadAsString() {
        return new String(payload, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Hot Path 메시지인지 확인
     *
     * @return CRITICAL 또는 HIGH 우선순위이면 true
     */
    public boolean isHotPath() {
        return priority == EventPriority.CRITICAL || priority == EventPriority.HIGH;
    }

    /**
     * Cold Path 메시지인지 확인
     *
     * @return LOW 우선순위이면 true
     */
    public boolean isColdPath() {
        return priority == EventPriority.LOW;
    }
}
