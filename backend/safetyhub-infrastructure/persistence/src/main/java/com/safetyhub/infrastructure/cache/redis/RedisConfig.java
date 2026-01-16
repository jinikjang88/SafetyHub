package com.safetyhub.infrastructure.cache.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정
 *
 * Spring Data Redis 설정
 *
 * 설정 원칙:
 * - Lettuce 사용 (비동기, 스레드 안전)
 * - String 직렬화 (키, 값 모두)
 * - Connection Pool 설정
 *
 * 주요 설정:
 * - host: Redis 호스트
 * - port: Redis 포트
 * - password: Redis 비밀번호 (선택)
 * - database: Redis DB 번호
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    /**
     * Redis Connection Factory
     *
     * Lettuce를 사용한 연결 팩토리
     * - 비동기 I/O
     * - 스레드 안전
     * - Connection Pool
     *
     * @return LettuceConnectionFactory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);

        // 비밀번호가 설정된 경우
        if (password != null && !password.trim().isEmpty()) {
            config.setPassword(password);
        }

        return new LettuceConnectionFactory(config);
    }

    /**
     * Redis Template
     *
     * String 키, String 값 사용
     * - 키: String (CacheKey 유틸리티 사용)
     * - 값: JSON 문자열 (ObjectMapper 사용)
     *
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // 키 직렬화: String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 값 직렬화: String (JSON)
        template.setValueSerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * ObjectMapper Bean
     *
     * JSON 직렬화/역직렬화에 사용
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
