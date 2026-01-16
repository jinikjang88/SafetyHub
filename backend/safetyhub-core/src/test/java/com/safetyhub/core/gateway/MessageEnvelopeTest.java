package com.safetyhub.core.gateway;

import com.safetyhub.core.event.EventPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MessageEnvelope 도메인 모델 테스트
 *
 * 보안 및 유효성 검증 테스트 포함
 */
@DisplayName("MessageEnvelope 테스트")
class MessageEnvelopeTest {

    @Nested
    @DisplayName("정상 생성 테스트")
    class CreateMessageEnvelopeTest {

        @Test
        @DisplayName("필수 필드만으로 메시지 생성")
        void createWithRequiredFields() {
            // given
            byte[] payload = "test payload".getBytes(StandardCharsets.UTF_8);

            // when
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT)
                .source("device-001")
                .priority(EventPriority.NORMAL)
                .payload(payload)
                .build();

            // then
            assertThat(envelope).isNotNull();
            assertThat(envelope.getMessageId()).isNotNull(); // 자동 생성
            assertThat(envelope.getTimestamp()).isNotNull(); // 자동 생성
            assertThat(envelope.getMessageType()).isEqualTo(MessageType.EVENT);
            assertThat(envelope.getProtocol()).isEqualTo(Protocol.MQTT);
            assertThat(envelope.getSource()).isEqualTo("device-001");
            assertThat(envelope.getPriority()).isEqualTo(EventPriority.NORMAL);
            assertThat(envelope.getPayload()).isEqualTo(payload);
        }

        @Test
        @DisplayName("모든 필드를 포함하여 메시지 생성")
        void createWithAllFields() {
            // given
            String messageId = "msg-001";
            String correlationId = "corr-001";
            Instant timestamp = Instant.now();
            byte[] payload = "test".getBytes(StandardCharsets.UTF_8);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("key", "value");

            // when
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageId(messageId)
                .correlationId(correlationId)
                .messageType(MessageType.COMMAND)
                .protocol(Protocol.WEBSOCKET)
                .source("client-001")
                .target("device-001")
                .priority(EventPriority.HIGH)
                .timestamp(timestamp)
                .payload(payload)
                .metadata(metadata)
                .build();

            // then
            assertThat(envelope.getMessageId()).isEqualTo(messageId);
            assertThat(envelope.getCorrelationId()).isEqualTo(correlationId);
            assertThat(envelope.getTarget()).isEqualTo("device-001");
            assertThat(envelope.getTimestamp()).isEqualTo(timestamp);
            assertThat(envelope.getMetadata()).isEqualTo(metadata);
        }
    }

    @Nested
    @DisplayName("필수 필드 검증 테스트")
    class RequiredFieldValidationTest {

        @Test
        @DisplayName("messageType이 null이면 예외 발생")
        void throwExceptionWhenMessageTypeIsNull() {
            // when & then
            assertThatThrownBy(() ->
                MessageEnvelope.builder()
                    .protocol(Protocol.MQTT)
                    .source("device-001")
                    .priority(EventPriority.NORMAL)
                    .payload("test".getBytes(StandardCharsets.UTF_8))
                    .build()
            )
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("messageType");
        }

        @Test
        @DisplayName("protocol이 null이면 예외 발생")
        void throwExceptionWhenProtocolIsNull() {
            // when & then
            assertThatThrownBy(() ->
                MessageEnvelope.builder()
                    .messageType(MessageType.EVENT)
                    .source("device-001")
                    .priority(EventPriority.NORMAL)
                    .payload("test".getBytes(StandardCharsets.UTF_8))
                    .build()
            )
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("protocol");
        }

        @Test
        @DisplayName("source가 null이면 예외 발생")
        void throwExceptionWhenSourceIsNull() {
            // when & then
            assertThatThrownBy(() ->
                MessageEnvelope.builder()
                    .messageType(MessageType.EVENT)
                    .protocol(Protocol.MQTT)
                    .priority(EventPriority.NORMAL)
                    .payload("test".getBytes(StandardCharsets.UTF_8))
                    .build()
            )
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("source");
        }

        @Test
        @DisplayName("source가 빈 문자열이면 예외 발생")
        void throwExceptionWhenSourceIsBlank() {
            // when & then
            assertThatThrownBy(() ->
                MessageEnvelope.builder()
                    .messageType(MessageType.EVENT)
                    .protocol(Protocol.MQTT)
                    .source("   ") // 공백
                    .priority(EventPriority.NORMAL)
                    .payload("test".getBytes(StandardCharsets.UTF_8))
                    .build()
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("비어있을 수 없습니다");
        }

        @Test
        @DisplayName("priority가 null이면 예외 발생")
        void throwExceptionWhenPriorityIsNull() {
            // when & then
            assertThatThrownBy(() ->
                MessageEnvelope.builder()
                    .messageType(MessageType.EVENT)
                    .protocol(Protocol.MQTT)
                    .source("device-001")
                    .payload("test".getBytes(StandardCharsets.UTF_8))
                    .build()
            )
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("priority");
        }

        @Test
        @DisplayName("payload가 null이면 예외 발생")
        void throwExceptionWhenPayloadIsNull() {
            // when & then
            assertThatThrownBy(() ->
                MessageEnvelope.builder()
                    .messageType(MessageType.EVENT)
                    .protocol(Protocol.MQTT)
                    .source("device-001")
                    .priority(EventPriority.NORMAL)
                    .build()
            )
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("payload");
        }
    }

    @Nested
    @DisplayName("보안 검증 테스트")
    class SecurityValidationTest {

        @Test
        @DisplayName("페이로드 크기가 1MB를 초과하면 예외 발생 (DoS 방지)")
        void throwExceptionWhenPayloadExceedsMaxSize() {
            // given
            byte[] largePayload = new byte[MessageEnvelope.MAX_PAYLOAD_SIZE + 1]; // 1MB + 1

            // when & then
            assertThatThrownBy(() ->
                MessageEnvelope.builder()
                    .messageType(MessageType.EVENT)
                    .protocol(Protocol.MQTT)
                    .source("device-001")
                    .priority(EventPriority.NORMAL)
                    .payload(largePayload)
                    .build()
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payload 크기가 제한을 초과");
        }

        @Test
        @DisplayName("1MB 이하 페이로드는 정상 처리")
        void acceptPayloadWithinMaxSize() {
            // given
            byte[] validPayload = new byte[MessageEnvelope.MAX_PAYLOAD_SIZE]; // 정확히 1MB

            // when
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT)
                .source("device-001")
                .priority(EventPriority.NORMAL)
                .payload(validPayload)
                .build();

            // then
            assertThat(envelope).isNotNull();
            assertThat(envelope.getPayload()).hasSize(MessageEnvelope.MAX_PAYLOAD_SIZE);
        }
    }

    @Nested
    @DisplayName("응답 메시지 생성 테스트")
    class CreateResponseTest {

        @Test
        @DisplayName("원본 메시지로부터 응답 메시지 생성")
        void createResponseFromOriginalMessage() {
            // given
            MessageEnvelope original = MessageEnvelope.builder()
                .messageId("msg-001")
                .messageType(MessageType.QUERY)
                .protocol(Protocol.REST)
                .source("client-001")
                .target("server-001")
                .priority(EventPriority.NORMAL)
                .payload("query".getBytes(StandardCharsets.UTF_8))
                .build();

            byte[] responsePayload = "response".getBytes(StandardCharsets.UTF_8);

            // when
            MessageEnvelope response = original.createResponse(responsePayload);

            // then
            assertThat(response.getMessageType()).isEqualTo(MessageType.RESPONSE);
            assertThat(response.getCorrelationId()).isEqualTo(original.getMessageId());
            assertThat(response.getSource()).isEqualTo(original.getTarget());
            assertThat(response.getTarget()).isEqualTo(original.getSource());
            assertThat(response.getProtocol()).isEqualTo(original.getProtocol());
            assertThat(response.getPriority()).isEqualTo(original.getPriority());
            assertThat(response.getPayload()).isEqualTo(responsePayload);
        }
    }

    @Nested
    @DisplayName("유틸리티 메서드 테스트")
    class UtilityMethodTest {

        @Test
        @DisplayName("페이로드를 문자열로 변환")
        void getPayloadAsString() {
            // given
            String text = "Hello, World!";
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT)
                .source("device-001")
                .priority(EventPriority.NORMAL)
                .payload(text.getBytes(StandardCharsets.UTF_8))
                .build();

            // when
            String result = envelope.getPayloadAsString();

            // then
            assertThat(result).isEqualTo(text);
        }

        @Test
        @DisplayName("CRITICAL 우선순위는 Hot Path")
        void criticalPriorityIsHotPath() {
            // given
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT)
                .source("device-001")
                .priority(EventPriority.CRITICAL)
                .payload("emergency".getBytes(StandardCharsets.UTF_8))
                .build();

            // when & then
            assertThat(envelope.isHotPath()).isTrue();
            assertThat(envelope.isColdPath()).isFalse();
        }

        @Test
        @DisplayName("HIGH 우선순위는 Hot Path")
        void highPriorityIsHotPath() {
            // given
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT)
                .source("device-001")
                .priority(EventPriority.HIGH)
                .payload("urgent".getBytes(StandardCharsets.UTF_8))
                .build();

            // when & then
            assertThat(envelope.isHotPath()).isTrue();
            assertThat(envelope.isColdPath()).isFalse();
        }

        @Test
        @DisplayName("LOW 우선순위는 Cold Path")
        void lowPriorityIsColdPath() {
            // given
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT)
                .source("device-001")
                .priority(EventPriority.LOW)
                .payload("log".getBytes(StandardCharsets.UTF_8))
                .build();

            // when & then
            assertThat(envelope.isHotPath()).isFalse();
            assertThat(envelope.isColdPath()).isTrue();
        }

        @Test
        @DisplayName("NORMAL 우선순위는 Hot/Cold Path 모두 아님 (Warm Path)")
        void normalPriorityIsWarmPath() {
            // given
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT)
                .source("device-001")
                .priority(EventPriority.NORMAL)
                .payload("data".getBytes(StandardCharsets.UTF_8))
                .build();

            // when & then
            assertThat(envelope.isHotPath()).isFalse();
            assertThat(envelope.isColdPath()).isFalse();
        }
    }

    @Nested
    @DisplayName("다양한 프로토콜 테스트")
    class ProtocolTest {

        @Test
        @DisplayName("MQTT 프로토콜 메시지 생성")
        void createMqttMessage() {
            // when
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT)
                .source("safetykit-001")
                .priority(EventPriority.HIGH)
                .payload("sensor data".getBytes(StandardCharsets.UTF_8))
                .build();

            // then
            assertThat(envelope.getProtocol()).isEqualTo(Protocol.MQTT);
        }

        @Test
        @DisplayName("WebSocket 프로토콜 메시지 생성")
        void createWebSocketMessage() {
            // when
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.COMMAND)
                .protocol(Protocol.WEBSOCKET)
                .source("dashboard-001")
                .target("robot-001")
                .priority(EventPriority.NORMAL)
                .payload("move command".getBytes(StandardCharsets.UTF_8))
                .build();

            // then
            assertThat(envelope.getProtocol()).isEqualTo(Protocol.WEBSOCKET);
        }

        @Test
        @DisplayName("Simulator 프로토콜 메시지 생성")
        void createSimulatorMessage() {
            // when
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.SIMULATOR)
                .source("virtual-robot-001")
                .priority(EventPriority.NORMAL)
                .payload("sim data".getBytes(StandardCharsets.UTF_8))
                .build();

            // then
            assertThat(envelope.getProtocol()).isEqualTo(Protocol.SIMULATOR);
        }
    }
}
