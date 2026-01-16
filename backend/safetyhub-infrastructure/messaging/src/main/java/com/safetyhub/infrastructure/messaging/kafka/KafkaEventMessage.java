package com.safetyhub.infrastructure.messaging.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.safetyhub.core.event.EventPriority;
import com.safetyhub.core.gateway.MessageEnvelope;
import com.safetyhub.core.gateway.MessageType;
import com.safetyhub.core.gateway.Protocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Kafka 이벤트 메시지 DTO
 *
 * MessageEnvelope를 Kafka에 전송하기 위한 직렬화 가능한 DTO
 *
 * 특징:
 * - JSON 직렬화 가능
 * - payload를 Base64로 인코딩 (바이너리 데이터 전송)
 * - 모든 필드 포함
 *
 * 보안:
 * - 민감정보는 암호화하여 전송 (TODO)
 * - Base64 인코딩으로 바이너리 안전 전송
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KafkaEventMessage {

    private String messageId;
    private String correlationId;
    private MessageType messageType;
    private Protocol protocol;
    private String source;
    private String target;
    private EventPriority priority;
    private Instant timestamp;

    /**
     * payload (Base64 인코딩)
     *
     * byte[]를 JSON으로 직렬화하기 어려우므로 Base64로 인코딩
     */
    private String payload;

    private Map<String, String> metadata;

    /**
     * MessageEnvelope로부터 KafkaEventMessage 생성
     *
     * @param envelope MessageEnvelope
     * @return KafkaEventMessage
     */
    public static KafkaEventMessage fromEnvelope(MessageEnvelope envelope) {
        return KafkaEventMessage.builder()
            .messageId(envelope.getMessageId())
            .correlationId(envelope.getCorrelationId())
            .messageType(envelope.getMessageType())
            .protocol(envelope.getProtocol())
            .source(envelope.getSource())
            .target(envelope.getTarget())
            .priority(envelope.getPriority())
            .timestamp(envelope.getTimestamp())
            .payload(Base64.getEncoder().encodeToString(envelope.getPayload()))
            .metadata(envelope.getMetadata())
            .build();
    }

    /**
     * KafkaEventMessage를 MessageEnvelope로 변환
     *
     * @return MessageEnvelope
     */
    public MessageEnvelope toEnvelope() {
        byte[] decodedPayload = Base64.getDecoder().decode(payload);

        MessageEnvelope.MessageEnvelopeBuilder builder = MessageEnvelope.builder()
            .messageId(messageId)
            .correlationId(correlationId)
            .messageType(messageType)
            .protocol(protocol)
            .source(source)
            .target(target)
            .priority(priority)
            .timestamp(timestamp)
            .payload(decodedPayload)
            .metadata(metadata);

        return builder.build();
    }

    /**
     * payload를 문자열로 변환 (디버깅용)
     *
     * @return UTF-8 문자열
     */
    public String getPayloadAsString() {
        if (payload == null) {
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(payload);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
