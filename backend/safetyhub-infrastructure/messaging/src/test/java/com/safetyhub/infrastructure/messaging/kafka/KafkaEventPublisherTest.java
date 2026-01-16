package com.safetyhub.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetyhub.core.event.EventPriority;
import com.safetyhub.core.gateway.MessageEnvelope;
import com.safetyhub.core.gateway.MessageType;
import com.safetyhub.core.gateway.Protocol;
import com.safetyhub.infrastructure.messaging.EventPublishException;
import com.safetyhub.infrastructure.messaging.EventTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * KafkaEventPublisher 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaEventPublisher 테스트")
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;
    private KafkaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        publisher = new KafkaEventPublisher(kafkaTemplate, objectMapper);
    }

    @Nested
    @DisplayName("publish 테스트 (토픽 자동 결정)")
    class PublishWithAutoTopicTest {

        @Test
        @DisplayName("긴급 이벤트는 EMERGENCY_EVENTS 토픽으로 발행")
        void publishEmergencyEventToEmergencyTopic() {
            // given
            MessageEnvelope envelope = createMessage("robot-001", EventPriority.CRITICAL);
            CompletableFuture<SendResult<String, String>> mockFuture =
                CompletableFuture.completedFuture(null);

            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(mockFuture);

            // when
            publisher.publish(envelope);

            // then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), anyString());

            assertThat(topicCaptor.getValue()).isEqualTo(EventTopic.EMERGENCY_EVENTS.getTopicName());
        }

        @Test
        @DisplayName("로봇 이벤트는 ROBOT_EVENTS 토픽으로 발행")
        void publishRobotEventToRobotTopic() {
            // given
            MessageEnvelope envelope = createMessage("robot-001", EventPriority.NORMAL);
            CompletableFuture<SendResult<String, String>> mockFuture =
                CompletableFuture.completedFuture(null);

            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(mockFuture);

            // when
            publisher.publish(envelope);

            // then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), anyString());

            assertThat(topicCaptor.getValue()).isEqualTo(EventTopic.ROBOT_EVENTS.getTopicName());
        }

        @Test
        @DisplayName("장치 이벤트는 DEVICE_EVENTS 토픽으로 발행")
        void publishDeviceEventToDeviceTopic() {
            // given
            MessageEnvelope envelope = createMessage("device-001", EventPriority.NORMAL);
            CompletableFuture<SendResult<String, String>> mockFuture =
                CompletableFuture.completedFuture(null);

            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(mockFuture);

            // when
            publisher.publish(envelope);

            // then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), anyString());

            assertThat(topicCaptor.getValue()).isEqualTo(EventTopic.DEVICE_EVENTS.getTopicName());
        }

        @Test
        @DisplayName("분석 이벤트는 ANALYTICS_EVENTS 토픽으로 발행")
        void publishAnalyticsEventToAnalyticsTopic() {
            // given
            MessageEnvelope envelope = createMessage("system-001", EventPriority.LOW);
            CompletableFuture<SendResult<String, String>> mockFuture =
                CompletableFuture.completedFuture(null);

            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(mockFuture);

            // when
            publisher.publish(envelope);

            // then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), anyString());

            assertThat(topicCaptor.getValue()).isEqualTo(EventTopic.ANALYTICS_EVENTS.getTopicName());
        }
    }

    @Nested
    @DisplayName("publish 테스트 (토픽 명시)")
    class PublishWithExplicitTopicTest {

        @Test
        @DisplayName("명시된 토픽으로 이벤트 발행")
        void publishToExplicitTopic() {
            // given
            MessageEnvelope envelope = createMessage("robot-001", EventPriority.NORMAL);
            EventTopic topic = EventTopic.SYSTEM_EVENTS;

            CompletableFuture<SendResult<String, String>> mockFuture =
                CompletableFuture.completedFuture(null);

            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(mockFuture);

            // when
            publisher.publish(topic, envelope);

            // then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), anyString());

            assertThat(topicCaptor.getValue()).isEqualTo(EventTopic.SYSTEM_EVENTS.getTopicName());
        }

        @Test
        @DisplayName("파티션 키는 소스 ID로 설정")
        void useSourceAsPartitionKey() {
            // given
            MessageEnvelope envelope = createMessage("robot-123", EventPriority.NORMAL);
            EventTopic topic = EventTopic.ROBOT_EVENTS;

            CompletableFuture<SendResult<String, String>> mockFuture =
                CompletableFuture.completedFuture(null);

            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(mockFuture);

            // when
            publisher.publish(topic, envelope);

            // then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), anyString());

            assertThat(keyCaptor.getValue()).isEqualTo("robot-123");
        }
    }

    @Nested
    @DisplayName("publish 테스트 (파티션 키 지정)")
    class PublishWithPartitionKeyTest {

        @Test
        @DisplayName("명시된 파티션 키로 이벤트 발행")
        void publishWithExplicitPartitionKey() {
            // given
            MessageEnvelope envelope = createMessage("robot-001", EventPriority.NORMAL);
            EventTopic topic = EventTopic.ROBOT_EVENTS;
            String partitionKey = "custom-key";

            CompletableFuture<SendResult<String, String>> mockFuture =
                CompletableFuture.completedFuture(null);

            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(mockFuture);

            // when
            publisher.publish(topic, partitionKey, envelope);

            // then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), anyString());

            assertThat(keyCaptor.getValue()).isEqualTo("custom-key");
        }
    }

    @Nested
    @DisplayName("입력 검증 테스트")
    class InputValidationTest {

        @Test
        @DisplayName("null topic은 예외 발생")
        void throwExceptionWhenTopicIsNull() {
            // given
            MessageEnvelope envelope = createMessage("robot-001", EventPriority.NORMAL);

            // when & then
            assertThatThrownBy(() -> publisher.publish(null, envelope))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("topic");
        }

        @Test
        @DisplayName("null partitionKey는 예외 발생")
        void throwExceptionWhenPartitionKeyIsNull() {
            // given
            MessageEnvelope envelope = createMessage("robot-001", EventPriority.NORMAL);
            EventTopic topic = EventTopic.ROBOT_EVENTS;

            // when & then
            assertThatThrownBy(() -> publisher.publish(topic, null, envelope))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("partitionKey");
        }

        @Test
        @DisplayName("null envelope은 예외 발생")
        void throwExceptionWhenEnvelopeIsNull() {
            // given
            EventTopic topic = EventTopic.ROBOT_EVENTS;
            String partitionKey = "key";

            // when & then
            assertThatThrownBy(() -> publisher.publish(topic, partitionKey, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("envelope");
        }
    }

    @Nested
    @DisplayName("KafkaEventMessage 변환 테스트")
    class KafkaEventMessageConversionTest {

        @Test
        @DisplayName("MessageEnvelope를 KafkaEventMessage로 변환")
        void convertEnvelopeToKafkaMessage() {
            // given
            MessageEnvelope envelope = createMessage("robot-001", EventPriority.NORMAL);

            // when
            KafkaEventMessage kafkaMessage = KafkaEventMessage.fromEnvelope(envelope);

            // then
            assertThat(kafkaMessage.getMessageId()).isEqualTo(envelope.getMessageId());
            assertThat(kafkaMessage.getSource()).isEqualTo(envelope.getSource());
            assertThat(kafkaMessage.getPriority()).isEqualTo(envelope.getPriority());
            assertThat(kafkaMessage.getPayloadAsString()).isEqualTo("test payload");
        }

        @Test
        @DisplayName("KafkaEventMessage를 MessageEnvelope로 역변환")
        void convertKafkaMessageToEnvelope() {
            // given
            MessageEnvelope original = createMessage("robot-001", EventPriority.NORMAL);
            KafkaEventMessage kafkaMessage = KafkaEventMessage.fromEnvelope(original);

            // when
            MessageEnvelope restored = kafkaMessage.toEnvelope();

            // then
            assertThat(restored.getMessageId()).isEqualTo(original.getMessageId());
            assertThat(restored.getSource()).isEqualTo(original.getSource());
            assertThat(restored.getPriority()).isEqualTo(original.getPriority());
            assertThat(restored.getPayloadAsString()).isEqualTo(original.getPayloadAsString());
        }
    }

    /**
     * 테스트용 메시지 생성 헬퍼
     */
    private MessageEnvelope createMessage(String source, EventPriority priority) {
        return MessageEnvelope.builder()
            .messageType(MessageType.EVENT)
            .protocol(Protocol.SIMULATOR)
            .source(source)
            .priority(priority)
            .payload("test payload".getBytes(StandardCharsets.UTF_8))
            .build();
    }
}
