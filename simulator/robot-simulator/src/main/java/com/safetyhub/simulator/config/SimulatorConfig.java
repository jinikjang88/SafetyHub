package com.safetyhub.simulator.config;

import com.safetyhub.simulator.event.EventGenerator;
import com.safetyhub.simulator.publisher.CompositeEventPublisher;
import com.safetyhub.simulator.publisher.KafkaEventPublisher;
import com.safetyhub.simulator.publisher.MqttEventPublisher;
import com.safetyhub.simulator.scenario.ScenarioEngine;
import com.safetyhub.simulator.scenario.ScenarioLoader;
import com.safetyhub.simulator.world.VirtualWorld;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 시뮬레이터 설정
 */
@Configuration
public class SimulatorConfig {

    @Value("${simulator.world.width:100}")
    private int worldWidth;

    @Value("${simulator.world.height:100}")
    private int worldHeight;

    @Value("${simulator.world.name:Virtual Factory}")
    private String worldName;

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String mqttBrokerUrl;

    @Value("${mqtt.client-id:robot-simulator}")
    private String mqttClientId;

    @Value("${mqtt.topic-prefix:safetyhub/simulator}")
    private String mqttTopicPrefix;

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String kafkaBootstrapServers;

    @Value("${kafka.topic-prefix:safetyhub-simulator}")
    private String kafkaTopicPrefix;

    @Bean
    public VirtualWorld virtualWorld() {
        return VirtualWorld.createStandardFactory("factory-1", worldName);
    }

    @Bean
    public EventGenerator eventGenerator() {
        return new EventGenerator();
    }

    @Bean
    public ScenarioLoader scenarioLoader() {
        return new ScenarioLoader();
    }

    @Bean
    public ScenarioEngine scenarioEngine(VirtualWorld world, EventGenerator eventGenerator) {
        return new ScenarioEngine(world, eventGenerator);
    }

    @Bean
    public MqttEventPublisher mqttEventPublisher() {
        return new MqttEventPublisher(mqttBrokerUrl, mqttClientId, mqttTopicPrefix);
    }

    @Bean
    public KafkaEventPublisher kafkaEventPublisher() {
        return new KafkaEventPublisher(kafkaBootstrapServers, kafkaTopicPrefix);
    }

    @Bean
    public CompositeEventPublisher compositeEventPublisher(
            MqttEventPublisher mqttPublisher,
            KafkaEventPublisher kafkaPublisher) {
        CompositeEventPublisher composite = new CompositeEventPublisher();
        composite.addPublisher(mqttPublisher);
        composite.addPublisher(kafkaPublisher);
        return composite;
    }
}
