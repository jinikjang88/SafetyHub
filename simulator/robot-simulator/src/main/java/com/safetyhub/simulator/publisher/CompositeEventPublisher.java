package com.safetyhub.simulator.publisher;

import com.safetyhub.simulator.event.EventGenerator;
import com.safetyhub.simulator.event.SimulatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 복합 이벤트 퍼블리셔
 * 여러 퍼블리셔(MQTT, Kafka, WebSocket)를 동시에 사용
 */
public class CompositeEventPublisher implements EventPublisher, EventGenerator.EventListener {
    private static final Logger log = LoggerFactory.getLogger(CompositeEventPublisher.class);

    private final List<EventPublisher> publishers = new ArrayList<>();

    public void addPublisher(EventPublisher publisher) {
        publishers.add(publisher);
    }

    public void removePublisher(EventPublisher publisher) {
        publishers.remove(publisher);
    }

    @Override
    public void publish(SimulatorEvent event) {
        for (EventPublisher publisher : publishers) {
            try {
                if (publisher.isConnected()) {
                    publisher.publish(event);
                }
            } catch (Exception e) {
                log.error("Publisher failed: {}", publisher.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void connect() {
        for (EventPublisher publisher : publishers) {
            try {
                publisher.connect();
                log.info("Connected: {}", publisher.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("Failed to connect: {}", publisher.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void disconnect() {
        for (EventPublisher publisher : publishers) {
            try {
                publisher.disconnect();
            } catch (Exception e) {
                log.error("Failed to disconnect: {}", publisher.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return publishers.stream().anyMatch(EventPublisher::isConnected);
    }

    @Override
    public void onEvent(SimulatorEvent event) {
        publish(event);
    }

    public List<EventPublisher> getPublishers() {
        return new ArrayList<>(publishers);
    }
}
