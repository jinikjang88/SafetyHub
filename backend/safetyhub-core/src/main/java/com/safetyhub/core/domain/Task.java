package com.safetyhub.core.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 작업(Task) 도메인 모델
 * 로봇에 할당될 수 있는 작업을 나타냄
 *
 * 불변 객체로 설계하여 스레드 안전성 보장
 */
@Getter
@Builder
@ToString
public class Task {

    /**
     * 작업 고유 ID
     */
    private final String id;

    /**
     * 작업 유형
     */
    private final TaskType taskType;

    /**
     * 작업 우선순위
     */
    private final TaskPriority priority;

    /**
     * 작업 상태
     */
    private final TaskStatus status;

    /**
     * 할당된 로봇 ID (할당 전에는 null)
     */
    private final String assignedRobotId;

    /**
     * 목표 위치
     */
    private final Location targetLocation;

    /**
     * 목표 구역 ID
     */
    private final String targetZoneId;

    /**
     * 작업 설명
     */
    private final String description;

    /**
     * 작업 생성 시간
     */
    private final LocalDateTime createdAt;

    /**
     * 작업 할당 시간
     */
    private final LocalDateTime assignedAt;

    /**
     * 작업 시작 시간
     */
    private final LocalDateTime startedAt;

    /**
     * 작업 완료 시간
     */
    private final LocalDateTime completedAt;

    /**
     * 실패 사유 (실패 시에만)
     */
    private final String failureReason;

    /**
     * 작업 유형 정의
     */
    public enum TaskType {
        PATROL("순찰"),
        DELIVERY("배송"),
        SAFETY_CHECK("안전 점검"),
        EMERGENCY_RESPONSE("긴급 대응"),
        CLEANING("청소"),
        DATA_COLLECTION("데이터 수집"),
        CHARGING("충전"),
        MAINTENANCE("유지보수");

        private final String displayName;

        TaskType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 빌더 클래스 커스터마이징
     */
    public static class TaskBuilder {
        public Task build() {
            // 기본값 설정
            if (id == null || id.isEmpty()) {
                id = UUID.randomUUID().toString();
            }
            if (createdAt == null) {
                createdAt = LocalDateTime.now();
            }
            if (status == null) {
                status = TaskStatus.PENDING;
            }

            // 필수 필드 검증
            validateRequired();

            return new Task(id, taskType, priority, status, assignedRobotId,
                    targetLocation, targetZoneId, description,
                    createdAt, assignedAt, startedAt, completedAt, failureReason);
        }

        private void validateRequired() {
            if (taskType == null) {
                throw new IllegalArgumentException("작업 유형(taskType)은 필수입니다");
            }
            if (priority == null) {
                throw new IllegalArgumentException("우선순위(priority)는 필수입니다");
            }
            if (targetZoneId == null || targetZoneId.isEmpty()) {
                throw new IllegalArgumentException("목표 구역 ID(targetZoneId)는 필수입니다");
            }
        }
    }

    /**
     * 작업 대기 시간 계산 (초 단위)
     * @return 대기 시간 (초)
     */
    public long getWaitingTimeSeconds() {
        if (assignedAt != null) {
            return Duration.between(createdAt, assignedAt).getSeconds();
        }
        return Duration.between(createdAt, LocalDateTime.now()).getSeconds();
    }

    /**
     * 작업 실행 시간 계산 (초 단위)
     * @return 실행 시간 (초), 아직 시작하지 않았으면 0
     */
    public long getExecutionTimeSeconds() {
        if (startedAt == null) {
            return 0;
        }
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return Duration.between(startedAt, endTime).getSeconds();
    }

    /**
     * 총 소요 시간 계산 (초 단위)
     * @return 생성부터 완료까지의 시간 (초)
     */
    public long getTotalTimeSeconds() {
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return Duration.between(createdAt, endTime).getSeconds();
    }

    /**
     * 대기 시간에 따라 승격된 우선순위 반환
     * @return 승격된 우선순위
     */
    public TaskPriority getEffectivePriority() {
        return priority.getPromotedPriority(getWaitingTimeSeconds());
    }

    /**
     * 작업이 할당 가능한 상태인지 확인
     * @return PENDING 상태이면 true
     */
    public boolean isAssignable() {
        return status.isPending();
    }

    /**
     * 작업이 진행 중인지 확인
     * @return 활성 상태이면 true
     */
    public boolean isInProgress() {
        return status.isActive();
    }

    /**
     * 작업이 완료되었는지 확인
     * @return 종료 상태이면 true
     */
    public boolean isCompleted() {
        return status.isTerminal();
    }

    /**
     * 로봇에 할당된 작업으로 변경
     * @param robotId 할당할 로봇 ID
     * @return 새로운 Task 인스턴스 (불변 객체)
     */
    public Task assignTo(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            throw new IllegalArgumentException("로봇 ID는 필수입니다");
        }
        if (!isAssignable()) {
            throw new IllegalStateException("PENDING 상태의 작업만 할당할 수 있습니다. 현재 상태: " + status);
        }

        return Task.builder()
                .id(id)
                .taskType(taskType)
                .priority(priority)
                .status(TaskStatus.ASSIGNED)
                .assignedRobotId(robotId)
                .targetLocation(targetLocation)
                .targetZoneId(targetZoneId)
                .description(description)
                .createdAt(createdAt)
                .assignedAt(LocalDateTime.now())
                .startedAt(startedAt)
                .completedAt(completedAt)
                .failureReason(failureReason)
                .build();
    }

    /**
     * 작업 시작
     * @return 새로운 Task 인스턴스 (불변 객체)
     */
    public Task start() {
        if (status != TaskStatus.ASSIGNED) {
            throw new IllegalStateException("ASSIGNED 상태의 작업만 시작할 수 있습니다. 현재 상태: " + status);
        }

        return Task.builder()
                .id(id)
                .taskType(taskType)
                .priority(priority)
                .status(TaskStatus.IN_PROGRESS)
                .assignedRobotId(assignedRobotId)
                .targetLocation(targetLocation)
                .targetZoneId(targetZoneId)
                .description(description)
                .createdAt(createdAt)
                .assignedAt(assignedAt)
                .startedAt(LocalDateTime.now())
                .completedAt(completedAt)
                .failureReason(failureReason)
                .build();
    }

    /**
     * 작업 완료
     * @return 새로운 Task 인스턴스 (불변 객체)
     */
    public Task complete() {
        if (status != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("IN_PROGRESS 상태의 작업만 완료할 수 있습니다. 현재 상태: " + status);
        }

        return Task.builder()
                .id(id)
                .taskType(taskType)
                .priority(priority)
                .status(TaskStatus.COMPLETED)
                .assignedRobotId(assignedRobotId)
                .targetLocation(targetLocation)
                .targetZoneId(targetZoneId)
                .description(description)
                .createdAt(createdAt)
                .assignedAt(assignedAt)
                .startedAt(startedAt)
                .completedAt(LocalDateTime.now())
                .failureReason(failureReason)
                .build();
    }

    /**
     * 작업 실패
     * @param reason 실패 사유
     * @return 새로운 Task 인스턴스 (불변 객체)
     */
    public Task fail(String reason) {
        if (!status.isActive()) {
            throw new IllegalStateException("활성 상태의 작업만 실패 처리할 수 있습니다. 현재 상태: " + status);
        }

        return Task.builder()
                .id(id)
                .taskType(taskType)
                .priority(priority)
                .status(TaskStatus.FAILED)
                .assignedRobotId(assignedRobotId)
                .targetLocation(targetLocation)
                .targetZoneId(targetZoneId)
                .description(description)
                .createdAt(createdAt)
                .assignedAt(assignedAt)
                .startedAt(startedAt)
                .completedAt(LocalDateTime.now())
                .failureReason(reason)
                .build();
    }

    /**
     * 작업 취소
     * @return 새로운 Task 인스턴스 (불변 객체)
     */
    public Task cancel() {
        if (status.isTerminal()) {
            throw new IllegalStateException("이미 종료된 작업은 취소할 수 없습니다. 현재 상태: " + status);
        }

        return Task.builder()
                .id(id)
                .taskType(taskType)
                .priority(priority)
                .status(TaskStatus.CANCELLED)
                .assignedRobotId(assignedRobotId)
                .targetLocation(targetLocation)
                .targetZoneId(targetZoneId)
                .description(description)
                .createdAt(createdAt)
                .assignedAt(assignedAt)
                .startedAt(startedAt)
                .completedAt(LocalDateTime.now())
                .failureReason(failureReason)
                .build();
    }
}
