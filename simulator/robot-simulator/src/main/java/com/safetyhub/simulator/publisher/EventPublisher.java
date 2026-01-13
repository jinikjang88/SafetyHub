package com.safetyhub.simulator.publisher;

import com.safetyhub.simulator.event.SimulatorEvent;

/**
 * 이벤트 퍼블리셔 인터페이스
 */
public interface EventPublisher {
    void publish(SimulatorEvent event);
    void connect();
    void disconnect();
    boolean isConnected();
}
