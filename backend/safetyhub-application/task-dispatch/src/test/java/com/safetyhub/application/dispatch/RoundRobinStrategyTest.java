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

@DisplayName("RoundRobinStrategy 테스트")
class RoundRobinStrategyTest {

    private RoundRobinStrategy strategy;
    private List<DispatchStrategy.RobotInfo> robots;

    @BeforeEach
    void setUp() {
        strategy = new RoundRobinStrategy();
        robots = createTestRobots(3);
    }

    @Test
    @DisplayName("전략 정보 조회")
    void getStrategyInfo() {
        // when & then
        assertEquals("Round-Robin", strategy.getName());
        assertNotNull(strategy.getDescription());
    }

    @Test
    @DisplayName("Round-Robin 순서대로 할당")
    void assignInRoundRobinOrder() {
        // given
        Task task1 = createTask();
        Task task2 = createTask();
        Task task3 = createTask();
        Task task4 = createTask();

        // when
        String robot1 = strategy.assignTask(task1, robots).orElse(null);
        String robot2 = strategy.assignTask(task2, robots).orElse(null);
        String robot3 = strategy.assignTask(task3, robots).orElse(null);
        String robot4 = strategy.assignTask(task4, robots).orElse(null);

        // then
        assertEquals("robot-001", robot1);
        assertEquals("robot-002", robot2);
        assertEquals("robot-003", robot3);
        assertEquals("robot-001", robot4); // 순환
    }

    @Test
    @DisplayName("사용 가능한 로봇만 선택")
    void assignOnlyAvailableRobots() {
        // given
        List<DispatchStrategy.RobotInfo> mixedRobots = new ArrayList<>();
        mixedRobots.add(createRobotInfo("robot-001", "WORKING", 50, 0)); // 사용 가능
        mixedRobots.add(createRobotInfo("robot-002", "OFFLINE", 50, 0)); // 사용 불가
        mixedRobots.add(createRobotInfo("robot-003", "WORKING", 10, 0)); // 배터리 부족
        mixedRobots.add(createRobotInfo("robot-004", "WORKING", 50, 0)); // 사용 가능

        Task task1 = createTask();
        Task task2 = createTask();
        Task task3 = createTask();

        // when
        String robot1 = strategy.assignTask(task1, mixedRobots).orElse(null);
        String robot2 = strategy.assignTask(task2, mixedRobots).orElse(null);
        String robot3 = strategy.assignTask(task3, mixedRobots).orElse(null);

        // then
        assertEquals("robot-001", robot1);
        assertEquals("robot-004", robot2);
        assertEquals("robot-001", robot3); // 순환
    }

    @Test
    @DisplayName("사용 가능한 로봇이 없으면 빈 Optional 반환")
    void noAvailableRobots() {
        // given
        List<DispatchStrategy.RobotInfo> unavailableRobots = new ArrayList<>();
        unavailableRobots.add(createRobotInfo("robot-001", "OFFLINE", 50, 0));
        unavailableRobots.add(createRobotInfo("robot-002", "EMERGENCY", 50, 0));

        Task task = createTask();

        // when
        Optional<String> result = strategy.assignTask(task, unavailableRobots);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("빈 로봇 목록에서 빈 Optional 반환")
    void emptyRobotList() {
        // given
        Task task = createTask();

        // when
        Optional<String> result = strategy.assignTask(task, new ArrayList<>());

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("null 로봇 목록에서 빈 Optional 반환")
    void nullRobotList() {
        // given
        Task task = createTask();

        // when
        Optional<String> result = strategy.assignTask(task, null);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("null 작업에서 빈 Optional 반환")
    void nullTask() {
        // when
        Optional<String> result = strategy.assignTask(null, robots);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("인덱스 리셋")
    void resetIndex() {
        // given
        strategy.assignTask(createTask(), robots);
        strategy.assignTask(createTask(), robots);
        assertEquals(1, strategy.getCurrentIndex());

        // when
        strategy.reset();

        // then
        assertEquals(-1, strategy.getCurrentIndex());

        // 다음 할당은 첫 번째 로봇부터 시작
        String nextRobot = strategy.assignTask(createTask(), robots).orElse(null);
        assertEquals("robot-001", nextRobot);
    }

    @Test
    @DisplayName("스레드 안전성")
    void threadSafety() throws InterruptedException {
        // given
        int threadCount = 10;
        int tasksPerThread = 10;
        Thread[] threads = new Thread[threadCount];
        List<String> results = new ArrayList<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < tasksPerThread; j++) {
                    strategy.assignTask(createTask(), robots).ifPresent(results::add);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // then
        assertEquals(threadCount * tasksPerThread, results.size());
        // 모든 로봇이 공평하게 할당되었는지 확인
        long robot1Count = results.stream().filter(r -> r.equals("robot-001")).count();
        long robot2Count = results.stream().filter(r -> r.equals("robot-002")).count();
        long robot3Count = results.stream().filter(r -> r.equals("robot-003")).count();

        // 각 로봇에 약 33개씩 할당되어야 함 (오차 범위 ±5)
        assertEquals(33, robot1Count, 5);
        assertEquals(33, robot2Count, 5);
        assertEquals(33, robot3Count, 5);
    }

    // 테스트 헬퍼 메서드
    private List<DispatchStrategy.RobotInfo> createTestRobots(int count) {
        List<DispatchStrategy.RobotInfo> robotList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            robotList.add(createRobotInfo(
                    String.format("robot-%03d", i),
                    "WORKING",
                    80,
                    0
            ));
        }
        return robotList;
    }

    private DispatchStrategy.RobotInfo createRobotInfo(String robotId, String state, int battery, int taskCount) {
        return SimpleRobotInfo.builder()
                .robotId(robotId)
                .name("Robot " + robotId)
                .state(state)
                .currentLocation(new Location(10.0, 20.0))
                .currentZoneId("zone-001")
                .batteryLevel(battery)
                .assignedTaskCount(taskCount)
                .build();
    }

    private Task createTask() {
        return Task.builder()
                .taskType(Task.TaskType.PATROL)
                .priority(TaskPriority.NORMAL)
                .targetZoneId("zone-001")
                .targetLocation(new Location(30.0, 40.0))
                .description("테스트 작업")
                .build();
    }
}
