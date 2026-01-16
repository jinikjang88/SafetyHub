package com.safetyhub.infrastructure.cache.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetyhub.infrastructure.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 캐시 서비스 구현체
 *
 * Spring Data Redis를 사용한 캐시 구현
 *
 * 특징:
 * - JSON 직렬화/역직렬화
 * - TTL 지원
 * - 패턴 기반 삭제
 * - 에러 처리
 *
 * 보안:
 * - 입력 검증
 * - 직렬화 에러 처리
 * - 민감정보 로깅 방지
 *
 * 성능:
 * - 파이프라인 지원 (TODO)
 * - 배치 작업 지원 (TODO)
 */
@Slf4j
@Service
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 생성자 주입
     *
     * @param redisTemplate Redis 템플릿
     * @param objectMapper JSON 직렬화
     */
    public RedisCacheService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {

        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate은 필수입니다");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper는 필수입니다");
    }

    @Override
    public void put(String key, Object value) {
        validateKey(key);
        Objects.requireNonNull(value, "value는 null일 수 없습니다");

        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
            log.trace("캐시 저장: key={}", key);

        } catch (JsonProcessingException e) {
            log.error("캐시 직렬화 실패: key={}", key, e);
            throw new CacheSerializationException("캐시 직렬화 실패", e);
        }
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        validateKey(key);
        Objects.requireNonNull(value, "value는 null일 수 없습니다");
        Objects.requireNonNull(ttl, "ttl은 null일 수 없습니다");

        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl.getSeconds(), TimeUnit.SECONDS);
            log.trace("캐시 저장 (TTL={}초): key={}", ttl.getSeconds(), key);

        } catch (JsonProcessingException e) {
            log.error("캐시 직렬화 실패: key={}", key, e);
            throw new CacheSerializationException("캐시 직렬화 실패", e);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        validateKey(key);
        Objects.requireNonNull(type, "type은 null일 수 없습니다");

        try {
            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                log.trace("캐시 미스: key={}", key);
                return Optional.empty();
            }

            T value = objectMapper.readValue(json, type);
            log.trace("캐시 히트: key={}", key);
            return Optional.of(value);

        } catch (JsonProcessingException e) {
            log.error("캐시 역직렬화 실패: key={}", key, e);
            // 역직렬화 실패 시 캐시 삭제 (손상된 데이터)
            delete(key);
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String key) {
        validateKey(key);

        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void delete(String key) {
        validateKey(key);

        redisTemplate.delete(key);
        log.trace("캐시 삭제: key={}", key);
    }

    @Override
    public void deleteByPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern은 null일 수 없습니다");

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("캐시 패턴 삭제: pattern={}, count={}", pattern, keys.size());
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        Objects.requireNonNull(pattern, "pattern은 null일 수 없습니다");

        Set<String> keys = redisTemplate.keys(pattern);
        log.trace("캐시 키 조회: pattern={}, count={}", pattern,
            keys != null ? keys.size() : 0);
        return keys;
    }

    @Override
    public void expire(String key, Duration ttl) {
        validateKey(key);
        Objects.requireNonNull(ttl, "ttl은 null일 수 없습니다");

        redisTemplate.expire(key, ttl.getSeconds(), TimeUnit.SECONDS);
        log.trace("캐시 TTL 설정: key={}, ttl={}초", key, ttl.getSeconds());
    }

    @Override
    public Long getExpire(String key) {
        validateKey(key);

        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        log.trace("캐시 TTL 조회: key={}, expire={}초", key, expire);
        return expire;
    }

    /**
     * 키 유효성 검증
     *
     * @param key 검증할 키
     */
    private void validateKey(String key) {
        Objects.requireNonNull(key, "key는 null일 수 없습니다");

        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key는 빈 문자열일 수 없습니다");
        }
    }
}
