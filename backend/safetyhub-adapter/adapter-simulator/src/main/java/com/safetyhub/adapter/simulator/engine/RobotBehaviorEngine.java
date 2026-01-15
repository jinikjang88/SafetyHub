package com.safetyhub.adapter.simulator.engine;

import com.safetyhub.adapter.simulator.robot.*;
import com.safetyhub.adapter.simulator.world.SimulationZone;
import com.safetyhub.adapter.simulator.world.VirtualWorld;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

/**
 * 로봇 행동 엔진
 * 스케줄에 따라 로봇의 행동을 결정하고 실행
 */
@Slf4j
@RequiredArgsConstructor
public class RobotBehaviorEngine {

    private final VirtualWorld world;
    private final Random random = new Random();

    // 긴급 상황 발생 확률 (틱당)
    private static final double EMERGENCY_PROBABILITY = 0.0001;

    // 건강 이상 발생 확률 (틱당)
    private static final double HEALTH_ISSUE_PROBABILITY = 0.0005;

    /**
     * 로봇의 다음 행동 결정 및 실행
     *
     * @param robot       대상 로봇
     * @param currentTime 현재 시뮬레이션 시간
     * @return 업데이트된 로봇
     */
    public RobotWorker tick(RobotWorker robot, LocalTime currentTime) {
        // 오프라인 상태면 무시
        if (robot.getState() == RobotState.OFFLINE) {
            return robot;
        }

        // 긴급 상황 중이면 유지
        if (robot.getState() == RobotState.EMERGENCY) {
            return robot;
        }

        // 대피 중이면 대피 로직 수행
        if (robot.getState() == RobotState.EVACUATING) {
            return handleEvacuation(robot);
        }

        // 랜덤 긴급 상황 발생 체크
        if (random.nextDouble() < EMERGENCY_PROBABILITY) {
            log.warn("Random emergency triggered for robot: {}", robot.getRobotId());
            return robot.triggerEmergency();
        }

        // 랜덤 건강 이상 발생 체크
        if (random.nextDouble() < HEALTH_ISSUE_PROBABILITY) {
            RobotWorker updated = robot.updateHealth(HealthStatus.createDanger());
            if (updated.getHealthStatus().getLevel() == HealthStatus.HealthLevel.CRITICAL) {
                log.warn("Critical health issue for robot: {}", robot.getRobotId());
                return updated.triggerEmergency();
            }
            return updated;
        }

        // 이동 중이면 이동 처리
        if (robot.getState() == RobotState.MOVING && robot.needsToMove()) {
            return handleMovement(robot);
        }

        // 스케줄에 따른 상태 결정
        RobotState scheduledState = robot.getScheduledState(currentTime);
        if (scheduledState == null || scheduledState == RobotState.OFFLINE) {
            return robot.updateState(RobotState.OFFLINE);
        }

        // 현재 상태와 스케줄 상태가 다르면 상태 변경
        if (robot.getState() != scheduledState && robot.getState() != RobotState.MOVING) {
            return handleStateChange(robot, scheduledState);
        }

        // 상태별 행동 수행
        return performStateAction(robot, scheduledState);
    }

    /**
     * 상태 변경 처리
     */
    private RobotWorker handleStateChange(RobotWorker robot, RobotState newState) {
        ZoneType targetZoneType = getTargetZoneType(newState);
        if (targetZoneType == null) {
            return robot.updateState(newState);
        }

        // 목표 구역 찾기
        SimulationZone targetZone = world.findNearestZone(
                robot.getCurrentPosition(), targetZoneType);

        if (targetZone == null) {
            log.warn("No zone found for type: {}", targetZoneType);
            return robot.updateState(newState);
        }

        // 이미 목표 구역에 있으면 상태만 변경
        if (targetZone.getZoneId().equals(robot.getCurrentZoneId())) {
            return robot.updateState(newState);
        }

        // 이동 시작
        Position target = targetZone.getRandomPosition();
        log.debug("Robot {} moving to {} for state {}", robot.getRobotId(), target, newState);
        return robot.setTarget(target);
    }

    /**
     * 이동 처리
     */
    private RobotWorker handleMovement(RobotWorker robot) {
        if (robot.getTargetPosition() == null) {
            return robot;
        }

        // 경로 계산
        List<Position> path = world.findPath(
                robot.getCurrentPosition(), robot.getTargetPosition());

        if (path.isEmpty()) {
            // 목표에 도달했거나 경로 없음
            if (robot.hasReachedTarget()) {
                RobotState scheduledState = robot.getScheduledState(LocalTime.now());
                return robot.updateState(scheduledState != null ? scheduledState : RobotState.WORKING);
            }
            log.warn("No path found for robot: {} to {}", robot.getRobotId(), robot.getTargetPosition());
            return robot.updateState(RobotState.WORKING);
        }

        // 한 칸 이동
        Position nextPosition = path.get(0);
        String newZoneId = world.findZoneAtPosition(nextPosition);

        RobotWorker moved = robot.updatePosition(nextPosition, newZoneId);
        world.updateRobotPosition(robot.getRobotId(), nextPosition);

        // 목표 도달 확인
        if (moved.hasReachedTarget()) {
            RobotState scheduledState = moved.getScheduledState(LocalTime.now());
            return moved.updateState(scheduledState != null ? scheduledState : RobotState.WORKING);
        }

        return moved;
    }

    /**
     * 대피 처리
     */
    private RobotWorker handleEvacuation(RobotWorker robot) {
        if (robot.getTargetPosition() == null) {
            // 대피소 위치 설정
            SimulationZone assemblyPoint = world.getAssemblyPoint();
            if (assemblyPoint != null) {
                return robot.startEvacuation(assemblyPoint.getCenterPosition());
            }
            return robot;
        }

        // 대피소로 이동
        List<Position> path = world.findPath(
                robot.getCurrentPosition(), robot.getTargetPosition());

        if (path.isEmpty() || robot.hasReachedTarget()) {
            // 대피 완료
            log.info("Robot {} reached assembly point", robot.getRobotId());
            return robot.updateState(RobotState.RESTING);
        }

        // 한 칸 이동 (대피 시 빠르게 2칸씩)
        int steps = Math.min(2, path.size());
        Position nextPosition = path.get(steps - 1);
        String newZoneId = world.findZoneAtPosition(nextPosition);

        RobotWorker moved = robot.updatePosition(nextPosition, newZoneId);
        world.updateRobotPosition(robot.getRobotId(), nextPosition);

        return moved;
    }

    /**
     * 상태별 행동 수행
     */
    private RobotWorker performStateAction(RobotWorker robot, RobotState state) {
        return switch (state) {
            case WORKING -> handleWorking(robot);
            case RESTING -> handleResting(robot);
            case EATING -> handleEating(robot);
            default -> robot;
        };
    }

    /**
     * 작업 중 행동
     */
    private RobotWorker handleWorking(RobotWorker robot) {
        // 건강 상태 업데이트 (작업 중)
        HealthStatus newHealth = HealthStatus.createWorking();

        // 배터리 소모
        BatteryStatus newBattery = robot.getBatteryStatus().drainWorking(1);

        // 구역 내 랜덤 이동 (가끔)
        if (random.nextDouble() < 0.1) {
            SimulationZone currentZone = world.getZones().get(robot.getCurrentZoneId());
            if (currentZone != null) {
                Position newPos = currentZone.getRandomPosition();
                if (world.getGridMap().isWalkable(newPos)) {
                    robot = robot.updatePosition(newPos, robot.getCurrentZoneId());
                    world.updateRobotPosition(robot.getRobotId(), newPos);
                }
            }
        }

        return robot.updateHealth(newHealth).updateBattery(newBattery);
    }

    /**
     * 휴식 중 행동
     */
    private RobotWorker handleResting(RobotWorker robot) {
        // 건강 상태 회복
        HealthStatus newHealth = HealthStatus.createNormal();

        // 배터리 소모 (대기)
        BatteryStatus newBattery = robot.getBatteryStatus().drainIdle(1);

        return robot.updateHealth(newHealth).updateBattery(newBattery);
    }

    /**
     * 식사 중 행동
     */
    private RobotWorker handleEating(RobotWorker robot) {
        // 건강 상태 유지
        HealthStatus newHealth = HealthStatus.createNormal();

        // 배터리 소모 (대기)
        BatteryStatus newBattery = robot.getBatteryStatus().drainIdle(1);

        return robot.updateHealth(newHealth).updateBattery(newBattery);
    }

    /**
     * 상태에 따른 목표 구역 타입 반환
     */
    private ZoneType getTargetZoneType(RobotState state) {
        return switch (state) {
            case WORKING -> ZoneType.WORK_AREA;
            case RESTING -> ZoneType.REST_AREA;
            case EATING -> ZoneType.CAFETERIA;
            case EVACUATING -> ZoneType.ASSEMBLY_POINT;
            default -> null;
        };
    }

    /**
     * 전체 로봇 대피 명령
     */
    public void triggerEvacuation() {
        SimulationZone assemblyPoint = world.getAssemblyPoint();
        if (assemblyPoint == null) {
            log.error("No assembly point found for evacuation!");
            return;
        }

        Position evacuationPoint = assemblyPoint.getCenterPosition();
        log.warn("EVACUATION ORDER - All robots moving to assembly point: {}", evacuationPoint);

        for (RobotWorker robot : world.getRobots().values()) {
            if (robot.isOnline() && robot.getState() != RobotState.EMERGENCY) {
                RobotWorker evacuating = robot.startEvacuation(evacuationPoint);
                world.getRobots().put(robot.getRobotId(), evacuating);
            }
        }
    }
}
