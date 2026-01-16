package com.safetyhub.infrastructure.cache;

/**
 * 캐시 키 네이밍 유틸리티
 *
 * 일관된 키 네이밍 전략:
 * - {domain}:{type}:{id}
 * - 예: robot:state:robot-001
 *
 * 장점:
 * - 가독성 향상
 * - 패턴 기반 삭제 용이
 * - 충돌 방지
 * - 디버깅 편의성
 *
 * 보안:
 * - 키 길이 제한 (512자)
 * - 특수문자 제한 (알파벳, 숫자, :_- 만 허용)
 */
public class CacheKey {

    /**
     * 키 최대 길이
     */
    public static final int MAX_KEY_LENGTH = 512;

    /**
     * 구분자
     */
    private static final String DELIMITER = ":";

    /**
     * 로봇 상태 캐시 키
     *
     * @param robotId 로봇 ID
     * @return robot:state:{robotId}
     */
    public static String robotState(String robotId) {
        return buildKey("robot", "state", robotId);
    }

    /**
     * 로봇 위치 캐시 키
     *
     * @param robotId 로봇 ID
     * @return robot:location:{robotId}
     */
    public static String robotLocation(String robotId) {
        return buildKey("robot", "location", robotId);
    }

    /**
     * 로봇 배터리 캐시 키
     *
     * @param robotId 로봇 ID
     * @return robot:battery:{robotId}
     */
    public static String robotBattery(String robotId) {
        return buildKey("robot", "battery", robotId);
    }

    /**
     * 장치 상태 캐시 키
     *
     * @param deviceId 장치 ID
     * @return device:state:{deviceId}
     */
    public static String deviceState(String deviceId) {
        return buildKey("device", "state", deviceId);
    }

    /**
     * 작업자 위치 캐시 키
     *
     * @param workerId 작업자 ID
     * @return worker:location:{workerId}
     */
    public static String workerLocation(String workerId) {
        return buildKey("worker", "location", workerId);
    }

    /**
     * 작업자 건강 상태 캐시 키
     *
     * @param workerId 작업자 ID
     * @return worker:health:{workerId}
     */
    public static String workerHealth(String workerId) {
        return buildKey("worker", "health", workerId);
    }

    /**
     * 하트비트 캐시 키
     *
     * @param sourceId 소스 ID
     * @return heartbeat:{sourceId}
     */
    public static String heartbeat(String sourceId) {
        return buildKey("heartbeat", sourceId);
    }

    /**
     * 긴급 상황 캐시 키
     *
     * @param emergencyId 긴급 상황 ID
     * @return emergency:{emergencyId}
     */
    public static String emergency(String emergencyId) {
        return buildKey("emergency", emergencyId);
    }

    /**
     * 세션 캐시 키
     *
     * @param sessionId 세션 ID
     * @return session:{sessionId}
     */
    public static String session(String sessionId) {
        return buildKey("session", sessionId);
    }

    /**
     * 패턴: 특정 도메인의 모든 키
     *
     * @param domain 도메인
     * @return {domain}:*
     */
    public static String allByDomain(String domain) {
        return domain + ":*";
    }

    /**
     * 패턴: 특정 도메인과 타입의 모든 키
     *
     * @param domain 도메인
     * @param type 타입
     * @return {domain}:{type}:*
     */
    public static String allByDomainAndType(String domain, String type) {
        return domain + DELIMITER + type + ":*";
    }

    /**
     * 키 빌더
     *
     * @param parts 키 구성 요소
     * @return 결합된 키
     */
    private static String buildKey(String... parts) {
        String key = String.join(DELIMITER, parts);

        // 키 길이 검증
        if (key.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("캐시 키가 너무 깁니다. (현재: %d, 최대: %d)",
                    key.length(), MAX_KEY_LENGTH));
        }

        // 키 형식 검증 (영문, 숫자, :_- 만 허용)
        if (!key.matches("^[a-zA-Z0-9:_-]+$")) {
            throw new IllegalArgumentException(
                "캐시 키에 허용되지 않는 문자가 포함되어 있습니다: " + key);
        }

        return key;
    }
}
