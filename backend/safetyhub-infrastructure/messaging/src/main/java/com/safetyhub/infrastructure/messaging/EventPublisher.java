package com.safetyhub.infrastructure.messaging;

import com.safetyhub.core.gateway.MessageEnvelope;

import java.util.concurrent.CompletableFuture;

/**
 * 이벤트 발행자 인터페이스
 *
 * Port (포트) 인터페이스:
 * - Core 도메인에서 정의
 * - Infrastructure에서 구현
 * - 단방향 의존성: Core ← Infrastructure
 *
 * 역할:
 * - 도메인 이벤트를 메시징 시스템에 발행
 * - Kafka, RabbitMQ 등 다양한 구현 지원
 *
 * 설계 원칙:
 * - 기술 독립적 인터페이스
 * - 비동기 발행 (CompletableFuture)
 * - 에러 처리
 *
 * @see MessageEnvelope
 * @see EventTopic
 */
public interface EventPublisher {

    /**
     * 이벤트 발행 (토픽 자동 결정)
     *
     * MessageEnvelope의 메타데이터를 기반으로 적절한 토픽 선택
     *
     * @param envelope 발행할 메시지
     * @return 발행 결과 (비동기)
     * @throws EventPublishException 발행 실패 시
     */
    CompletableFuture<Void> publish(MessageEnvelope envelope);

    /**
     * 이벤트 발행 (토픽 명시)
     *
     * @param topic 토픽
     * @param envelope 발행할 메시지
     * @return 발행 결과 (비동기)
     * @throws EventPublishException 발행 실패 시
     */
    CompletableFuture<Void> publish(EventTopic topic, MessageEnvelope envelope);

    /**
     * 이벤트 발행 (파티션 키 지정)
     *
     * 동일한 키를 가진 메시지는 같은 파티션으로 전송되어 순서 보장
     *
     * @param topic 토픽
     * @param partitionKey 파티션 키 (보통 소스 ID)
     * @param envelope 발행할 메시지
     * @return 발행 결과 (비동기)
     * @throws EventPublishException 발행 실패 시
     */
    CompletableFuture<Void> publish(EventTopic topic, String partitionKey, MessageEnvelope envelope);
}
