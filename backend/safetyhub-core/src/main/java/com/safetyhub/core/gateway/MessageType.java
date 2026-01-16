package com.safetyhub.core.gateway;

/**
 * 메시지 타입
 *
 * 게이트웨이를 통과하는 메시지의 용도를 정의
 */
public enum MessageType {
    /**
     * 명령 (Command)
     * - 외부 → 내부 (장치 제어, 작업 지시 등)
     * - 예: 설비 정지, 로봇 이동 명령
     */
    COMMAND,

    /**
     * 이벤트 (Event)
     * - 내부 → 외부 또는 내부 → 내부
     * - 예: 센서 데이터, 상태 변경 알림
     */
    EVENT,

    /**
     * 쿼리 (Query)
     * - 상태 조회 요청
     * - 예: 현재 위치 조회, 배터리 레벨 조회
     */
    QUERY,

    /**
     * 응답 (Response)
     * - 쿼리에 대한 응답
     */
    RESPONSE,

    /**
     * 하트비트 (Heartbeat)
     * - 연결 유지 확인
     * - 장치 생존 확인
     */
    HEARTBEAT
}
