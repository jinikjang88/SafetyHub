package com.safetyhub.core.port.out;

import com.safetyhub.core.event.DomainEvent;

/**
 * 이벤트 발행 출력 포트
 */
public interface EventPublisher {

    /**
     * 이벤트 발행
     */
    void publish(DomainEvent event);

    /**
     * 특정 토픽으로 이벤트 발행
     */
    void publish(String topic, DomainEvent event);
}
