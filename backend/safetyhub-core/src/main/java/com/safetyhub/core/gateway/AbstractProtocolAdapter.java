package com.safetyhub.core.gateway;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 프로토콜 어댑터 추상 클래스
 *
 * ProtocolAdapter 구현을 위한 기본 기능 제공
 * - 공통 검증 로직
 * - 에러 처리
 * - 로깅
 *
 * 보안 기능:
 * - 입력 검증 (null 체크)
 * - 페이로드 크기 검증
 * - 에러 로깅 (민감정보 제외)
 *
 * @param <T> 프로토콜별 메시지 타입
 */
@Slf4j
public abstract class AbstractProtocolAdapter<T> implements ProtocolAdapter<T> {

    private final Protocol protocol;

    /**
     * 생성자
     *
     * @param protocol 지원하는 프로토콜 타입
     */
    protected AbstractProtocolAdapter(Protocol protocol) {
        this.protocol = Objects.requireNonNull(protocol, "protocol은 필수입니다");
    }

    @Override
    public final MessageEnvelope toEnvelope(T message) {
        // 입력 검증
        validateMessage(message);

        try {
            log.debug("메시지 변환 시작: {} -> MessageEnvelope", protocol);
            MessageEnvelope envelope = doToEnvelope(message);

            // 변환 결과 검증
            Objects.requireNonNull(envelope, "변환된 envelope이 null입니다");

            log.debug("메시지 변환 완료: messageId={}", envelope.getMessageId());
            return envelope;

        } catch (MessageConversionException e) {
            // 이미 MessageConversionException인 경우 그대로 전파
            throw e;
        } catch (Exception e) {
            // 그 외 예외는 MessageConversionException으로 래핑
            log.error("메시지 변환 중 예외 발생: protocol={}", protocol, e);
            throw new MessageConversionException(protocol,
                "메시지 변환 중 예외가 발생했습니다", e);
        }
    }

    @Override
    public final T fromEnvelope(MessageEnvelope envelope) {
        // 입력 검증
        validateEnvelope(envelope);

        // 프로토콜 일치 검증
        if (envelope.getProtocol() != protocol) {
            throw new MessageConversionException(protocol,
                String.format("프로토콜이 일치하지 않습니다. 기대값: %s, 실제값: %s",
                    protocol, envelope.getProtocol()));
        }

        try {
            log.debug("Envelope 변환 시작: MessageEnvelope -> {}", protocol);
            T message = doFromEnvelope(envelope);

            // 변환 결과 검증
            Objects.requireNonNull(message, "변환된 message가 null입니다");

            log.debug("Envelope 변환 완료: messageId={}", envelope.getMessageId());
            return message;

        } catch (MessageConversionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Envelope 변환 중 예외 발생: protocol={}", protocol, e);
            throw new MessageConversionException(protocol,
                "Envelope 변환 중 예외가 발생했습니다", e);
        }
    }

    @Override
    public final Protocol getSupportedProtocol() {
        return protocol;
    }

    @Override
    public boolean supports(T message) {
        // 기본 구현: null이 아니면 지원
        return message != null;
    }

    /**
     * 실제 변환 로직 (구현체에서 오버라이드)
     *
     * @param message 프로토콜별 메시지
     * @return MessageEnvelope
     */
    protected abstract MessageEnvelope doToEnvelope(T message);

    /**
     * 실제 역변환 로직 (구현체에서 오버라이드)
     *
     * @param envelope MessageEnvelope
     * @return 프로토콜별 메시지
     */
    protected abstract T doFromEnvelope(MessageEnvelope envelope);

    /**
     * 메시지 유효성 검증
     *
     * @param message 검증할 메시지
     * @throws IllegalArgumentException 메시지가 null이거나 유효하지 않은 경우
     */
    protected void validateMessage(T message) {
        if (message == null) {
            throw new IllegalArgumentException("message는 null일 수 없습니다");
        }

        if (!supports(message)) {
            throw new MessageConversionException(protocol,
                "지원하지 않는 메시지 타입입니다: " + message.getClass().getName());
        }
    }

    /**
     * Envelope 유효성 검증
     *
     * @param envelope 검증할 envelope
     * @throws IllegalArgumentException envelope이 null이거나 유효하지 않은 경우
     */
    protected void validateEnvelope(MessageEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope는 null일 수 없습니다");
    }

    /**
     * 페이로드 크기 검증
     *
     * @param payload 검증할 페이로드
     * @throws MessageConversionException 페이로드 크기가 제한을 초과한 경우
     */
    protected void validatePayloadSize(byte[] payload) {
        if (payload != null && payload.length > MessageEnvelope.MAX_PAYLOAD_SIZE) {
            throw new MessageConversionException(protocol,
                String.format("페이로드 크기가 제한을 초과했습니다. (현재: %d, 최대: %d)",
                    payload.length, MessageEnvelope.MAX_PAYLOAD_SIZE));
        }
    }
}
