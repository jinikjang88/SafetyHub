package com.safetyhub.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Task 도메인 모델 테스트")
class TaskTest {

    @Test
    @DisplayName("Task 생성 - 정상")
    void createTask_Success() {
        // given & when
        Task task = Task.builder()
                .taskType(Task.TaskType.PATROL)
                .priority(TaskPriority.NORMAL)
                .targetZoneId("zone-001")
                .description("정기 순찰")
                .build();

        // then
        assertNotNull(task.getId());
        assertEquals(Task.TaskType.PATROL, task.getTaskType());
        assertEquals(TaskPriority.NORMAL, task.getPriority());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals("zone-001", task.getTargetZoneId());
        assertNotNull(task.getCreatedAt());
        assertNull(task.getAssignedRobotId());
    }

    @Test
    @DisplayName("Task 생성 - 필수 필드 누락 시 예외 발생")
    void createTask_MissingRequiredFields_ThrowsException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                Task.builder()
                        .priority(TaskPriority.NORMAL)
                        .targetZoneId("zone-001")
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                Task.builder()
                        .taskType(Task.TaskType.PATROL)
                        .targetZoneId("zone-001")
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                Task.builder()
                        .taskType(Task.TaskType.PATROL)
                        .priority(TaskPriority.NORMAL)
                        .build()
        );
    }

    @Test
    @DisplayName("작업 할당 - 정상")
    void assignTask_Success() {
        // given
        Task task = createSampleTask();

        // when
        Task assignedTask = task.assignTo("robot-001");

        // then
        assertEquals("robot-001", assignedTask.getAssignedRobotId());
        assertEquals(TaskStatus.ASSIGNED, assignedTask.getStatus());
        assertNotNull(assignedTask.getAssignedAt());
        assertEquals(task.getId(), assignedTask.getId()); // 같은 작업
        assertTrue(task.isAssignable()); // 원본은 변경 안 됨
        assertFalse(assignedTask.isAssignable()); // 할당된 작업은 더 이상 할당 불가
    }

    @Test
    @DisplayName("작업 할당 - PENDING 상태가 아니면 예외 발생")
    void assignTask_NotPending_ThrowsException() {
        // given
        Task task = createSampleTask().assignTo("robot-001");

        // when & then
        assertThrows(IllegalStateException.class, () ->
                task.assignTo("robot-002")
        );
    }

    @Test
    @DisplayName("작업 시작 - 정상")
    void startTask_Success() {
        // given
        Task task = createSampleTask().assignTo("robot-001");

        // when
        Task startedTask = task.start();

        // then
        assertEquals(TaskStatus.IN_PROGRESS, startedTask.getStatus());
        assertNotNull(startedTask.getStartedAt());
        assertTrue(startedTask.isInProgress());
    }

    @Test
    @DisplayName("작업 시작 - ASSIGNED 상태가 아니면 예외 발생")
    void startTask_NotAssigned_ThrowsException() {
        // given
        Task task = createSampleTask();

        // when & then
        assertThrows(IllegalStateException.class, task::start);
    }

    @Test
    @DisplayName("작업 완료 - 정상")
    void completeTask_Success() {
        // given
        Task task = createSampleTask()
                .assignTo("robot-001")
                .start();

        // when
        Task completedTask = task.complete();

        // then
        assertEquals(TaskStatus.COMPLETED, completedTask.getStatus());
        assertNotNull(completedTask.getCompletedAt());
        assertTrue(completedTask.isCompleted());
    }

    @Test
    @DisplayName("작업 완료 - IN_PROGRESS 상태가 아니면 예외 발생")
    void completeTask_NotInProgress_ThrowsException() {
        // given
        Task task = createSampleTask();

        // when & then
        assertThrows(IllegalStateException.class, task::complete);
    }

    @Test
    @DisplayName("작업 실패 - 정상")
    void failTask_Success() {
        // given
        Task task = createSampleTask()
                .assignTo("robot-001")
                .start();

        // when
        Task failedTask = task.fail("로봇 배터리 부족");

        // then
        assertEquals(TaskStatus.FAILED, failedTask.getStatus());
        assertEquals("로봇 배터리 부족", failedTask.getFailureReason());
        assertNotNull(failedTask.getCompletedAt());
        assertTrue(failedTask.isCompleted());
    }

    @Test
    @DisplayName("작업 취소 - 정상")
    void cancelTask_Success() {
        // given
        Task task = createSampleTask();

        // when
        Task cancelledTask = task.cancel();

        // then
        assertEquals(TaskStatus.CANCELLED, cancelledTask.getStatus());
        assertNotNull(cancelledTask.getCompletedAt());
        assertTrue(cancelledTask.isCompleted());
    }

    @Test
    @DisplayName("작업 취소 - 이미 종료된 작업은 취소 불가")
    void cancelTask_AlreadyCompleted_ThrowsException() {
        // given
        Task task = createSampleTask()
                .assignTo("robot-001")
                .start()
                .complete();

        // when & then
        assertThrows(IllegalStateException.class, task::cancel);
    }

    @Test
    @DisplayName("대기 시간 계산")
    void calculateWaitingTime() throws InterruptedException {
        // given
        Task task = createSampleTask();

        // when
        Thread.sleep(100);
        long waitingTime = task.getWaitingTimeSeconds();

        // then
        assertTrue(waitingTime >= 0);
    }

    @Test
    @DisplayName("실행 시간 계산")
    void calculateExecutionTime() throws InterruptedException {
        // given
        Task task = createSampleTask()
                .assignTo("robot-001")
                .start();

        // when
        Thread.sleep(100);
        long executionTime = task.getExecutionTimeSeconds();

        // then
        assertTrue(executionTime >= 0);
    }

    @Test
    @DisplayName("총 소요 시간 계산")
    void calculateTotalTime() throws InterruptedException {
        // given
        Task task = createSampleTask();

        // when
        Thread.sleep(100);
        long totalTime = task.getTotalTimeSeconds();

        // then
        assertTrue(totalTime >= 0);
    }

    @Test
    @DisplayName("우선순위 승격 - 대기 시간에 따라")
    void priorityPromotion() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(3);
        Task task = Task.builder()
                .taskType(Task.TaskType.PATROL)
                .priority(TaskPriority.LOW)
                .targetZoneId("zone-001")
                .createdAt(pastTime)
                .build();

        // when
        TaskPriority effectivePriority = task.getEffectivePriority();

        // then
        // 2시간 이상 대기 시 LOW -> NORMAL로 승격
        assertTrue(effectivePriority.getLevel() < TaskPriority.LOW.getLevel());
    }

    @Test
    @DisplayName("TaskStatus - isTerminal 메서드")
    void taskStatus_isTerminal() {
        assertTrue(TaskStatus.COMPLETED.isTerminal());
        assertTrue(TaskStatus.FAILED.isTerminal());
        assertTrue(TaskStatus.CANCELLED.isTerminal());
        assertFalse(TaskStatus.PENDING.isTerminal());
        assertFalse(TaskStatus.ASSIGNED.isTerminal());
        assertFalse(TaskStatus.IN_PROGRESS.isTerminal());
    }

    @Test
    @DisplayName("TaskStatus - isActive 메서드")
    void taskStatus_isActive() {
        assertTrue(TaskStatus.ASSIGNED.isActive());
        assertTrue(TaskStatus.IN_PROGRESS.isActive());
        assertFalse(TaskStatus.PENDING.isActive());
        assertFalse(TaskStatus.COMPLETED.isActive());
    }

    @Test
    @DisplayName("TaskPriority - Hot/Warm/Cold Path 판별")
    void taskPriority_PathDetection() {
        assertTrue(TaskPriority.CRITICAL.isHotPath());
        assertTrue(TaskPriority.HIGH.isHotPath());
        assertFalse(TaskPriority.NORMAL.isHotPath());

        assertTrue(TaskPriority.NORMAL.isWarmPath());
        assertFalse(TaskPriority.CRITICAL.isWarmPath());

        assertTrue(TaskPriority.LOW.isColdPath());
        assertFalse(TaskPriority.NORMAL.isColdPath());
    }

    @Test
    @DisplayName("TaskPriority - 비교")
    void taskPriority_Comparison() {
        assertTrue(TaskPriority.CRITICAL.compareTo(TaskPriority.HIGH) < 0);
        assertTrue(TaskPriority.HIGH.compareTo(TaskPriority.NORMAL) < 0);
        assertTrue(TaskPriority.NORMAL.compareTo(TaskPriority.LOW) < 0);
        assertEquals(0, TaskPriority.NORMAL.compareTo(TaskPriority.NORMAL));
    }

    @Test
    @DisplayName("불변 객체 - 원본 유지")
    void immutability() {
        // given
        Task original = createSampleTask();

        // when
        Task assigned = original.assignTo("robot-001");
        Task started = assigned.start();
        Task completed = started.complete();

        // then
        assertEquals(TaskStatus.PENDING, original.getStatus());
        assertEquals(TaskStatus.ASSIGNED, assigned.getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, started.getStatus());
        assertEquals(TaskStatus.COMPLETED, completed.getStatus());
    }

    // 테스트용 샘플 Task 생성 헬퍼 메서드
    private Task createSampleTask() {
        return Task.builder()
                .taskType(Task.TaskType.PATROL)
                .priority(TaskPriority.NORMAL)
                .targetZoneId("zone-001")
                .targetLocation(new Location(10.0, 20.0))
                .description("정기 순찰")
                .build();
    }
}
