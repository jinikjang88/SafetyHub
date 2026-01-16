package com.safetyhub.infrastructure.cache;

import java.time.Duration;

/**
 * 캐시 TTL (Time To Live) 정책
 *
 * 도메인별 캐시 만료 시간 정의
 *
 * 원칙:
 * - Hot 데이터: 짧은 TTL (초 단위)
 * - Warm 데이터: 중간 TTL (분 단위)
 * - Cold 데이터: 긴 TTL (시간 단위)
 *
 * 보안:
 * - 민감정보는 짧은 TTL
 * - 세션은 자동 만료로 보안 강화
 */
public class CacheTTL {

    /**
     * 로봇 상태 (1분)
     *
     * 자주 변경되므로 짧은 TTL
     */
    public static final Duration ROBOT_STATE = Duration.ofMinutes(1);

    /**
     * 로봇 위치 (30초)
     *
     * 실시간 추적이므로 매우 짧은 TTL
     */
    public static final Duration ROBOT_LOCATION = Duration.ofSeconds(30);

    /**
     * 로봇 배터리 (5분)
     *
     * 천천히 변경되므로 중간 TTL
     */
    public static final Duration ROBOT_BATTERY = Duration.ofMinutes(5);

    /**
     * 장치 상태 (2분)
     */
    public static final Duration DEVICE_STATE = Duration.ofMinutes(2);

    /**
     * 작업자 위치 (30초)
     *
     * 실시간 추적
     */
    public static final Duration WORKER_LOCATION = Duration.ofSeconds(30);

    /**
     * 작업자 건강 상태 (1분)
     */
    public static final Duration WORKER_HEALTH = Duration.ofMinutes(1);

    /**
     * 하트비트 (1분)
     *
     * 하트비트 타임아웃 감지용
     */
    public static final Duration HEARTBEAT = Duration.ofMinutes(1);

    /**
     * 긴급 상황 (10분)
     *
     * 긴급 상황은 빠르게 처리되어야 하지만
     * 기록은 잠시 유지
     */
    public static final Duration EMERGENCY = Duration.ofMinutes(10);

    /**
     * 세션 (30분)
     *
     * 사용자 세션
     */
    public static final Duration SESSION = Duration.ofMinutes(30);

    /**
     * 기본 TTL (5분)
     *
     * 특별한 정책이 없는 경우
     */
    public static final Duration DEFAULT = Duration.ofMinutes(5);
}
