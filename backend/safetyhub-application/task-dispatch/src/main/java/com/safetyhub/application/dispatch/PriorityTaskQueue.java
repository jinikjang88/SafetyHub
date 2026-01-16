package com.safetyhub.application.dispatch;

import com.safetyhub.core.domain.Task;
import com.safetyhub.core.domain.TaskPriority;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * 우선순위 기반 작업 대기열 구현
 * 스레드 안전성을 보장하며, 대기 시간에 따른 우선순위 승격 지원
 */
@Slf4j
public class PriorityTaskQueue implements TaskQueue {

    /**
     * 우선순위 기반 블로킹 큐
     * 대기 시간을 고려한 우선순위로 정렬
     */
    private final PriorityBlockingQueue<Task> queue;

    /**
     * 빠른 조회를 위한 Task ID -> Task 매핑
     * ConcurrentHashMap으로 스레드 안전성 보장
     */
    private final Map<String, Task> taskMap;

    /**
     * 최대 큐 크기 (DoS 공격 방지)
     */
    private final int maxSize;

    /**
     * 기본 최대 큐 크기
     */
    private static final int DEFAULT_MAX_SIZE = 10000;

    /**
     * 기본 생성자 (최대 크기 10000)
     */
    public PriorityTaskQueue() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * 최대 크기를 지정하는 생성자
     * @param maxSize 최대 큐 크기
     */
    public PriorityTaskQueue(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("최대 크기는 0보다 커야 합니다");
        }
        this.maxSize = maxSize;
        this.queue = new PriorityBlockingQueue<>(100, new TaskComparator());
        this.taskMap = new ConcurrentHashMap<>();
    }

    @Override
    public void enqueue(Task task) {
        // 입력 검증
        if (task == null) {
            throw new IllegalArgumentException("작업(task)은 null일 수 없습니다");
        }
        if (!task.isAssignable()) {
            throw new IllegalArgumentException("PENDING 상태의 작업만 큐에 추가할 수 있습니다. 현재 상태: " + task.getStatus());
        }

        // 큐 크기 제한 (DoS 방지)
        if (size() >= maxSize) {
            log.error("큐가 최대 크기에 도달했습니다. 현재 크기: {}, 최대 크기: {}", size(), maxSize);
            throw new IllegalStateException("작업 큐가 가득 찼습니다. 최대 크기: " + maxSize);
        }

        // 중복 확인
        if (taskMap.containsKey(task.getId())) {
            log.warn("이미 큐에 존재하는 작업입니다. Task ID: {}", task.getId());
            throw new IllegalArgumentException("이미 큐에 존재하는 작업입니다. Task ID: " + task.getId());
        }

        // 큐에 추가
        queue.offer(task);
        taskMap.put(task.getId(), task);
        log.debug("작업 큐에 추가됨. Task ID: {}, Priority: {}, Queue Size: {}",
                task.getId(), task.getPriority(), size());
    }

    @Override
    public Optional<Task> dequeue() {
        Task task = queue.poll();
        if (task != null) {
            taskMap.remove(task.getId());
            log.debug("작업 큐에서 제거됨. Task ID: {}, Priority: {}, Queue Size: {}",
                    task.getId(), task.getPriority(), size());
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Task> peek() {
        return Optional.ofNullable(queue.peek());
    }

    @Override
    public Optional<Task> remove(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("작업 ID는 null이거나 빈 문자열일 수 없습니다");
        }

        Task task = taskMap.remove(taskId);
        if (task != null) {
            queue.remove(task);
            log.debug("작업 제거됨. Task ID: {}, Queue Size: {}", taskId, size());
        }
        return Optional.ofNullable(task);
    }

    @Override
    public List<Task> getTasksByPriority(TaskPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("우선순위는 null일 수 없습니다");
        }

        return taskMap.values().stream()
                .filter(task -> task.getEffectivePriority() == priority)
                .sorted(new TaskComparator())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Task> getAllTasks() {
        return taskMap.values().stream()
                .sorted(new TaskComparator())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public int size() {
        return taskMap.size();
    }

    @Override
    public boolean isEmpty() {
        return taskMap.isEmpty();
    }

    @Override
    public void clear() {
        queue.clear();
        taskMap.clear();
        log.debug("작업 큐가 비워졌습니다");
    }

    @Override
    public boolean contains(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return false;
        }
        return taskMap.containsKey(taskId);
    }

    /**
     * 작업 비교자
     * 1순위: 승격된 우선순위 (낮은 숫자가 높은 우선순위)
     * 2순위: 생성 시간 (오래된 작업 우선 - FIFO)
     */
    private static class TaskComparator implements Comparator<Task> {
        @Override
        public int compare(Task t1, Task t2) {
            // 1순위: 승격된 우선순위로 비교
            TaskPriority p1 = t1.getEffectivePriority();
            TaskPriority p2 = t2.getEffectivePriority();

            int priorityComparison = p1.compareTo(p2);
            if (priorityComparison != 0) {
                return priorityComparison;
            }

            // 2순위: 생성 시간으로 비교 (오래된 작업 우선 - FIFO)
            return t1.getCreatedAt().compareTo(t2.getCreatedAt());
        }
    }
}
