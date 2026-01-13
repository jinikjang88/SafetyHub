package com.safetyhub.core.event;

import java.time.LocalDateTime;

/**
 * 도메인 이벤트 기본 인터페이스
 */
public interface DomainEvent {

    String getEventId();

    String getEventType();

    LocalDateTime getOccurredAt();

    /**
     * 이벤트 우선순위 (Hot Path 처리용)
     */
    default EventPriority getPriority() {
        return EventPriority.NORMAL;
    }

    enum EventPriority {
        CRITICAL,    // 즉시 처리 (< 100ms)
        HIGH,        // 빠른 처리 (< 500ms)
        NORMAL,      // 일반 처리 (< 2s)
        LOW          // 지연 처리 가능
    }
}
