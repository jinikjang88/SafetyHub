package com.safetyhub.infrastructure.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * 캐시 서비스 인터페이스
 *
 * Port (포트) 인터페이스:
 * - Core 도메인에서 정의
 * - Infrastructure에서 구현
 * - 단방향 의존성: Core ← Infrastructure
 *
 * 역할:
 * - Hot Path에서 빠른 데이터 조회
 * - 상태 정보 임시 저장
 * - 세션 관리
 *
 * 설계 원칙:
 * - 기술 독립적 인터페이스
 * - Redis, Hazelcast 등 다양한 구현 지원
 *
 * @param <T> 캐시할 값의 타입
 */
public interface CacheService {

    /**
     * 캐시에 값 저장
     *
     * @param key 키
     * @param value 값
     */
    void put(String key, Object value);

    /**
     * 캐시에 값 저장 (TTL 지정)
     *
     * @param key 키
     * @param value 값
     * @param ttl 만료 시간
     */
    void put(String key, Object value, Duration ttl);

    /**
     * 캐시에서 값 조회
     *
     * @param key 키
     * @param type 값의 타입
     * @param <T> 값의 타입
     * @return Optional로 감싼 값
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * 캐시에 키가 존재하는지 확인
     *
     * @param key 키
     * @return 존재 여부
     */
    boolean exists(String key);

    /**
     * 캐시에서 값 삭제
     *
     * @param key 키
     */
    void delete(String key);

    /**
     * 패턴과 일치하는 모든 키 삭제
     *
     * 예: "robot:*" - robot으로 시작하는 모든 키
     *
     * @param pattern 패턴 (와일드카드 지원)
     */
    void deleteByPattern(String pattern);

    /**
     * 패턴과 일치하는 모든 키 조회
     *
     * @param pattern 패턴 (와일드카드 지원)
     * @return 키 목록
     */
    Set<String> keys(String pattern);

    /**
     * TTL 설정
     *
     * @param key 키
     * @param ttl 만료 시간
     */
    void expire(String key, Duration ttl);

    /**
     * 남은 TTL 조회
     *
     * @param key 키
     * @return 남은 시간 (초)
     */
    Long getExpire(String key);
}
