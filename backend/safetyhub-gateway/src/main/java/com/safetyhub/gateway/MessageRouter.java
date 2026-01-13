package com.safetyhub.gateway;

import com.safetyhub.core.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 메시지 라우터
 * 이벤트 우선순위에 따라 Hot/Warm/Cold Path로 라우팅
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRouter {

    /**
     * 이벤트 라우팅
     * Hot Path: CRITICAL, HIGH 우선순위 → 즉시 처리
     * Warm Path: NORMAL 우선순위 → 일반 처리
     * Cold Path: LOW 우선순위 → 배치 처리
     */
    public void route(DomainEvent event) {
        switch (event.getPriority()) {
            case CRITICAL -> routeToHotPath(event);
            case HIGH -> routeToHotPath(event);
            case NORMAL -> routeToWarmPath(event);
            case LOW -> routeToColdPath(event);
        }
    }

    /**
     * Hot Path 라우팅
     * - 긴급 상황 (화재, 가스 누출, 낙상 등)
     * - 목표 응답 시간: < 100ms
     */
    private void routeToHotPath(DomainEvent event) {
        log.warn("HOT PATH: {} - Priority: {}",
                event.getEventType(), event.getPriority());
        // TODO: 즉시 처리 큐로 전달
        // TODO: 대시보드 실시간 알림
        // TODO: 필요시 119 신고
    }

    /**
     * Warm Path 라우팅
     * - 일반 상태 업데이트
     * - 목표 응답 시간: < 2s
     */
    private void routeToWarmPath(DomainEvent event) {
        log.debug("WARM PATH: {} - Priority: {}",
                event.getEventType(), event.getPriority());
        // TODO: 일반 처리 큐로 전달
    }

    /**
     * Cold Path 라우팅
     * - 분석용 데이터, 로그 등
     * - 배치 처리 가능
     */
    private void routeToColdPath(DomainEvent event) {
        log.debug("COLD PATH: {} - Priority: {}",
                event.getEventType(), event.getPriority());
        // TODO: 배치 처리 큐로 전달
    }
}
