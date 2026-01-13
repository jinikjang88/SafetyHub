package com.safetyhub.infrastructure.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 119 긴급 신고 API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Emergency119ApiClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * 119 신고 요청
     */
    public Mono<Emergency119Response> call119(Emergency119Request request) {
        log.error("119 EMERGENCY CALL - Location: ({}, {}), Type: {}, Description: {}",
                request.latitude(), request.longitude(),
                request.emergencyType(), request.description());

        // TODO: 실제 119 API 연동
        // 현재는 시뮬레이션 응답
        return Mono.just(new Emergency119Response(
                "CALL-" + System.currentTimeMillis(),
                "RECEIVED",
                "Emergency call received"
        ));
    }

    /**
     * 119 신고 요청 DTO
     */
    public record Emergency119Request(
            String emergencyId,
            String emergencyType,
            String description,
            Double latitude,
            Double longitude,
            String address,
            String reporterName,
            String reporterPhone,
            Integer affectedCount
    ) {}

    /**
     * 119 신고 응답 DTO
     */
    public record Emergency119Response(
            String callId,
            String status,
            String message
    ) {}
}
