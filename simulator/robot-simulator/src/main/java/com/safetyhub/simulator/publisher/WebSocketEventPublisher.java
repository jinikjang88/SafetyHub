package com.safetyhub.simulator.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.safetyhub.simulator.event.SimulatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * WebSocket (STOMP) 이벤트 퍼블리셔
 */
public class WebSocketEventPublisher implements EventPublisher {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final String destinationPrefix;
    private final ObjectMapper objectMapper;
    private volatile boolean connected = false;

    public WebSocketEventPublisher(SimpMessagingTemplate messagingTemplate, String destinationPrefix) {
        this.messagingTemplate = messagingTemplate;
        this.destinationPrefix = destinationPrefix;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void connect() {
        // WebSocket은 Spring에서 자동 관리
        connected = true;
        log.info("WebSocket publisher ready");
    }

    @Override
    public void disconnect() {
        connected = false;
        log.info("WebSocket publisher disconnected");
    }

    @Override
    public void publish(SimulatorEvent event) {
        if (!connected || messagingTemplate == null) {
            return;
        }

        try {
            String destination = buildDestination(event);
            messagingTemplate.convertAndSend(destination, event);

            // 긴급 이벤트는 추가 브로드캐스트
            if (event.getPriority() == SimulatorEvent.EventPriority.CRITICAL) {
                messagingTemplate.convertAndSend(destinationPrefix + "/emergency", event);
            }
        } catch (Exception e) {
            log.error("Failed to publish event via WebSocket", e);
        }
    }

    private String buildDestination(SimulatorEvent event) {
        return switch (event.getType()) {
            case "LOCATION_UPDATE" -> destinationPrefix + "/location/" + event.getRobotId();
            case "HEARTBEAT" -> destinationPrefix + "/heartbeat/" + event.getRobotId();
            case "EMERGENCY" -> destinationPrefix + "/emergency/" + event.getRobotId();
            case "STATE_CHANGE" -> destinationPrefix + "/state/" + event.getRobotId();
            default -> destinationPrefix + "/events";
        };
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
