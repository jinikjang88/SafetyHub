package com.safetyhub.infrastructure.cache.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetyhub.infrastructure.cache.CacheKey;
import com.safetyhub.infrastructure.cache.CacheTTL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisCacheService 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisCacheService 테스트")
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private RedisCacheService cacheService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cacheService = new RedisCacheService(redisTemplate, objectMapper);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("put 테스트")
    class PutTest {

        @Test
        @DisplayName("객체를 캐시에 저장")
        void putObject() {
            // given
            String key = CacheKey.robotState("robot-001");
            TestData data = new TestData("test", 123);

            // when
            cacheService.put(key, data);

            // then
            verify(valueOperations).set(eq(key), anyString());
        }

        @Test
        @DisplayName("TTL과 함께 객체를 캐시에 저장")
        void putObjectWithTTL() {
            // given
            String key = CacheKey.robotState("robot-001");
            TestData data = new TestData("test", 123);
            Duration ttl = CacheTTL.ROBOT_STATE;

            // when
            cacheService.put(key, data, ttl);

            // then
            verify(valueOperations).set(eq(key), anyString(), eq(ttl.getSeconds()), eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("null 키는 예외 발생")
        void throwExceptionWhenKeyIsNull() {
            // given
            TestData data = new TestData("test", 123);

            // when & then
            assertThatThrownBy(() -> cacheService.put(null, data))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("key");
        }

        @Test
        @DisplayName("빈 키는 예외 발생")
        void throwExceptionWhenKeyIsEmpty() {
            // given
            TestData data = new TestData("test", 123);

            // when & then
            assertThatThrownBy(() -> cacheService.put("  ", data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("빈 문자열");
        }

        @Test
        @DisplayName("null 값은 예외 발생")
        void throwExceptionWhenValueIsNull() {
            // given
            String key = CacheKey.robotState("robot-001");

            // when & then
            assertThatThrownBy(() -> cacheService.put(key, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("value");
        }
    }

    @Nested
    @DisplayName("get 테스트")
    class GetTest {

        @Test
        @DisplayName("캐시에서 객체 조회")
        void getObject() throws Exception {
            // given
            String key = CacheKey.robotState("robot-001");
            TestData data = new TestData("test", 123);
            String json = objectMapper.writeValueAsString(data);

            when(valueOperations.get(key)).thenReturn(json);

            // when
            Optional<TestData> result = cacheService.get(key, TestData.class);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("test");
            assertThat(result.get().getValue()).isEqualTo(123);
        }

        @Test
        @DisplayName("캐시 미스 시 Optional.empty 반환")
        void returnEmptyWhenCacheMiss() {
            // given
            String key = CacheKey.robotState("robot-001");

            when(valueOperations.get(key)).thenReturn(null);

            // when
            Optional<TestData> result = cacheService.get(key, TestData.class);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("역직렬화 실패 시 캐시 삭제 후 Optional.empty 반환")
        void deleteAndReturnEmptyWhenDeserializationFails() {
            // given
            String key = CacheKey.robotState("robot-001");
            String invalidJson = "invalid json";

            when(valueOperations.get(key)).thenReturn(invalidJson);

            // when
            Optional<TestData> result = cacheService.get(key, TestData.class);

            // then
            assertThat(result).isEmpty();
            verify(redisTemplate).delete(key); // 손상된 캐시 삭제 확인
        }
    }

    @Nested
    @DisplayName("exists 테스트")
    class ExistsTest {

        @Test
        @DisplayName("캐시에 키가 존재하면 true")
        void returnTrueWhenKeyExists() {
            // given
            String key = CacheKey.robotState("robot-001");

            when(redisTemplate.hasKey(key)).thenReturn(true);

            // when
            boolean result = cacheService.exists(key);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("캐시에 키가 없으면 false")
        void returnFalseWhenKeyNotExists() {
            // given
            String key = CacheKey.robotState("robot-001");

            when(redisTemplate.hasKey(key)).thenReturn(false);

            // when
            boolean result = cacheService.exists(key);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("delete 테스트")
    class DeleteTest {

        @Test
        @DisplayName("캐시에서 키 삭제")
        void deleteKey() {
            // given
            String key = CacheKey.robotState("robot-001");

            // when
            cacheService.delete(key);

            // then
            verify(redisTemplate).delete(key);
        }

        @Test
        @DisplayName("패턴과 일치하는 모든 키 삭제")
        void deleteByPattern() {
            // given
            String pattern = CacheKey.allByDomainAndType("robot", "state");
            Set<String> keys = new HashSet<>();
            keys.add("robot:state:robot-001");
            keys.add("robot:state:robot-002");

            when(redisTemplate.keys(pattern)).thenReturn(keys);

            // when
            cacheService.deleteByPattern(pattern);

            // then
            verify(redisTemplate).delete(keys);
        }
    }

    @Nested
    @DisplayName("keys 테스트")
    class KeysTest {

        @Test
        @DisplayName("패턴과 일치하는 모든 키 조회")
        void getKeysByPattern() {
            // given
            String pattern = CacheKey.allByDomainAndType("robot", "state");
            Set<String> expectedKeys = new HashSet<>();
            expectedKeys.add("robot:state:robot-001");
            expectedKeys.add("robot:state:robot-002");

            when(redisTemplate.keys(pattern)).thenReturn(expectedKeys);

            // when
            Set<String> result = cacheService.keys(pattern);

            // then
            assertThat(result).containsExactlyInAnyOrderElementsOf(expectedKeys);
        }
    }

    @Nested
    @DisplayName("expire 테스트")
    class ExpireTest {

        @Test
        @DisplayName("TTL 설정")
        void setExpire() {
            // given
            String key = CacheKey.robotState("robot-001");
            Duration ttl = CacheTTL.ROBOT_STATE;

            // when
            cacheService.expire(key, ttl);

            // then
            verify(redisTemplate).expire(key, ttl.getSeconds(), TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("남은 TTL 조회")
        void getExpire() {
            // given
            String key = CacheKey.robotState("robot-001");
            Long expectedExpire = 60L;

            when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(expectedExpire);

            // when
            Long result = cacheService.getExpire(key);

            // then
            assertThat(result).isEqualTo(expectedExpire);
        }
    }

    @Nested
    @DisplayName("CacheKey 테스트")
    class CacheKeyTest {

        @Test
        @DisplayName("로봇 상태 키 생성")
        void createRobotStateKey() {
            // when
            String key = CacheKey.robotState("robot-001");

            // then
            assertThat(key).isEqualTo("robot:state:robot-001");
        }

        @Test
        @DisplayName("로봇 위치 키 생성")
        void createRobotLocationKey() {
            // when
            String key = CacheKey.robotLocation("robot-001");

            // then
            assertThat(key).isEqualTo("robot:location:robot-001");
        }

        @Test
        @DisplayName("패턴: 도메인 전체")
        void createPatternForAllByDomain() {
            // when
            String pattern = CacheKey.allByDomain("robot");

            // then
            assertThat(pattern).isEqualTo("robot:*");
        }

        @Test
        @DisplayName("패턴: 도메인과 타입")
        void createPatternForAllByDomainAndType() {
            // when
            String pattern = CacheKey.allByDomainAndType("robot", "state");

            // then
            assertThat(pattern).isEqualTo("robot:state:*");
        }

        @Test
        @DisplayName("키 길이가 최대치를 초과하면 예외 발생")
        void throwExceptionWhenKeyExceedsMaxLength() {
            // given
            String longId = "x".repeat(600);

            // when & then
            assertThatThrownBy(() -> CacheKey.robotState(longId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("너무 깁니다");
        }

        @Test
        @DisplayName("키에 허용되지 않는 문자가 포함되면 예외 발생")
        void throwExceptionWhenKeyContainsInvalidCharacters() {
            // when & then
            assertThatThrownBy(() -> CacheKey.robotState("robot@#$%"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 문자");
        }
    }

    /**
     * 테스트용 데이터 클래스
     */
    static class TestData {
        private String name;
        private int value;

        public TestData() {
        }

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
