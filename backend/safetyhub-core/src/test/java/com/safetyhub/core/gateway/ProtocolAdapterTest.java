package com.safetyhub.core.gateway;

import com.safetyhub.core.event.EventPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * ProtocolAdapter 인터페이스 및 AbstractProtocolAdapter 테스트
 *
 * 테스트용 어댑터 구현을 통한 검증
 */
@DisplayName("ProtocolAdapter 테스트")
class ProtocolAdapterTest {

    /**
     * 테스트용 간단한 메시지 클래스
     */
    static class TestMessage {
        String content;
        String source;
        EventPriority priority;

        TestMessage(String content, String source, EventPriority priority) {
            this.content = content;
            this.source = source;
            this.priority = priority;
        }
    }

    /**
     * 테스트용 어댑터 구현
     */
    static class TestProtocolAdapter extends AbstractProtocolAdapter<TestMessage> {

        TestProtocolAdapter() {
            super(Protocol.SIMULATOR);
        }

        @Override
        protected MessageEnvelope doToEnvelope(TestMessage message) {
            return MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.SIMULATOR)
                .source(message.source)
                .priority(message.priority)
                .payload(message.content.getBytes(StandardCharsets.UTF_8))
                .build();
        }

        @Override
        protected TestMessage doFromEnvelope(MessageEnvelope envelope) {
            String content = new String(envelope.getPayload(), StandardCharsets.UTF_8);
            return new TestMessage(content, envelope.getSource(), envelope.getPriority());
        }
    }

    private TestProtocolAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TestProtocolAdapter();
    }

    @Nested
    @DisplayName("toEnvelope 테스트")
    class ToEnvelopeTest {

        @Test
        @DisplayName("정상 메시지 변환")
        void convertValidMessage() {
            // given
            TestMessage message = new TestMessage(
                "test content",
                "test-source",
                EventPriority.NORMAL
            );

            // when
            MessageEnvelope envelope = adapter.toEnvelope(message);

            // then
            assertThat(envelope).isNotNull();
            assertThat(envelope.getProtocol()).isEqualTo(Protocol.SIMULATOR);
            assertThat(envelope.getSource()).isEqualTo("test-source");
            assertThat(envelope.getPriority()).isEqualTo(EventPriority.NORMAL);
            assertThat(envelope.getPayloadAsString()).isEqualTo("test content");
        }

        @Test
        @DisplayName("null 메시지는 예외 발생")
        void throwExceptionWhenMessageIsNull() {
            // when & then
            assertThatThrownBy(() -> adapter.toEnvelope(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("변환 중 예외 발생 시 MessageConversionException으로 래핑")
        void wrapExceptionAsMessageConversionException() {
            // given
            TestProtocolAdapter faultyAdapter = new TestProtocolAdapter() {
                @Override
                protected MessageEnvelope doToEnvelope(TestMessage message) {
                    throw new RuntimeException("변환 실패");
                }
            };

            TestMessage message = new TestMessage("test", "source", EventPriority.NORMAL);

            // when & then
            assertThatThrownBy(() -> faultyAdapter.toEnvelope(message))
                .isInstanceOf(MessageConversionException.class)
                .hasMessageContaining("SIMULATOR")
                .hasMessageContaining("예외가 발생했습니다");
        }
    }

    @Nested
    @DisplayName("fromEnvelope 테스트")
    class FromEnvelopeTest {

        @Test
        @DisplayName("정상 Envelope 역변환")
        void convertValidEnvelope() {
            // given
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.SIMULATOR)
                .source("test-source")
                .priority(EventPriority.HIGH)
                .payload("test content".getBytes(StandardCharsets.UTF_8))
                .build();

            // when
            TestMessage message = adapter.fromEnvelope(envelope);

            // then
            assertThat(message).isNotNull();
            assertThat(message.content).isEqualTo("test content");
            assertThat(message.source).isEqualTo("test-source");
            assertThat(message.priority).isEqualTo(EventPriority.HIGH);
        }

        @Test
        @DisplayName("null Envelope은 예외 발생")
        void throwExceptionWhenEnvelopeIsNull() {
            // when & then
            assertThatThrownBy(() -> adapter.fromEnvelope(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("프로토콜이 일치하지 않으면 예외 발생")
        void throwExceptionWhenProtocolMismatch() {
            // given
            MessageEnvelope envelope = MessageEnvelope.builder()
                .messageType(MessageType.EVENT)
                .protocol(Protocol.MQTT) // SIMULATOR가 아님
                .source("test-source")
                .priority(EventPriority.NORMAL)
                .payload("test".getBytes(StandardCharsets.UTF_8))
                .build();

            // when & then
            assertThatThrownBy(() -> adapter.fromEnvelope(envelope))
                .isInstanceOf(MessageConversionException.class)
                .hasMessageContaining("프로토콜이 일치하지 않습니다")
                .hasMessageContaining("SIMULATOR")
                .hasMessageContaining("MQTT");
        }
    }

    @Nested
    @DisplayName("getSupportedProtocol 테스트")
    class GetSupportedProtocolTest {

        @Test
        @DisplayName("지원하는 프로토콜 반환")
        void returnSupportedProtocol() {
            // when
            Protocol protocol = adapter.getSupportedProtocol();

            // then
            assertThat(protocol).isEqualTo(Protocol.SIMULATOR);
        }
    }

    @Nested
    @DisplayName("supports 테스트")
    class SupportsTest {

        @Test
        @DisplayName("유효한 메시지는 지원")
        void supportsValidMessage() {
            // given
            TestMessage message = new TestMessage("test", "source", EventPriority.NORMAL);

            // when
            boolean result = adapter.supports(message);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null 메시지는 지원하지 않음")
        void doesNotSupportNullMessage() {
            // when
            boolean result = adapter.supports(null);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("페이로드 크기 검증 테스트")
    class PayloadSizeValidationTest {

        @Test
        @DisplayName("페이로드 크기가 제한을 초과하면 예외 발생")
        void throwExceptionWhenPayloadExceedsLimit() {
            // given
            byte[] largePayload = new byte[MessageEnvelope.MAX_PAYLOAD_SIZE + 1];

            TestProtocolAdapter validatingAdapter = new TestProtocolAdapter() {
                @Override
                protected MessageEnvelope doToEnvelope(TestMessage message) {
                    validatePayloadSize(largePayload); // 크기 검증
                    return super.doToEnvelope(message);
                }
            };

            TestMessage message = new TestMessage("test", "source", EventPriority.NORMAL);

            // when & then
            assertThatThrownBy(() -> validatingAdapter.toEnvelope(message))
                .isInstanceOf(MessageConversionException.class)
                .hasMessageContaining("페이로드 크기가 제한을 초과");
        }

        @Test
        @DisplayName("페이로드 크기가 제한 내이면 정상 처리")
        void acceptPayloadWithinLimit() {
            // given
            byte[] validPayload = new byte[1024]; // 1KB

            TestProtocolAdapter validatingAdapter = new TestProtocolAdapter() {
                @Override
                protected MessageEnvelope doToEnvelope(TestMessage message) {
                    validatePayloadSize(validPayload); // 크기 검증
                    return super.doToEnvelope(message);
                }
            };

            TestMessage message = new TestMessage("test", "source", EventPriority.NORMAL);

            // when & then
            assertThatNoException().isThrownBy(() -> validatingAdapter.toEnvelope(message));
        }
    }

    @Nested
    @DisplayName("MessageConversionException 테스트")
    class MessageConversionExceptionTest {

        @Test
        @DisplayName("기본 생성자")
        void createWithMessage() {
            // when
            MessageConversionException exception = new MessageConversionException("에러");

            // then
            assertThat(exception.getMessage()).isEqualTo("에러");
        }

        @Test
        @DisplayName("원인 예외 포함")
        void createWithCause() {
            // given
            RuntimeException cause = new RuntimeException("원인");

            // when
            MessageConversionException exception =
                new MessageConversionException("에러", cause);

            // then
            assertThat(exception.getMessage()).isEqualTo("에러");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("프로토콜 정보 포함")
        void createWithProtocol() {
            // when
            MessageConversionException exception =
                new MessageConversionException(Protocol.MQTT, "에러");

            // then
            assertThat(exception.getMessage()).contains("MQTT");
            assertThat(exception.getMessage()).contains("에러");
        }

        @Test
        @DisplayName("프로토콜 정보와 원인 예외 포함")
        void createWithProtocolAndCause() {
            // given
            RuntimeException cause = new RuntimeException("원인");

            // when
            MessageConversionException exception =
                new MessageConversionException(Protocol.WEBSOCKET, "에러", cause);

            // then
            assertThat(exception.getMessage()).contains("WEBSOCKET");
            assertThat(exception.getMessage()).contains("에러");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }
}
