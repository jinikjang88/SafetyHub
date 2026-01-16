package com.safetyhub.application.charging;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * 충전 대기열
 * 우선순위 기반으로 충전 요청 관리
 */
@Slf4j
public class ChargingQueue {

    /**
     * 우선순위 기반 대기열
     */
    private final PriorityBlockingQueue<ChargingRequest> queue;

    /**
     * 빠른 조회를 위한 Request ID -> Request 매핑
     */
    private final Map<String, ChargingRequest> requestMap;

    /**
     * 최대 큐 크기 (DoS 방지)
     */
    private final int maxSize;

    /**
     * 기본 최대 큐 크기
     */
    private static final int DEFAULT_MAX_SIZE = 1000;

    public ChargingQueue() {
        this(DEFAULT_MAX_SIZE);
    }

    public ChargingQueue(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("최대 크기는 0보다 커야 합니다");
        }
        this.maxSize = maxSize;
        this.queue = new PriorityBlockingQueue<>(100, new ChargingRequestComparator());
        this.requestMap = new ConcurrentHashMap<>();
    }

    /**
     * 충전 요청 추가
     */
    public void enqueue(ChargingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("충전 요청은 null일 수 없습니다");
        }
        if (request.getStatus() != ChargingRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("PENDING 상태의 요청만 추가할 수 있습니다. 현재 상태: " + request.getStatus());
        }

        // 큐 크기 제한
        if (size() >= maxSize) {
            log.error("충전 대기열이 최대 크기에 도달했습니다. 현재: {}, 최대: {}", size(), maxSize);
            throw new IllegalStateException("충전 대기열이 가득 찼습니다");
        }

        // 중복 확인
        if (requestMap.containsKey(request.getRequestId())) {
            log.warn("이미 대기열에 존재하는 요청입니다. Request ID: {}", request.getRequestId());
            throw new IllegalArgumentException("이미 대기열에 존재하는 요청입니다");
        }

        queue.offer(request);
        requestMap.put(request.getRequestId(), request);

        log.info("충전 요청 추가. Robot: {}, Priority: {}, Battery: {}%, Queue Size: {}",
                request.getRobotId(), request.getPriority(), request.getCurrentBatteryLevel(), size());
    }

    /**
     * 가장 높은 우선순위의 요청 꺼내기
     */
    public Optional<ChargingRequest> dequeue() {
        ChargingRequest request = queue.poll();
        if (request != null) {
            requestMap.remove(request.getRequestId());
            log.debug("충전 요청 꺼냄. Robot: {}, Priority: {}, Queue Size: {}",
                    request.getRobotId(), request.getPriority(), size());
        }
        return Optional.ofNullable(request);
    }

    /**
     * 가장 높은 우선순위의 요청 조회 (제거하지 않음)
     */
    public Optional<ChargingRequest> peek() {
        return Optional.ofNullable(queue.peek());
    }

    /**
     * 특정 요청 제거
     */
    public Optional<ChargingRequest> remove(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("요청 ID는 null이거나 빈 문자열일 수 없습니다");
        }

        ChargingRequest request = requestMap.remove(requestId);
        if (request != null) {
            queue.remove(request);
            log.info("충전 요청 제거. Request ID: {}, Robot: {}", requestId, request.getRobotId());
        }
        return Optional.ofNullable(request);
    }

    /**
     * 로봇 ID로 요청 제거
     */
    public Optional<ChargingRequest> removeByRobotId(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            return Optional.empty();
        }

        Optional<ChargingRequest> request = requestMap.values().stream()
                .filter(r -> r.getRobotId().equals(robotId))
                .findFirst();

        request.ifPresent(r -> remove(r.getRequestId()));
        return request;
    }

    /**
     * 우선순위별 요청 조회
     */
    public List<ChargingRequest> getRequestsByPriority(ChargingRequest.ChargingPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("우선순위는 null일 수 없습니다");
        }

        return requestMap.values().stream()
                .filter(r -> r.getPriority() == priority)
                .sorted(new ChargingRequestComparator())
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * 모든 요청 조회 (우선순위 순)
     */
    public List<ChargingRequest> getAllRequests() {
        return requestMap.values().stream()
                .sorted(new ChargingRequestComparator())
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * 긴급 요청 조회
     */
    public List<ChargingRequest> getUrgentRequests() {
        return getRequestsByPriority(ChargingRequest.ChargingPriority.URGENT);
    }

    /**
     * 큐 크기
     */
    public int size() {
        return requestMap.size();
    }

    /**
     * 큐가 비어있는지 확인
     */
    public boolean isEmpty() {
        return requestMap.isEmpty();
    }

    /**
     * 큐 비우기
     */
    public void clear() {
        queue.clear();
        requestMap.clear();
        log.info("충전 대기열이 비워졌습니다");
    }

    /**
     * 요청 존재 확인
     */
    public boolean contains(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            return false;
        }
        return requestMap.containsKey(requestId);
    }

    /**
     * 로봇의 요청 존재 확인
     */
    public boolean containsRobot(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            return false;
        }
        return requestMap.values().stream()
                .anyMatch(r -> r.getRobotId().equals(robotId));
    }

    /**
     * 충전 요청 비교자
     * 1순위: 우선순위 (URGENT > HIGH > NORMAL > LOW)
     * 2순위: 요청 시간 (오래된 것 우선 - FIFO)
     */
    private static class ChargingRequestComparator implements Comparator<ChargingRequest> {
        @Override
        public int compare(ChargingRequest r1, ChargingRequest r2) {
            // 1순위: 우선순위
            int priorityComparison = r1.getPriority().compareTo(r2.getPriority());
            if (priorityComparison != 0) {
                return priorityComparison;
            }

            // 2순위: 요청 시간 (오래된 것 우선)
            return r1.getRequestedAt().compareTo(r2.getRequestedAt());
        }
    }
}
