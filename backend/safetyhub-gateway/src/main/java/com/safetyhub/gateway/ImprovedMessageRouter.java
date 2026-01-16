package com.safetyhub.gateway;

import com.safetyhub.core.gateway.MessageEnvelope;
import com.safetyhub.core.gateway.MessageHandler;
import com.safetyhub.core.gateway.MessageHandlingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 개선된 메시지 라우터
 *
 * Hot/Warm/Cold Path로 메시지를 라우팅하고 처리
 *
 * 성능 목표:
 * - Hot Path: < 10ms (긴급 상황 즉시 처리)
 * - Warm Path: < 500ms (일반 작업 처리)
 * - Cold Path: 비동기 (배치 처리)
 *
 * 보안:
 * - 입력 검증 (null 체크)
 * - 에러 처리 (민감정보 노출 방지)
 * - 스레드 안전성 (불변 객체 사용)
 *
 * 설계 패턴:
 * - 전략 패턴: Path별 핸들러 교체 가능
 * - 템플릿 메서드 패턴: 공통 로직 재사용
 */
@Slf4j
@Component
public class ImprovedMessageRouter {

    private final MessageHandler hotPathHandler;
    private final MessageHandler warmPathHandler;
    private final MessageHandler coldPathHandler;
    private final ExecutorService coldPathExecutor;

    /**
     * 생성자 주입
     *
     * @param hotPathHandler Hot Path 핸들러
     * @param warmPathHandler Warm Path 핸들러
     * @param coldPathHandler Cold Path 핸들러
     */
    public ImprovedMessageRouter(
            MessageHandler hotPathHandler,
            MessageHandler warmPathHandler,
            MessageHandler coldPathHandler) {

        this.hotPathHandler = Objects.requireNonNull(hotPathHandler, "hotPathHandler는 필수입니다");
        this.warmPathHandler = Objects.requireNonNull(warmPathHandler, "warmPathHandler는 필수입니다");
        this.coldPathHandler = Objects.requireNonNull(coldPathHandler, "coldPathHandler는 필수입니다");

        // Cold Path용 스레드 풀 (비동기 처리)
        this.coldPathExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("cold-path-" + thread.getId());
                thread.setDaemon(true); // 데몬 스레드로 설정
                return thread;
            }
        );
    }

    /**
     * 메시지 라우팅
     *
     * 우선순위에 따라 적절한 Path로 라우팅:
     * - CRITICAL, HIGH → Hot Path (즉시 처리)
     * - NORMAL → Warm Path (일반 처리)
     * - LOW → Cold Path (비동기 배치 처리)
     *
     * @param envelope 라우팅할 메시지
     * @throws IllegalArgumentException envelope이 null인 경우
     */
    public void route(MessageEnvelope envelope) {
        // 입력 검증
        Objects.requireNonNull(envelope, "envelope은 null일 수 없습니다");

        try {
            // Hot Path 판별 (CRITICAL, HIGH)
            if (envelope.isHotPath()) {
                routeToHotPath(envelope);
            }
            // Cold Path 판별 (LOW)
            else if (envelope.isColdPath()) {
                routeToColdPath(envelope);
            }
            // Warm Path (NORMAL)
            else {
                routeToWarmPath(envelope);
            }
        } catch (MessageHandlingException e) {
            // 메시지 처리 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            // 그 외 예외는 래핑하여 전파
            log.error("메시지 라우팅 중 예외 발생: messageId={}, priority={}",
                envelope.getMessageId(), envelope.getPriority(), e);
            throw new MessageHandlingException("메시지 라우팅 실패", e);
        }
    }

    /**
     * Hot Path 라우팅
     *
     * 목표 응답 시간: < 10ms
     *
     * 처리 항목:
     * - 긴급 정지 (설비, 로봇)
     * - 충돌 회피
     * - 119 자동 신고
     * - 즉각적인 대시보드 알림
     *
     * 특징:
     * - In-Memory 처리 (DB 접근 최소화)
     * - 동기 처리 (즉시 응답)
     * - 최소한의 로직
     *
     * @param envelope 처리할 메시지
     */
    private void routeToHotPath(MessageEnvelope envelope) {
        log.warn("🔴 HOT PATH: messageId={}, priority={}, source={}",
            envelope.getMessageId(), envelope.getPriority(), envelope.getSource());

        long startTime = System.currentTimeMillis();

        try {
            hotPathHandler.handle(envelope);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("HOT PATH 처리 완료: {}ms", elapsedTime);

            // 성능 목표 초과 경고
            if (elapsedTime > 10) {
                log.warn("⚠️ HOT PATH 성능 목표 초과: {}ms > 10ms", elapsedTime);
            }

        } catch (Exception e) {
            log.error("HOT PATH 처리 실패: messageId={}", envelope.getMessageId(), e);
            throw new MessageHandlingException("HOT", "긴급 메시지 처리 실패", e);
        }
    }

    /**
     * Warm Path 라우팅
     *
     * 목표 응답 시간: < 500ms
     *
     * 처리 항목:
     * - 태스크 분배
     * - 경로 계산 (A* 알고리즘)
     * - 스케줄링
     * - 알림 발송
     * - DB 업데이트
     *
     * 특징:
     * - DB 접근 허용
     * - 동기 처리
     * - 복잡한 비즈니스 로직
     *
     * @param envelope 처리할 메시지
     */
    private void routeToWarmPath(MessageEnvelope envelope) {
        log.debug("🟡 WARM PATH: messageId={}, priority={}, source={}",
            envelope.getMessageId(), envelope.getPriority(), envelope.getSource());

        long startTime = System.currentTimeMillis();

        try {
            warmPathHandler.handle(envelope);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.debug("WARM PATH 처리 완료: {}ms", elapsedTime);

            // 성능 목표 초과 경고
            if (elapsedTime > 500) {
                log.warn("⚠️ WARM PATH 성능 목표 초과: {}ms > 500ms", elapsedTime);
            }

        } catch (Exception e) {
            log.error("WARM PATH 처리 실패: messageId={}", envelope.getMessageId(), e);
            throw new MessageHandlingException("WARM", "일반 메시지 처리 실패", e);
        }
    }

    /**
     * Cold Path 라우팅
     *
     * 목표: 비동기 처리 (응답 시간 제약 없음)
     *
     * 처리 항목:
     * - 로그 저장
     * - 통계 분석
     * - 리포팅
     * - 데이터 마이닝
     * - 배치 처리
     *
     * 특징:
     * - 비동기 처리 (CompletableFuture)
     * - 별도 스레드 풀
     * - 에러가 메인 플로우에 영향 없음
     *
     * @param envelope 처리할 메시지
     */
    private void routeToColdPath(MessageEnvelope envelope) {
        log.debug("🔵 COLD PATH: messageId={}, priority={}, source={}",
            envelope.getMessageId(), envelope.getPriority(), envelope.getSource());

        // 비동기 처리 (Fire-and-Forget)
        CompletableFuture.runAsync(() -> {
            try {
                coldPathHandler.handle(envelope);
                log.debug("COLD PATH 처리 완료: messageId={}", envelope.getMessageId());

            } catch (Exception e) {
                // Cold Path 에러는 로깅만 (메인 플로우에 영향 없음)
                log.error("COLD PATH 처리 실패 (무시): messageId={}",
                    envelope.getMessageId(), e);
            }
        }, coldPathExecutor);
    }

    /**
     * 라우터 종료 (리소스 정리)
     *
     * 애플리케이션 종료 시 호출되어야 함
     */
    public void shutdown() {
        log.info("MessageRouter 종료 중...");
        coldPathExecutor.shutdown();
        log.info("MessageRouter 종료 완료");
    }
}
