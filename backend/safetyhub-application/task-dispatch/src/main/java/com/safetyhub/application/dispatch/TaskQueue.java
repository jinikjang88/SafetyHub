package com.safetyhub.application.dispatch;

import com.safetyhub.core.domain.Task;
import com.safetyhub.core.domain.TaskPriority;

import java.util.List;
import java.util.Optional;

/**
 * 작업 대기열 인터페이스
 * 우선순위 기반으로 작업을 관리
 */
public interface TaskQueue {

    /**
     * 작업을 큐에 추가
     * @param task 추가할 작업
     * @throws IllegalArgumentException task가 null이거나 이미 할당된 작업인 경우
     */
    void enqueue(Task task);

    /**
     * 가장 높은 우선순위의 작업을 꺼냄 (제거)
     * 대기 시간에 따른 우선순위 승격을 고려
     * @return 가장 높은 우선순위의 작업, 큐가 비어있으면 Optional.empty()
     */
    Optional<Task> dequeue();

    /**
     * 가장 높은 우선순위의 작업을 조회 (제거하지 않음)
     * @return 가장 높은 우선순위의 작업, 큐가 비어있으면 Optional.empty()
     */
    Optional<Task> peek();

    /**
     * 특정 작업을 큐에서 제거
     * @param taskId 제거할 작업 ID
     * @return 제거된 작업, 없으면 Optional.empty()
     */
    Optional<Task> remove(String taskId);

    /**
     * 특정 우선순위의 모든 작업 조회
     * @param priority 조회할 우선순위
     * @return 해당 우선순위의 작업 목록 (읽기 전용)
     */
    List<Task> getTasksByPriority(TaskPriority priority);

    /**
     * 모든 작업 조회 (우선순위 순)
     * @return 모든 작업 목록 (읽기 전용)
     */
    List<Task> getAllTasks();

    /**
     * 큐의 크기
     * @return 대기 중인 작업 수
     */
    int size();

    /**
     * 큐가 비어있는지 확인
     * @return 비어있으면 true
     */
    boolean isEmpty();

    /**
     * 큐를 비움
     */
    void clear();

    /**
     * 특정 작업이 큐에 있는지 확인
     * @param taskId 확인할 작업 ID
     * @return 큐에 있으면 true
     */
    boolean contains(String taskId);
}
