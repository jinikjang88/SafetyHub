package com.safetyhub.application.dispatch;

import com.safetyhub.core.domain.Location;
import com.safetyhub.core.domain.Task;
import com.safetyhub.core.domain.TaskPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskDispatcher 통합 테스트")
class TaskDispatcherTest {

    private TaskDispatcher dispatcher;
    private List<DispatchStrategy.RobotInfo> robots;

    @BeforeEach
    void setUp() {
        TaskQueue taskQueue = new PriorityTaskQueue();
        DispatchStrategy strategy = new RoundRobinStrategy();
        dispatcher = new TaskDispatcher(taskQueue, strategy);
        robots = createTestRobots(3);
    }

    @Test
    @DisplayName("작업 제출")
    void submitTask() {
        // given
        Task task = createTask(TaskPriority.NORMAL);

        // when
        dispatcher.submitTask(task);

        // then
        assertEquals(1, dispatcher.getQueueSize());
        assertEquals(1, dispatcher.getStatistics().getSubmitted().get());
    }

    @Test
    @DisplayName("작업 할당")
    void dispatchTask() {
        // given
        Task task = createTask(TaskPriority.NORMAL);
        dispatcher.submitTask(task);

        // when
        Optional<Task> assigned = dispatcher.dispatch(robots);

        // then
        assertTrue(assigned.isPresent());
        assertEquals("robot-001", assigned.get().getAssignedRobotId());
        assertEquals(0, dispatcher.getQueueSize());
        assertEquals(1, dispatcher.getAssignedTaskCount());
        assertEquals(1, dispatcher.getStatistics().getAssigned().get());
    }

    @Test
    @DisplayName("여러 작업 Round-Robin 할당")
    void dispatchMultipleTasks() {
        // given
        dispatcher.submitTask(createTask(TaskPriority.NORMAL));
        dispatcher.submitTask(createTask(TaskPriority.NORMAL));
        dispatcher.submitTask(createTask(TaskPriority.NORMAL));

        // when
        Task task1 = dispatcher.dispatch(robots).orElse(null);
        Task task2 = dispatcher.dispatch(robots).orElse(null);
        Task task3 = dispatcher.dispatch(robots).orElse(null);

        // then
        assertNotNull(task1);
        assertNotNull(task2);
        assertNotNull(task3);
        assertEquals("robot-001", task1.getAssignedRobotId());
        assertEquals("robot-002", task2.getAssignedRobotId());
        assertEquals("robot-003", task3.getAssignedRobotId());
    }

    @Test
    @DisplayName("우선순위 순서대로 할당")
    void dispatchByPriority() {
        // given
        Task lowTask = createTask(TaskPriority.LOW);
        Task highTask = createTask(TaskPriority.HIGH);
        Task normalTask = createTask(TaskPriority.NORMAL);

        dispatcher.submitTask(lowTask);
        dispatcher.submitTask(highTask);
        dispatcher.submitTask(normalTask);

        // when
        Task first = dispatcher.dispatch(robots).orElse(null);
        Task second = dispatcher.dispatch(robots).orElse(null);
        Task third = dispatcher.dispatch(robots).orElse(null);

        // then
        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);
        assertEquals(highTask.getId(), first.getId());
        assertEquals(normalTask.getId(), second.getId());
        assertEquals(lowTask.getId(), third.getId());
    }

    @Test
    @DisplayName("작업 시작")
    void startTask() {
        // given
        Task task = createTask(TaskPriority.NORMAL);
        dispatcher.submitTask(task);
        Task assigned = dispatcher.dispatch(robots).orElse(null);
        assertNotNull(assigned);

        // when
        Optional<Task> started = dispatcher.startTask(assigned.getId());

        // then
        assertTrue(started.isPresent());
        assertEquals(com.safetyhub.core.domain.TaskStatus.IN_PROGRESS, started.get().getStatus());
    }

    @Test
    @DisplayName("작업 완료")
    void completeTask() {
        // given
        Task task = createTask(TaskPriority.NORMAL);
        dispatcher.submitTask(task);
        Task assigned = dispatcher.dispatch(robots).orElse(null);
        assertNotNull(assigned);
        dispatcher.startTask(assigned.getId());

        // when
        Optional<Task> completed = dispatcher.completeTask(assigned.getId());

        // then
        assertTrue(completed.isPresent());
        assertEquals(com.safetyhub.core.domain.TaskStatus.COMPLETED, completed.get().getStatus());
        assertEquals(0, dispatcher.getAssignedTaskCount());
        assertEquals(1, dispatcher.getStatistics().getCompleted().get());
    }

    @Test
    @DisplayName("작업 실패")
    void failTask() {
        // given
        Task task = createTask(TaskPriority.NORMAL);
        dispatcher.submitTask(task);
        Task assigned = dispatcher.dispatch(robots).orElse(null);
        assertNotNull(assigned);
        dispatcher.startTask(assigned.getId());

        // when
        Optional<Task> failed = dispatcher.failTask(assigned.getId(), "배터리 부족");

        // then
        assertTrue(failed.isPresent());
        assertEquals(com.safetyhub.core.domain.TaskStatus.FAILED, failed.get().getStatus());
        assertEquals("배터리 부족", failed.get().getFailureReason());
        assertEquals(0, dispatcher.getAssignedTaskCount());
        assertEquals(1, dispatcher.getStatistics().getFailed().get());
    }

    @Test
    @DisplayName("대기 중인 작업 취소")
    void cancelPendingTask() {
        // given
        Task task = createTask(TaskPriority.NORMAL);
        dispatcher.submitTask(task);

        // when
        Optional<Task> cancelled = dispatcher.cancelTask(task.getId());

        // then
        assertTrue(cancelled.isPresent());
        assertEquals(com.safetyhub.core.domain.TaskStatus.CANCELLED, cancelled.get().getStatus());
        assertEquals(0, dispatcher.getQueueSize());
        assertEquals(1, dispatcher.getStatistics().getCancelled().get());
    }

    @Test
    @DisplayName("할당된 작업 취소")
    void cancelAssignedTask() {
        // given
        Task task = createTask(TaskPriority.NORMAL);
        dispatcher.submitTask(task);
        Task assigned = dispatcher.dispatch(robots).orElse(null);
        assertNotNull(assigned);

        // when
        Optional<Task> cancelled = dispatcher.cancelTask(assigned.getId());

        // then
        assertTrue(cancelled.isPresent());
        assertEquals(com.safetyhub.core.domain.TaskStatus.CANCELLED, cancelled.get().getStatus());
        assertEquals(0, dispatcher.getAssignedTaskCount());
        assertEquals(1, dispatcher.getStatistics().getCancelled().get());
    }

    @Test
    @DisplayName("대기 작업 조회")
    void getPendingTasks() {
        // given
        dispatcher.submitTask(createTask(TaskPriority.NORMAL));
        dispatcher.submitTask(createTask(TaskPriority.HIGH));
        dispatcher.submitTask(createTask(TaskPriority.LOW));

        // when
        List<Task> pending = dispatcher.getPendingTasks();

        // then
        assertEquals(3, pending.size());
        // 우선순위 순으로 정렬됨
        assertEquals(TaskPriority.HIGH, pending.get(0).getPriority());
        assertEquals(TaskPriority.NORMAL, pending.get(1).getPriority());
        assertEquals(TaskPriority.LOW, pending.get(2).getPriority());
    }

    @Test
    @DisplayName("할당된 작업 조회")
    void getAssignedTasks() {
        // given
        dispatcher.submitTask(createTask(TaskPriority.NORMAL));
        dispatcher.submitTask(createTask(TaskPriority.NORMAL));
        dispatcher.dispatch(robots);
        dispatcher.dispatch(robots);

        // when
        List<Task> assigned = dispatcher.getAssignedTasks();

        // then
        assertEquals(2, assigned.size());
    }

    @Test
    @DisplayName("통계 - 성공률 계산")
    void statisticsSuccessRate() {
        // given
        for (int i = 0; i < 10; i++) {
            dispatcher.submitTask(createTask(TaskPriority.NORMAL));
        }

        // 7개 성공, 3개 실패
        for (int i = 0; i < 7; i++) {
            Task assigned = dispatcher.dispatch(robots).orElse(null);
            assertNotNull(assigned);
            dispatcher.startTask(assigned.getId());
            dispatcher.completeTask(assigned.getId());
        }

        for (int i = 0; i < 3; i++) {
            Task assigned = dispatcher.dispatch(robots).orElse(null);
            assertNotNull(assigned);
            dispatcher.startTask(assigned.getId());
            dispatcher.failTask(assigned.getId(), "테스트 실패");
        }

        // when
        double successRate = dispatcher.getStatistics().getSuccessRate();

        // then
        assertEquals(70.0, successRate, 0.01);
    }

    @Test
    @DisplayName("통계 - 할당률 계산")
    void statisticsAssignmentRate() {
        // given
        for (int i = 0; i < 10; i++) {
            dispatcher.submitTask(createTask(TaskPriority.NORMAL));
        }

        // 8개만 할당
        for (int i = 0; i < 8; i++) {
            dispatcher.dispatch(robots);
        }

        // when
        double assignmentRate = dispatcher.getStatistics().getAssignmentRate();

        // then
        assertEquals(80.0, assignmentRate, 0.01);
    }

    @Test
    @DisplayName("로봇이 없으면 할당 실패")
    void dispatchWithoutRobots() {
        // given
        dispatcher.submitTask(createTask(TaskPriority.NORMAL));

        // when
        Optional<Task> result = dispatcher.dispatch(new ArrayList<>());

        // then
        assertFalse(result.isPresent());
        assertEquals(1, dispatcher.getQueueSize()); // 큐에 다시 추가됨
    }

    // 테스트 헬퍼 메서드
    private List<DispatchStrategy.RobotInfo> createTestRobots(int count) {
        List<DispatchStrategy.RobotInfo> robotList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            robotList.add(SimpleRobotInfo.builder()
                    .robotId(String.format("robot-%03d", i))
                    .name("Robot " + i)
                    .state("WORKING")
                    .currentLocation(new Location(10.0, 20.0))
                    .currentZoneId("zone-001")
                    .batteryLevel(80)
                    .assignedTaskCount(0)
                    .build());
        }
        return robotList;
    }

    private Task createTask(TaskPriority priority) {
        return Task.builder()
                .taskType(Task.TaskType.PATROL)
                .priority(priority)
                .targetZoneId("zone-001")
                .targetLocation(new Location(30.0, 40.0))
                .description("테스트 작업")
                .build();
    }
}
