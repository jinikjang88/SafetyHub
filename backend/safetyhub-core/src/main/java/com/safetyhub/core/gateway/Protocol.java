package com.safetyhub.core.gateway;

/**
 * 통신 프로토콜 타입
 *
 * SafetyHub 게이트웨이가 지원하는 프로토콜 정의
 * - 각 프로토콜은 별도의 어댑터를 통해 통합 메시지로 변환됨
 *
 * @see MessageEnvelope
 * @see ProtocolAdapter
 */
public enum Protocol {
    /**
     * MQTT (Message Queuing Telemetry Transport)
     * - IoT 장치 (SafetyKit, LifeGuard)
     * - QoS 0,1,2 지원
     */
    MQTT,

    /**
     * WebSocket (STOMP over WebSocket)
     * - 실시간 양방향 통신
     * - 로봇 시뮬레이터, 대시보드
     */
    WEBSOCKET,

    /**
     * REST (HTTP/HTTPS)
     * - 일반 API 호출
     * - 외부 시스템 연동
     */
    REST,

    /**
     * gRPC (Google Remote Procedure Call)
     * - 고성능 내부 서비스 통신
     * - 미래 확장용
     */
    GRPC,

    /**
     * 시뮬레이터 (가상 프로토콜)
     * - 개발/테스트 환경
     * - 실제 하드웨어 없이 시뮬레이션
     */
    SIMULATOR
}
