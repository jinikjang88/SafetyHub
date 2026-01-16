package com.safetyhub.application.dispatch;

import com.safetyhub.core.domain.Location;
import com.safetyhub.core.domain.Task;
import com.safetyhub.core.domain.TaskPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriorityTaskQueue 테스트")
class PriorityTaskQueueTest {

    private TaskQueue taskQueue;

    @BeforeEach
    void setUp() {
        taskQueue = new PriorityTaskQueue();
    }

    @Test
    @DisplayName("작업 추가 및 조회")
    void enqueueAndPeek() {
        // given
        Task task = createTask(TaskPriority.NORMAL);

        // when
        taskQueue.enqueue(task);

        // then
        assertEquals(1, taskQueue.size());
        assertFalse(taskQueue.isEmpty());
        assertTrue(taskQueue.contains(task.getId()));

        Optional<Task> peeked = taskQueue.peek();
        assertTrue(peeked.isPresent());
        assertEquals(task.getId(), peeked.get().getId());
        assertEquals(1, taskQueue.size()); // peek은 제거하지 않음
    }

    @Test
    @DisplayName("작업 꺼내기")
    void dequeue() {
        // given
        Task task1 = createTask(TaskPriority.NORMAL);
        Task task2 = createTask(TaskPriority.HIGH);

        taskQueue.enqueue(task1);
        taskQueue.enqueue(task2);

        // when
        Optional<Task> dequeued = taskQueue.dequeue();

        // then
        assertTrue(dequeued.isPresent());
        assertEquals(task2.getId(), dequeued.get().getId()); // HIGH 우선순위가 먼저
        assertEquals(1, taskQueue.size());
        assertFalse(taskQueue.contains(task2.getId()));
    }

    @Test
    @DisplayName("우선순위 순서대로 꺼내기")
    void dequeuePriorityOrder() {
        // given
        Task lowTask = createTask(TaskPriority.LOW);
        Task normalTask = createTask(TaskPriority.NORMAL);
        Task highTask = createTask(TaskPriority.HIGH);
        Task criticalTask = createTask(TaskPriority.CRITICAL);

        // 순서 섞어서 추가
        taskQueue.enqueue(normalTask);
        taskQueue.enqueue(lowTask);
        taskQueue.enqueue(criticalTask);
        taskQueue.enqueue(highTask);

        // when & then
        assertEquals(criticalTask.getId(), taskQueue.dequeue().get().getId());
        assertEquals(highTask.getId(), taskQueue.dequeue().get().getId());
        assertEquals(normalTask.getId(), taskQueue.dequeue().get().getId());
        assertEquals(lowTask.getId(), taskQueue.dequeue().get().getId());
        assertTrue(taskQueue.isEmpty());
    }

    @Test
    @DisplayName("같은 우선순위는 FIFO 순서")
    void samePriorityFIFO() {
        // given
        Task task1 = createTask(TaskPriority.NORMAL);
        Task task2 = createTask(TaskPriority.NORMAL);
        Task task3 = createTask(TaskPriority.NORMAL);

        // when
        taskQueue.enqueue(task1);
        taskQueue.enqueue(task2);
        taskQueue.enqueue(task3);

        // then
        assertEquals(task1.getId(), taskQueue.dequeue().get().getId());
        assertEquals(task2.getId(), taskQueue.dequeue().get().getId());
        assertEquals(task3.getId(), taskQueue.dequeue().get().getId());
    }

    @Test
    @DisplayName("빈 큐에서 dequeue")
    void dequeueEmptyQueue() {
        // when
        Optional<Task> dequeued = taskQueue.dequeue();

        // then
        assertFalse(dequeued.isPresent());
    }

    @Test
    @DisplayName("특정 작업 제거")
    void removeTask() {
        // given
        Task task1 = createTask(TaskPriority.NORMAL);
        Task task2 = createTask(TaskPriority.HIGH);
        Task task3 = createTask(TaskPriority.LOW);

        taskQueue.enqueue(task1);
        taskQueue.enqueue(task2);
        taskQueue.enqueue(task3);

        // when
        Optional<Task> removed = taskQueue.remove(task2.getId());

        // then
        assertTrue(removed.isPresent());
        assertEquals(task2.getId(), removed.get().getId());
        assertEquals(2, taskQueue.size());
        assertFalse(taskQueue.contains(task2.getId()));
    }

    @Test
    @DisplayName("존재하지 않는 작업 제거")
    void removeNonExistentTask() {
        // when
        Optional<Task> removed = taskQueue.remove("non-existent-id");

        // then
        assertFalse(removed.isPresent());
    }

    @Test
    @DisplayName("우선순위별 작업 조회")
    void getTasksByPriority() {
        // given
        Task normalTask1 = createTask(TaskPriority.NORMAL);
        Task normalTask2 = createTask(TaskPriority.NORMAL);
        Task highTask = createTask(TaskPriority.HIGH);

        taskQueue.enqueue(normalTask1);
        taskQueue.enqueue(highTask);
        taskQueue.enqueue(normalTask2);

        // when
        List<Task> normalTasks = taskQueue.getTasksByPriority(TaskPriority.NORMAL);
        List<Task> highTasks = taskQueue.getTasksByPriority(TaskPriority.HIGH);

        // then
        assertEquals(2, normalTasks.size());
        assertEquals(1, highTasks.size());
    }

    @Test
    @DisplayName("모든 작업 조회")
    void getAllTasks() {
        // given
        Task task1 = createTask(TaskPriority.LOW);
        Task task2 = createTask(TaskPriority.HIGH);
        Task task3 = createTask(TaskPriority.NORMAL);

        taskQueue.enqueue(task1);
        taskQueue.enqueue(task2);
        taskQueue.enqueue(task3);

        // when
        List<Task> allTasks = taskQueue.getAllTasks();

        // then
        assertEquals(3, allTasks.size());
        // 우선순위 순으로 정렬됨
        assertEquals(task2.getId(), allTasks.get(0).getId()); // HIGH
        assertEquals(task3.getId(), allTasks.get(1).getId()); // NORMAL
        assertEquals(task1.getId(), allTasks.get(2).getId()); // LOW
    }

    @Test
    @DisplayName("큐 비우기")
    void clearQueue() {
        // given
        taskQueue.enqueue(createTask(TaskPriority.NORMAL));
        taskQueue.enqueue(createTask(TaskPriority.HIGH));

        // when
        taskQueue.clear();

        // then
        assertTrue(taskQueue.isEmpty());
        assertEquals(0, taskQueue.size());
    }

    @Test
    @DisplayName("null 작업 추가 시 예외")
    void enqueueNull_ThrowsException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                taskQueue.enqueue(null)
        );
    }

    @Test
    @DisplayName("이미 할당된 작업 추가 시 예외")
    void enqueueAssignedTask_ThrowsException() {
        // given
        Task task = createTask(TaskPriority.NORMAL).assignTo("robot-001");

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                taskQueue.enqueue(task)
        );
    }

    @Test
    @DisplayName("중복 작업 추가 시 예외")
    void enqueueDuplicate_ThrowsException() {
        // given
        Task task = createTask(TaskPriority.NORMAL);
        taskQueue.enqueue(task);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                taskQueue.enqueue(task)
        );
    }

    @Test
    @DisplayName("최대 큐 크기 제한")
    void maxQueueSize() {
        // given
        TaskQueue smallQueue = new PriorityTaskQueue(3);
        smallQueue.enqueue(createTask(TaskPriority.NORMAL));
        smallQueue.enqueue(createTask(TaskPriority.NORMAL));
        smallQueue.enqueue(createTask(TaskPriority.NORMAL));

        // when & then
        assertThrows(IllegalStateException.class, () ->
                smallQueue.enqueue(createTask(TaskPriority.NORMAL))
        );
    }

    @Test
    @DisplayName("우선순위 승격 - 대기 시간에 따라")
    void priorityPromotion() throws InterruptedException {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(3);
        Task oldLowTask = Task.builder()
                .taskType(Task.TaskType.PATROL)
                .priority(TaskPriority.LOW)
                .targetZoneId("zone-001")
                .createdAt(pastTime)
                .build();

        Task newHighTask = createTask(TaskPriority.HIGH);

        // when
        taskQueue.enqueue(oldLowTask);
        taskQueue.enqueue(newHighTask);

        // then
        // LOW 작업이 2시간 이상 대기하여 NORMAL로 승격되었지만,
        // HIGH 작업이 여전히 더 높은 우선순위
        Optional<Task> first = taskQueue.dequeue();
        assertTrue(first.isPresent());
        assertEquals(newHighTask.getId(), first.get().getId());
    }

    @Test
    @DisplayName("스레드 안전성 - 동시 추가")
    void concurrentEnqueue() throws InterruptedException {
        // given
        int threadCount = 10;
        int tasksPerThread = 10;
        Thread[] threads = new Thread[threadCount];

        // when
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < tasksPerThread; j++) {
                    taskQueue.enqueue(createTask(TaskPriority.NORMAL));
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // then
        assertEquals(threadCount * tasksPerThread, taskQueue.size());
    }

    // 테스트용 Task 생성 헬퍼 메서드
    private Task createTask(TaskPriority priority) {
        return Task.builder()
                .taskType(Task.TaskType.PATROL)
                .priority(priority)
                .targetZoneId("zone-001")
                .targetLocation(new Location(10.0, 20.0))
                .description("테스트 작업")
                .build();
    }
}
