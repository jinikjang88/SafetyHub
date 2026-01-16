package com.safetyhub.infrastructure.messaging.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer 설정
 *
 * Spring Kafka 설정
 *
 * 설정 원칙:
 * - 성능: 배치 전송, 압축
 * - 안정성: acks=all, 재시도
 * - 보안: SSL/SASL (프로덕션 환경)
 *
 * 주요 설정:
 * - bootstrap.servers: Kafka 브로커 주소
 * - key.serializer: String
 * - value.serializer: String (JSON)
 * - acks: all (모든 복제본 확인)
 * - retries: 3 (재시도 횟수)
 * - linger.ms: 10 (배치 대기 시간)
 * - compression.type: lz4 (압축)
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:3}")
    private Integer retries;

    @Value("${spring.kafka.producer.linger-ms:10}")
    private Integer lingerMs;

    @Value("${spring.kafka.producer.compression-type:lz4}")
    private String compressionType;

    /**
     * Kafka Producer Factory
     *
     * @return ProducerFactory
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // 브로커 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 직렬화 설정
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 안정성 설정
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 멱등성 보장

        // 성능 설정
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs); // 배치 대기 시간
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 배치 크기
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType); // 압축
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB 버퍼

        // 타임아웃 설정
        configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000); // 5초
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000); // 30초

        // 메타데이터
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "safetyhub-producer");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka Template
     *
     * @return KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
