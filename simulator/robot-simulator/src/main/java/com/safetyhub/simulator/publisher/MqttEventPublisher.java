package com.safetyhub.simulator.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.safetyhub.simulator.event.SimulatorEvent;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQTT 이벤트 퍼블리셔
 */
public class MqttEventPublisher implements EventPublisher {
    private static final Logger log = LoggerFactory.getLogger(MqttEventPublisher.class);

    private final String brokerUrl;
    private final String clientId;
    private final String topicPrefix;
    private final ObjectMapper objectMapper;
    private MqttClient mqttClient;
    private volatile boolean connected = false;

    public MqttEventPublisher(String brokerUrl, String clientId, String topicPrefix) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topicPrefix = topicPrefix;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void connect() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT connection lost", cause);
                    connected = false;
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {}

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            mqttClient.connect(options);
            connected = true;
            log.info("Connected to MQTT broker: {}", brokerUrl);
        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker", e);
        }
    }

    @Override
    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                connected = false;
                log.info("Disconnected from MQTT broker");
            } catch (MqttException e) {
                log.error("Error disconnecting from MQTT broker", e);
            }
        }
    }

    @Override
    public void publish(SimulatorEvent event) {
        if (!connected || mqttClient == null) {
            return;
        }

        try {
            String topic = buildTopic(event);
            String payload = objectMapper.writeValueAsString(event);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(getQos(event.getPriority()));
            message.setRetained(false);
            mqttClient.publish(topic, message);
        } catch (Exception e) {
            log.error("Failed to publish event", e);
        }
    }

    private String buildTopic(SimulatorEvent event) {
        return String.format("%s/%s/%s", topicPrefix, event.getType().toLowerCase(), event.getRobotId());
    }

    private int getQos(SimulatorEvent.EventPriority priority) {
        return switch (priority) {
            case CRITICAL -> 2;
            case HIGH -> 1;
            default -> 0;
        };
    }

    @Override
    public boolean isConnected() {
        return connected && mqttClient != null && mqttClient.isConnected();
    }
}
