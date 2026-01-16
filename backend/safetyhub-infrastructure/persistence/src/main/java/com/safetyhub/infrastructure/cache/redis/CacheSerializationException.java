package com.safetyhub.infrastructure.cache.redis;

/**
 * 캐시 직렬화/역직렬화 예외
 *
 * JSON 변환 실패 시 발생
 *
 * 발생 상황:
 * - 직렬화 불가능한 객체
 * - JSON 형식 오류
 * - 타입 불일치
 *
 * 보안 고려사항:
 * - 에러 메시지에 민감정보 노출 금지
 */
public class CacheSerializationException extends RuntimeException {

    /**
     * 기본 생성자
     *
     * @param message 에러 메시지
     */
    public CacheSerializationException(String message) {
        super(message);
    }

    /**
     * 원인 예외를 포함하는 생성자
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public CacheSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
