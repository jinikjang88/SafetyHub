package com.safetyhub.application.dispatch;

import com.safetyhub.core.domain.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-Robin 분배 전략
 * 로봇에 순차적으로 작업을 할당하는 가장 단순한 분배 방식
 *
 * 특징:
 * - 구현이 간단하고 예측 가능
 * - 모든 로봇에 공평하게 작업 분배
 * - 위치나 부하를 고려하지 않음
 * - 스레드 안전성 보장 (AtomicInteger 사용)
 *
 * 적합한 상황:
 * - 모든 로봇이 비슷한 위치에 있을 때
 * - 작업이 균일한 부하를 가질 때
 * - 간단한 분배 로직이 필요할 때
 */
@Slf4j
public class RoundRobinStrategy implements DispatchStrategy {

    /**
     * 마지막으로 할당한 로봇의 인덱스
     * AtomicInteger로 스레드 안전성 보장
     */
    private final AtomicInteger lastAssignedIndex = new AtomicInteger(-1);

    @Override
    public String getName() {
        return "Round-Robin";
    }

    @Override
    public String getDescription() {
        return "로봇에 순차적으로 작업을 할당합니다. 가장 단순하고 공평한 분배 방식입니다.";
    }

    @Override
    public Optional<String> assignTask(Task task, List<RobotInfo> availableRobots) {
        // 입력 검증
        if (task == null) {
            log.warn("작업이 null입니다");
            return Optional.empty();
        }

        if (availableRobots == null || availableRobots.isEmpty()) {
            log.warn("사용 가능한 로봇이 없습니다. Task ID: {}", task.getId());
            return Optional.empty();
        }

        // 사용 가능한 로봇만 필터링
        List<RobotInfo> activeRobots = availableRobots.stream()
                .filter(RobotInfo::isAvailable)
                .toList();

        if (activeRobots.isEmpty()) {
            log.warn("활성 상태의 로봇이 없습니다. Task ID: {}, Total Robots: {}",
                    task.getId(), availableRobots.size());
            return Optional.empty();
        }

        // Round-Robin 방식으로 다음 로봇 선택
        int nextIndex = getNextIndex(activeRobots.size());
        RobotInfo selectedRobot = activeRobots.get(nextIndex);

        log.debug("Round-Robin 할당 - Task ID: {}, Robot: {}, Index: {}/{}, Total Active: {}",
                task.getId(),
                selectedRobot.getRobotId(),
                nextIndex,
                activeRobots.size() - 1,
                activeRobots.size());

        return Optional.of(selectedRobot.getRobotId());
    }

    /**
     * 다음 할당할 로봇의 인덱스 계산
     * @param robotCount 로봇 수
     * @return 다음 인덱스 (0 ~ robotCount-1)
     */
    private int getNextIndex(int robotCount) {
        // AtomicInteger를 사용하여 스레드 안전하게 증가
        int current = lastAssignedIndex.updateAndGet(index -> {
            int next = index + 1;
            // 범위를 벗어나면 0으로 순환
            return next >= robotCount ? 0 : next;
        });

        return current;
    }

    /**
     * 인덱스 초기화 (테스트용)
     */
    public void reset() {
        lastAssignedIndex.set(-1);
        log.debug("Round-Robin 인덱스가 초기화되었습니다");
    }

    /**
     * 현재 인덱스 조회 (테스트용)
     */
    public int getCurrentIndex() {
        return lastAssignedIndex.get();
    }
}
