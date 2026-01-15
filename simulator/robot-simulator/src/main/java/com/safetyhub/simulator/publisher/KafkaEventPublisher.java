package com.safetyhub.simulator.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.safetyhub.simulator.event.SimulatorEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Kafka 이벤트 퍼블리셔
 */
public class KafkaEventPublisher implements EventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final String bootstrapServers;
    private final String topicPrefix;
    private final ObjectMapper objectMapper;
    private KafkaProducer<String, String> producer;
    private volatile boolean connected = false;

    public KafkaEventPublisher(String bootstrapServers, String topicPrefix) {
        this.bootstrapServers = bootstrapServers;
        this.topicPrefix = topicPrefix;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void connect() {
        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            props.put(ProducerConfig.RETRIES_CONFIG, 3);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

            producer = new KafkaProducer<>(props);
            connected = true;
            log.info("Connected to Kafka: {}", bootstrapServers);
        } catch (Exception e) {
            log.error("Failed to connect to Kafka", e);
        }
    }

    @Override
    public void disconnect() {
        if (producer != null) {
            producer.flush();
            producer.close();
            connected = false;
            log.info("Disconnected from Kafka");
        }
    }

    @Override
    public void publish(SimulatorEvent event) {
        if (!connected || producer == null) {
            return;
        }

        try {
            String topic = buildTopic(event);
            String key = event.getRobotId();
            String value = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to send event to Kafka", exception);
                }
            });
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka", e);
        }
    }

    private String buildTopic(SimulatorEvent event) {
        return switch (event.getPriority()) {
            case CRITICAL -> topicPrefix + "-emergency";
            case HIGH -> topicPrefix + "-high";
            default -> topicPrefix + "-normal";
        };
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
