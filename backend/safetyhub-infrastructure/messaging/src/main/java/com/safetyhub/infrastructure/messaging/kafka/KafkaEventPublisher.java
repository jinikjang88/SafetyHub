package com.safetyhub.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetyhub.core.gateway.MessageEnvelope;
import com.safetyhub.infrastructure.messaging.EventPublishException;
import com.safetyhub.infrastructure.messaging.EventPublisher;
import com.safetyhub.infrastructure.messaging.EventTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka 이벤트 발행자 구현체
 *
 * Spring Kafka를 사용한 이벤트 발행
 *
 * 특징:
 * - 비동기 발행 (CompletableFuture)
 * - JSON 직렬화
 * - 파티션 키 지원 (순서 보장)
 * - 에러 처리 및 재시도
 *
 * 보안:
 * - 입력 검증
 * - 직렬화 에러 처리
 * - 타임아웃 설정
 * - 민감정보 로깅 방지
 *
 * 성능:
 * - 배치 전송 (linger.ms)
 * - 압축 (compression.type)
 * - 비동기 처리
 */
@Slf4j
@Component
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 생성자 주입
     *
     * @param kafkaTemplate Kafka 템플릿
     * @param objectMapper JSON 직렬화
     */
    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate은 필수입니다");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper는 필수입니다");
    }

    @Override
    public CompletableFuture<Void> publish(MessageEnvelope envelope) {
        // 토픽 자동 결정
        EventTopic topic = determineTopicFromEnvelope(envelope);

        // 파티션 키: 소스 ID (순서 보장)
        String partitionKey = envelope.getSource();

        return publish(topic, partitionKey, envelope);
    }

    @Override
    public CompletableFuture<Void> publish(EventTopic topic, MessageEnvelope envelope) {
        // 파티션 키: 소스 ID
        String partitionKey = envelope.getSource();

        return publish(topic, partitionKey, envelope);
    }

    @Override
    public CompletableFuture<Void> publish(EventTopic topic, String partitionKey, MessageEnvelope envelope) {
        // 입력 검증
        Objects.requireNonNull(topic, "topic은 null일 수 없습니다");
        Objects.requireNonNull(partitionKey, "partitionKey는 null일 수 없습니다");
        Objects.requireNonNull(envelope, "envelope은 null일 수 없습니다");

        try {
            // MessageEnvelope를 JSON으로 직렬화
            String messageJson = serializeEnvelope(envelope);

            // Kafka로 전송 (비동기)
            CompletableFuture<SendResult<String, String>> kafkaFuture =
                kafkaTemplate.send(topic.getTopicName(), partitionKey, messageJson);

            // 결과 처리
            return kafkaFuture
                .thenAccept(result -> {
                    log.debug("이벤트 발행 성공: topic={}, partition={}, offset={}, messageId={}",
                        topic.getTopicName(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        envelope.getMessageId());
                })
                .exceptionally(ex -> {
                    log.error("이벤트 발행 실패: topic={}, messageId={}",
                        topic.getTopicName(), envelope.getMessageId(), ex);
                    throw new EventPublishException(topic, "이벤트 발행 실패", ex);
                });

        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패: messageId={}", envelope.getMessageId(), e);
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(
                new EventPublishException(topic, "이벤트 직렬화 실패", e));
            return failedFuture;
        } catch (Exception e) {
            log.error("예상치 못한 에러: messageId={}", envelope.getMessageId(), e);
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(
                new EventPublishException(topic, "예상치 못한 에러", e));
            return failedFuture;
        }
    }

    /**
     * MessageEnvelope를 JSON으로 직렬화
     *
     * @param envelope 직렬화할 envelope
     * @return JSON 문자열
     * @throws JsonProcessingException 직렬화 실패 시
     */
    private String serializeEnvelope(MessageEnvelope envelope) throws JsonProcessingException {
        // MessageEnvelope를 직접 직렬화하면 payload가 byte[]이므로
        // KafkaEventMessage DTO로 변환 후 직렬화
        KafkaEventMessage message = KafkaEventMessage.fromEnvelope(envelope);
        return objectMapper.writeValueAsString(message);
    }

    /**
     * MessageEnvelope로부터 토픽 자동 결정
     *
     * 규칙:
     * - CRITICAL, HIGH 우선순위 → EMERGENCY_EVENTS
     * - Protocol이 SIMULATOR → 소스에 따라 분류
     * - 기본: SYSTEM_EVENTS
     *
     * @param envelope MessageEnvelope
     * @return 결정된 토픽
     */
    private EventTopic determineTopicFromEnvelope(MessageEnvelope envelope) {
        // 긴급 이벤트는 별도 토픽
        if (envelope.isHotPath()) {
            return EventTopic.EMERGENCY_EVENTS;
        }

        // 소스 기반 토픽 결정
        String source = envelope.getSource();
        if (source.startsWith("robot-") || source.startsWith("virtual-robot-")) {
            return EventTopic.ROBOT_EVENTS;
        } else if (source.startsWith("device-") || source.startsWith("safetykit-")) {
            return EventTopic.DEVICE_EVENTS;
        } else if (source.startsWith("worker-") || source.startsWith("lifeguard-")) {
            return EventTopic.WORKER_EVENTS;
        }

        // Cold Path는 분석용 토픽
        if (envelope.isColdPath()) {
            return EventTopic.ANALYTICS_EVENTS;
        }

        // 기본 토픽
        return EventTopic.SYSTEM_EVENTS;
    }
}
