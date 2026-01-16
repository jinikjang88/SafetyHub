package com.safetyhub.adapter.simulator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetyhub.core.event.EventPriority;
import com.safetyhub.core.gateway.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 시뮬레이터 프로토콜 어댑터
 *
 * 로봇 시뮬레이터의 메시지를 MessageEnvelope로 변환
 * - JSON 기반 메시지 형식
 * - 개발/테스트 환경에서 사용
 *
 * 메시지 형식 예시:
 * <pre>
 * {
 *   "type": "EVENT",
 *   "source": "robot-001",
 *   "target": "server",
 *   "priority": "NORMAL",
 *   "payload": {
 *     "eventType": "POSITION_UPDATE",
 *     "x": 10.5,
 *     "y": 20.3
 *   }
 * }
 * </pre>
 */
@Slf4j
@Component
public class SimulatorProtocolAdapter extends AbstractProtocolAdapter<String> {

    private final ObjectMapper objectMapper;

    public SimulatorProtocolAdapter(ObjectMapper objectMapper) {
        super(Protocol.SIMULATOR);
        this.objectMapper = objectMapper;
    }

    @Override
    protected MessageEnvelope doToEnvelope(String message) {
        try {
            // JSON 파싱
            JsonNode root = objectMapper.readTree(message);

            // 필드 추출
            MessageType messageType = MessageType.valueOf(
                root.path("type").asText("EVENT"));
            String source = root.path("source").asText();
            String target = root.path("target").asText(null);
            EventPriority priority = EventPriority.valueOf(
                root.path("priority").asText("NORMAL"));

            // 페이로드 추출 (JSON 객체 전체를 payload로)
            JsonNode payloadNode = root.path("payload");
            byte[] payload = objectMapper.writeValueAsBytes(payloadNode);

            // 페이로드 크기 검증
            validatePayloadSize(payload);

            // 메타데이터 추출
            Map<String, String> metadata = new HashMap<>();
            JsonNode metadataNode = root.path("metadata");
            if (metadataNode.isObject()) {
                metadataNode.fields().forEachRemaining(entry ->
                    metadata.put(entry.getKey(), entry.getValue().asText()));
            }

            // MessageEnvelope 생성
            MessageEnvelope.MessageEnvelopeBuilder builder = MessageEnvelope.builder()
                .messageType(messageType)
                .protocol(Protocol.SIMULATOR)
                .source(source)
                .priority(priority)
                .payload(payload)
                .metadata(metadata);

            // 선택 필드 설정
            if (target != null && !target.isEmpty()) {
                builder.target(target);
            }

            if (root.has("messageId")) {
                builder.messageId(root.path("messageId").asText());
            }

            if (root.has("correlationId")) {
                builder.correlationId(root.path("correlationId").asText());
            }

            return builder.build();

        } catch (JsonProcessingException e) {
            throw new MessageConversionException(Protocol.SIMULATOR,
                "JSON 파싱 실패: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new MessageConversionException(Protocol.SIMULATOR,
                "유효하지 않은 필드 값: " + e.getMessage(), e);
        }
    }

    @Override
    protected String doFromEnvelope(MessageEnvelope envelope) {
        try {
            Map<String, Object> message = new HashMap<>();

            // 기본 필드
            message.put("messageId", envelope.getMessageId());
            message.put("type", envelope.getMessageType().name());
            message.put("source", envelope.getSource());
            message.put("priority", envelope.getPriority().name());

            // 선택 필드
            if (envelope.getCorrelationId() != null) {
                message.put("correlationId", envelope.getCorrelationId());
            }
            if (envelope.getTarget() != null) {
                message.put("target", envelope.getTarget());
            }

            // 페이로드 (JSON으로 파싱)
            String payloadJson = new String(envelope.getPayload(), StandardCharsets.UTF_8);
            JsonNode payloadNode = objectMapper.readTree(payloadJson);
            message.put("payload", payloadNode);

            // 메타데이터
            if (envelope.getMetadata() != null && !envelope.getMetadata().isEmpty()) {
                message.put("metadata", envelope.getMetadata());
            }

            // 타임스탬프
            message.put("timestamp", envelope.getTimestamp().toString());

            return objectMapper.writeValueAsString(message);

        } catch (JsonProcessingException e) {
            throw new MessageConversionException(Protocol.SIMULATOR,
                "JSON 직렬화 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        try {
            // JSON 형식인지 확인
            objectMapper.readTree(message);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
