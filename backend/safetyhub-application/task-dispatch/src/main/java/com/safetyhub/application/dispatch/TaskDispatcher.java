package com.safetyhub.application.dispatch;

import com.safetyhub.core.domain.Task;
import com.safetyhub.core.domain.TaskPriority;
import com.safetyhub.core.domain.TaskStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 작업 분배 서비스
 * 작업 큐를 관리하고 분배 전략에 따라 로봇에 작업을 할당
 *
 * 주요 기능:
 * - 작업 제출 및 큐 관리
 * - 분배 전략에 따른 작업 할당
 * - 작업 상태 추적
 * - 통계 수집
 */
@Slf4j
public class TaskDispatcher {

    /**
     * 작업 대기열
     */
    private final TaskQueue taskQueue;

    /**
     * 분배 전략
     */
    private final DispatchStrategy dispatchStrategy;

    /**
     * 할당된 작업 추적 (Task ID -> Task)
     */
    private final Map<String, Task> assignedTasks;

    /**
     * 완료된 작업 추적 (Task ID -> Task)
     */
    private final Map<String, Task> completedTasks;

    /**
     * 통계
     */
    private final Statistics statistics;

    /**
     * 생성자
     * @param taskQueue 작업 대기열
     * @param dispatchStrategy 분배 전략
     */
    public TaskDispatcher(TaskQueue taskQueue, DispatchStrategy dispatchStrategy) {
        if (taskQueue == null) {
            throw new IllegalArgumentException("taskQueue는 null일 수 없습니다");
        }
        if (dispatchStrategy == null) {
            throw new IllegalArgumentException("dispatchStrategy는 null일 수 없습니다");
        }

        this.taskQueue = taskQueue;
        this.dispatchStrategy = dispatchStrategy;
        this.assignedTasks = new ConcurrentHashMap<>();
        this.completedTasks = new ConcurrentHashMap<>();
        this.statistics = new Statistics();

        log.info("TaskDispatcher 초기화 완료. 전략: {}", dispatchStrategy.getName());
    }

    /**
     * 작업 제출
     * @param task 제출할 작업
     */
    public void submitTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("작업은 null일 수 없습니다");
        }

        taskQueue.enqueue(task);
        statistics.incrementSubmitted();

        log.info("작업 제출됨. Task ID: {}, Type: {}, Priority: {}, Queue Size: {}",
                task.getId(), task.getTaskType(), task.getPriority(), taskQueue.size());
    }

    /**
     * 작업 할당
     * 큐에서 가장 높은 우선순위의 작업을 꺼내 로봇에 할당
     *
     * @param availableRobots 사용 가능한 로봇 목록
     * @return 할당된 작업, 할당할 작업이 없으면 Optional.empty()
     */
    public Optional<Task> dispatch(List<DispatchStrategy.RobotInfo> availableRobots) {
        // 입력 검증
        if (availableRobots == null || availableRobots.isEmpty()) {
            log.debug("사용 가능한 로봇이 없습니다");
            return Optional.empty();
        }

        // 큐에서 작업 꺼내기
        Optional<Task> taskOpt = taskQueue.dequeue();
        if (taskOpt.isEmpty()) {
            log.debug("큐에 작업이 없습니다");
            return Optional.empty();
        }

        Task task = taskOpt.get();

        // 분배 전략에 따라 로봇 선택
        Optional<String> robotIdOpt = dispatchStrategy.assignTask(task, availableRobots);
        if (robotIdOpt.isEmpty()) {
            log.warn("작업을 할당할 로봇을 찾을 수 없습니다. Task ID: {}, 큐에 다시 추가", task.getId());
            taskQueue.enqueue(task); // 다시 큐에 넣기
            statistics.incrementFailed();
            return Optional.empty();
        }

        // 작업 할당
        String robotId = robotIdOpt.get();
        Task assignedTask = task.assignTo(robotId);
        assignedTasks.put(assignedTask.getId(), assignedTask);
        statistics.incrementAssigned();

        log.info("작업 할당됨. Task ID: {}, Robot: {}, Priority: {}, Strategy: {}",
                assignedTask.getId(), robotId, assignedTask.getPriority(), dispatchStrategy.getName());

        return Optional.of(assignedTask);
    }

    /**
     * 작업 시작
     * @param taskId 시작할 작업 ID
     * @return 시작된 작업, 없으면 Optional.empty()
     */
    public Optional<Task> startTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("작업 ID는 null이거나 빈 문자열일 수 없습니다");
        }

        Task assignedTask = assignedTasks.get(taskId);
        if (assignedTask == null) {
            log.warn("할당된 작업을 찾을 수 없습니다. Task ID: {}", taskId);
            return Optional.empty();
        }

        Task startedTask = assignedTask.start();
        assignedTasks.put(taskId, startedTask);

        log.info("작업 시작됨. Task ID: {}, Robot: {}", taskId, startedTask.getAssignedRobotId());

        return Optional.of(startedTask);
    }

    /**
     * 작업 완료
     * @param taskId 완료할 작업 ID
     * @return 완료된 작업, 없으면 Optional.empty()
     */
    public Optional<Task> completeTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("작업 ID는 null이거나 빈 문자열일 수 없습니다");
        }

        Task assignedTask = assignedTasks.remove(taskId);
        if (assignedTask == null) {
            log.warn("완료할 작업을 찾을 수 없습니다. Task ID: {}", taskId);
            return Optional.empty();
        }

        Task completedTask = assignedTask.complete();
        completedTasks.put(taskId, completedTask);
        statistics.incrementCompleted();

        log.info("작업 완료됨. Task ID: {}, Robot: {}, 대기시간: {}초, 실행시간: {}초",
                taskId, completedTask.getAssignedRobotId(),
                completedTask.getWaitingTimeSeconds(),
                completedTask.getExecutionTimeSeconds());

        return Optional.of(completedTask);
    }

    /**
     * 작업 실패
     * @param taskId 실패한 작업 ID
     * @param reason 실패 사유
     * @return 실패 처리된 작업, 없으면 Optional.empty()
     */
    public Optional<Task> failTask(String taskId, String reason) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("작업 ID는 null이거나 빈 문자열일 수 없습니다");
        }

        Task assignedTask = assignedTasks.remove(taskId);
        if (assignedTask == null) {
            log.warn("실패 처리할 작업을 찾을 수 없습니다. Task ID: {}", taskId);
            return Optional.empty();
        }

        Task failedTask = assignedTask.fail(reason);
        completedTasks.put(taskId, failedTask);
        statistics.incrementFailed();

        log.warn("작업 실패. Task ID: {}, Robot: {}, Reason: {}",
                taskId, failedTask.getAssignedRobotId(), reason);

        return Optional.of(failedTask);
    }

    /**
     * 작업 취소
     * @param taskId 취소할 작업 ID
     * @return 취소된 작업, 없으면 Optional.empty()
     */
    public Optional<Task> cancelTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("작업 ID는 null이거나 빈 문자열일 수 없습니다");
        }

        // 큐에서 제거 시도
        Optional<Task> queuedTask = taskQueue.remove(taskId);
        if (queuedTask.isPresent()) {
            Task cancelledTask = queuedTask.get().cancel();
            completedTasks.put(taskId, cancelledTask);
            statistics.incrementCancelled();
            log.info("대기 중인 작업 취소됨. Task ID: {}", taskId);
            return Optional.of(cancelledTask);
        }

        // 할당된 작업에서 제거 시도
        Task assignedTask = assignedTasks.remove(taskId);
        if (assignedTask != null) {
            Task cancelledTask = assignedTask.cancel();
            completedTasks.put(taskId, cancelledTask);
            statistics.incrementCancelled();
            log.info("할당된 작업 취소됨. Task ID: {}, Robot: {}", taskId, assignedTask.getAssignedRobotId());
            return Optional.of(cancelledTask);
        }

        log.warn("취소할 작업을 찾을 수 없습니다. Task ID: {}", taskId);
        return Optional.empty();
    }

    /**
     * 큐에 대기 중인 작업 조회
     */
    public List<Task> getPendingTasks() {
        return taskQueue.getAllTasks();
    }

    /**
     * 할당된 작업 조회
     */
    public List<Task> getAssignedTasks() {
        return List.copyOf(assignedTasks.values());
    }

    /**
     * 우선순위별 대기 작업 조회
     */
    public List<Task> getPendingTasksByPriority(TaskPriority priority) {
        return taskQueue.getTasksByPriority(priority);
    }

    /**
     * 통계 조회
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * 큐 크기 조회
     */
    public int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * 할당된 작업 수 조회
     */
    public int getAssignedTaskCount() {
        return assignedTasks.size();
    }

    /**
     * 통계 클래스
     */
    @Getter
    public static class Statistics {
        private final AtomicLong submitted = new AtomicLong(0);
        private final AtomicLong assigned = new AtomicLong(0);
        private final AtomicLong completed = new AtomicLong(0);
        private final AtomicLong failed = new AtomicLong(0);
        private final AtomicLong cancelled = new AtomicLong(0);

        void incrementSubmitted() {
            submitted.incrementAndGet();
        }

        void incrementAssigned() {
            assigned.incrementAndGet();
        }

        void incrementCompleted() {
            completed.incrementAndGet();
        }

        void incrementFailed() {
            failed.incrementAndGet();
        }

        void incrementCancelled() {
            cancelled.incrementAndGet();
        }

        /**
         * 성공률 계산 (완료 / (완료 + 실패) * 100)
         */
        public double getSuccessRate() {
            long total = completed.get() + failed.get();
            if (total == 0) {
                return 0.0;
            }
            return (double) completed.get() / total * 100.0;
        }

        /**
         * 할당률 계산 (할당 / 제출 * 100)
         */
        public double getAssignmentRate() {
            long totalSubmitted = submitted.get();
            if (totalSubmitted == 0) {
                return 0.0;
            }
            return (double) assigned.get() / totalSubmitted * 100.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "Statistics{submitted=%d, assigned=%d, completed=%d, failed=%d, cancelled=%d, " +
                    "successRate=%.2f%%, assignmentRate=%.2f%%}",
                    submitted.get(), assigned.get(), completed.get(), failed.get(), cancelled.get(),
                    getSuccessRate(), getAssignmentRate()
            );
        }
    }
}
