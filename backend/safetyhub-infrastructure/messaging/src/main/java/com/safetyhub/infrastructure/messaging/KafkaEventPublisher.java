package com.safetyhub.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetyhub.core.event.DomainEvent;
import com.safetyhub.core.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka 이벤트 발행자 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_TOPIC = "safetyhub-events";
    private static final String EMERGENCY_TOPIC = "safetyhub-emergencies";

    @Override
    public void publish(DomainEvent event) {
        String topic = determineTopic(event);
        publish(topic, event);
    }

    @Override
    public void publish(String topic, DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getEventId(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event: {} to topic: {}",
                                    event.getEventId(), topic, ex);
                        } else {
                            log.debug("Event published: {} to topic: {} partition: {}",
                                    event.getEventId(), topic,
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventId(), e);
        }
    }

    /**
     * 이벤트 타입에 따른 토픽 결정
     */
    private String determineTopic(DomainEvent event) {
        if (event.getPriority() == DomainEvent.EventPriority.CRITICAL ||
                event.getPriority() == DomainEvent.EventPriority.HIGH) {
            return EMERGENCY_TOPIC;
        }
        return DEFAULT_TOPIC;
    }
}
