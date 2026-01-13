package com.safetyhub.adapter.websocket;

import com.safetyhub.core.domain.Device;
import com.safetyhub.core.domain.Emergency;
import com.safetyhub.core.domain.Worker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 이벤트 발행자
 * 실시간 대시보드 업데이트를 위한 메시지 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 장치 상태 업데이트 전송
     */
    public void publishDeviceUpdate(Device device) {
        log.debug("Publishing device update: {}", device.getDeviceId());
        messagingTemplate.convertAndSend("/topic/devices", device);
    }

    /**
     * 작업자 상태 업데이트 전송
     */
    public void publishWorkerUpdate(Worker worker) {
        log.debug("Publishing worker update: {}", worker.getWorkerId());
        messagingTemplate.convertAndSend("/topic/workers", worker);
    }

    /**
     * 긴급 상황 알림 전송
     */
    public void publishEmergencyAlert(Emergency emergency) {
        log.warn("Publishing emergency alert: {} - Level: {}",
                emergency.getEmergencyId(), emergency.getLevel());
        messagingTemplate.convertAndSend("/topic/emergencies", emergency);
    }

    /**
     * 구역 상태 업데이트 전송
     */
    public void publishZoneUpdate(String zoneId, Object zoneData) {
        log.debug("Publishing zone update: {}", zoneId);
        messagingTemplate.convertAndSend("/topic/zones/" + zoneId, zoneData);
    }
}
