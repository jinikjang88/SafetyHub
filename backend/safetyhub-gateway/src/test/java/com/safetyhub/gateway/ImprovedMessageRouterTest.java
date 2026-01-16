package com.safetyhub.gateway;

import com.safetyhub.core.event.EventPriority;
import com.safetyhub.core.gateway.MessageEnvelope;
import com.safetyhub.core.gateway.MessageHandler;
import com.safetyhub.core.gateway.MessageHandlingException;
import com.safetyhub.core.gateway.MessageType;
import com.safetyhub.core.gateway.Protocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ImprovedMessageRouter 테스트
 */
@DisplayName("ImprovedMessageRouter 테스트")
class ImprovedMessageRouterTest {

    private MessageHandler hotPathHandler;
    private MessageHandler warmPathHandler;
    private MessageHandler coldPathHandler;
    private ImprovedMessageRouter router;

    @BeforeEach
    void setUp() {
        hotPathHandler = mock(MessageHandler.class);
        warmPathHandler = mock(MessageHandler.class);
        coldPathHandler = mock(MessageHandler.class);

        router = new ImprovedMessageRouter(
            hotPathHandler,
            warmPathHandler,
            coldPathHandler
        );
    }

    @AfterEach
    void tearDown() {
        router.shutdown();
    }

    @Nested
    @DisplayName("Hot Path 라우팅 테스트")
    class HotPathRoutingTest {

        @Test
        @DisplayName("CRITICAL 우선순위는 Hot Path로 라우팅")
        void routeCriticalMessageToHotPath() {
            // given
            MessageEnvelope envelope = createMessage(EventPriority.CRITICAL);

            // when
            router.route(envelope);

            // then
            verify(hotPathHandler, times(1)).handle(envelope);
            verify(warmPathHandler, never()).handle(any());
            verify(coldPathHandler, never()).handle(any());
        }

        @Test
        @DisplayName("HIGH 우선순위는 Hot Path로 라우팅")
        void routeHighMessageToHotPath() {
            // given
            MessageEnvelope envelope = createMessage(EventPriority.HIGH);

            // when
            router.route(envelope);

            // then
            verify(hotPathHandler, times(1)).handle(envelope);
            verify(warmPathHandler, never()).handle(any());
            verify(coldPathHandler, never()).handle(any());
        }

        @Test
        @DisplayName("Hot Path 처리 실패 시 예외 전파")
        void propagateExceptionWhenHotPathFails() {
            // given
            MessageEnvelope envelope = createMessage(EventPriority.CRITICAL);
            doThrow(new MessageHandlingException("HOT", "처리 실패"))
                .when(hotPathHandler).handle(envelope);

            // when & then
            assertThatThrownBy(() -> router.route(envelope))
                .isInstanceOf(MessageHandlingException.class)
                .hasMessageContaining("HOT");
        }
    }

    @Nested
    @DisplayName("Warm Path 라우팅 테스트")
    class WarmPathRoutingTest {

        @Test
        @DisplayName("NORMAL 우선순위는 Warm Path로 라우팅")
        void routeNormalMessageToWarmPath() {
            // given
            MessageEnvelope envelope = createMessage(EventPriority.NORMAL);

            // when
            router.route(envelope);

            // then
            verify(warmPathHandler, times(1)).handle(envelope);
            verify(hotPathHandler, never()).handle(any());
            verify(coldPathHandler, never()).handle(any());
        }

        @Test
        @DisplayName("Warm Path 처리 실패 시 예외 전파")
        void propagateExceptionWhenWarmPathFails() {
            // given
            MessageEnvelope envelope = createMessage(EventPriority.NORMAL);
            doThrow(new MessageHandlingException("WARM", "처리 실패"))
                .when(warmPathHandler).handle(envelope);

            // when & then
            assertThatThrownBy(() -> router.route(envelope))
                .isInstanceOf(MessageHandlingException.class)
                .hasMessageContaining("WARM");
        }
    }

    @Nested
    @DisplayName("Cold Path 라우팅 테스트")
    class ColdPathRoutingTest {

        @Test
        @DisplayName("LOW 우선순위는 Cold Path로 라우팅 (비동기)")
        void routeLowMessageToColdPath() throws InterruptedException {
            // given
            CountDownLatch latch = new CountDownLatch(1);
            MessageEnvelope envelope = createMessage(EventPriority.LOW);

            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(coldPathHandler).handle(envelope);

            // when
            router.route(envelope);

            // then
            // 비동기 처리이므로 대기
            boolean completed = latch.await(1, TimeUnit.SECONDS);
            assertThat(completed).isTrue();

            verify(coldPathHandler, times(1)).handle(envelope);
            verify(hotPathHandler, never()).handle(any());
            verify(warmPathHandler, never()).handle(any());
        }

        @Test
        @DisplayName("Cold Path 처리 실패 시 예외가 메인 플로우에 영향 없음")
        void coldPathFailureDoesNotAffectMainFlow() throws InterruptedException {
            // given
            CountDownLatch latch = new CountDownLatch(1);
            MessageEnvelope envelope = createMessage(EventPriority.LOW);

            doAnswer(invocation -> {
                latch.countDown();
                throw new RuntimeException("Cold Path 실패");
            }).when(coldPathHandler).handle(envelope);

            // when
            assertThatNoException().isThrownBy(() -> router.route(envelope));

            // then
            // 비동기 처리 대기
            latch.await(1, TimeUnit.SECONDS);

            // 핸들러는 호출되었지만 예외가 메인 플로우에 영향 없음
            verify(coldPathHandler, times(1)).handle(envelope);
        }
    }

    @Nested
    @DisplayName("입력 검증 테스트")
    class InputValidationTest {

        @Test
        @DisplayName("null envelope은 예외 발생")
        void throwExceptionWhenEnvelopeIsNull() {
            // when & then
            assertThatThrownBy(() -> router.route(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("null 핸들러는 생성자에서 예외 발생")
        void throwExceptionWhenHandlerIsNull() {
            // when & then
            assertThatThrownBy(() -> new ImprovedMessageRouter(
                null,
                warmPathHandler,
                coldPathHandler
            ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("hotPathHandler");

            assertThatThrownBy(() -> new ImprovedMessageRouter(
                hotPathHandler,
                null,
                coldPathHandler
            ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("warmPathHandler");

            assertThatThrownBy(() -> new ImprovedMessageRouter(
                hotPathHandler,
                warmPathHandler,
                null
            ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("coldPathHandler");
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("Hot Path 처리는 빠르게 완료되어야 함")
        void hotPathShouldBeProcessedQuickly() {
            // given
            MessageEnvelope envelope = createMessage(EventPriority.CRITICAL);

            // 핸들러가 즉시 반환하도록 설정
            doNothing().when(hotPathHandler).handle(envelope);

            // when
            long startTime = System.currentTimeMillis();
            router.route(envelope);
            long elapsedTime = System.currentTimeMillis() - startTime;

            // then
            // Hot Path는 10ms 이내에 완료되어야 함 (실제로는 1ms 이내)
            assertThat(elapsedTime).isLessThan(10);
        }

        @Test
        @DisplayName("Cold Path는 비동기로 처리되어 메인 스레드를 블록하지 않음")
        void coldPathShouldNotBlockMainThread() {
            // given
            MessageEnvelope envelope = createMessage(EventPriority.LOW);
            AtomicBoolean handlerExecuted = new AtomicBoolean(false);

            // 핸들러가 100ms 동안 작업한다고 가정
            doAnswer(invocation -> {
                Thread.sleep(100);
                handlerExecuted.set(true);
                return null;
            }).when(coldPathHandler).handle(envelope);

            // when
            long startTime = System.currentTimeMillis();
            router.route(envelope);
            long elapsedTime = System.currentTimeMillis() - startTime;

            // then
            // route() 메서드는 즉시 반환되어야 함 (비동기 처리)
            assertThat(elapsedTime).isLessThan(50); // 50ms 이내
        }
    }

    @Nested
    @DisplayName("MessageHandlingException 테스트")
    class MessageHandlingExceptionTest {

        @Test
        @DisplayName("기본 생성자")
        void createWithMessage() {
            // when
            MessageHandlingException exception = new MessageHandlingException("에러");

            // then
            assertThat(exception.getMessage()).isEqualTo("에러");
        }

        @Test
        @DisplayName("원인 예외 포함")
        void createWithCause() {
            // given
            RuntimeException cause = new RuntimeException("원인");

            // when
            MessageHandlingException exception =
                new MessageHandlingException("에러", cause);

            // then
            assertThat(exception.getMessage()).isEqualTo("에러");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Path 정보 포함")
        void createWithPath() {
            // when
            MessageHandlingException exception =
                new MessageHandlingException("HOT", "처리 실패");

            // then
            assertThat(exception.getMessage()).contains("HOT PATH");
            assertThat(exception.getMessage()).contains("처리 실패");
        }

        @Test
        @DisplayName("Path 정보와 원인 예외 포함")
        void createWithPathAndCause() {
            // given
            RuntimeException cause = new RuntimeException("원인");

            // when
            MessageHandlingException exception =
                new MessageHandlingException("WARM", "처리 실패", cause);

            // then
            assertThat(exception.getMessage()).contains("WARM PATH");
            assertThat(exception.getMessage()).contains("처리 실패");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    /**
     * 테스트용 메시지 생성 헬퍼
     */
    private MessageEnvelope createMessage(EventPriority priority) {
        return MessageEnvelope.builder()
            .messageType(MessageType.EVENT)
            .protocol(Protocol.SIMULATOR)
            .source("test-source")
            .priority(priority)
            .payload("test payload".getBytes(StandardCharsets.UTF_8))
            .build();
    }
}
