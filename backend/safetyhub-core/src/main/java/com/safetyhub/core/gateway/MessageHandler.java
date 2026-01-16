package com.safetyhub.core.gateway;

/**
 * 메시지 핸들러 인터페이스
 *
 * Hot/Warm/Cold Path에서 메시지를 처리하는 핸들러
 * - 단일 책임 원칙: 각 핸들러는 하나의 Path만 담당
 * - 전략 패턴: Path별 처리 전략을 교체 가능
 *
 * @see MessageRouter
 * @see MessageEnvelope
 */
@FunctionalInterface
public interface MessageHandler {

    /**
     * 메시지 처리
     *
     * @param envelope 처리할 메시지
     * @throws MessageHandlingException 메시지 처리 실패 시
     */
    void handle(MessageEnvelope envelope);
}
