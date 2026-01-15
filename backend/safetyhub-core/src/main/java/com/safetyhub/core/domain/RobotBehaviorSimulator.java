package com.safetyhub.core.domain;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 로봇 행동 시뮬레이터
 * 로봇의 행동을 시뮬레이션하고 상태를 관리
 */
public class RobotBehaviorSimulator {

    private final VirtualWorld world;
    private final Random random;

    // 각 상태별 목표 구역 매핑
    private static final Map<RobotWorker.RobotState, String> STATE_ZONE_MAP = new HashMap<>();

    static {
        STATE_ZONE_MAP.put(RobotWorker.RobotState.WORKING, null);  // 할당된 작업 구역
        STATE_ZONE_MAP.put(RobotWorker.RobotState.RESTING, "ZONE_E");  // 휴게실
        STATE_ZONE_MAP.put(RobotWorker.RobotState.EATING, "ZONE_F");   // 식당
        STATE_ZONE_MAP.put(RobotWorker.RobotState.EMERGENCY, "ZONE_G"); // 의무실
        STATE_ZONE_MAP.put(RobotWorker.RobotState.EVACUATING, "ZONE_H"); // 대피소
    }

    public RobotBehaviorSimulator(VirtualWorld world) {
        this.world = world;
        this.random = new Random();
    }

    /**
     * 모든 로봇의 행동을 시뮬레이션 (1 틱)
     */
    public void simulateTick(LocalTime currentTime) {
        world.getAllRobots().forEach(robot -> {
            // 스케줄에 따른 상태 확인
            RobotWorker.RobotState scheduledState = robot.getSchedule().getStateAtTime(currentTime);

            // 긴급 상황이 아니면 스케줄에 따라 상태 변경
            if (robot.getState() != RobotWorker.RobotState.EMERGENCY &&
                robot.getState() != RobotWorker.RobotState.EVACUATING) {

                if (robot.getState() != scheduledState) {
                    handleStateTransition(robot, scheduledState);
                }
            }

            // 현재 상태에 따른 행동 수행
            performBehavior(robot);

            // 배터리 소모
            if (robot.getState() != RobotWorker.RobotState.OFFLINE) {
                robot.getBattery().discharge();

                // 배터리 부족 시 휴식 구역으로 이동 (충전)
                if (robot.needsCharging() && robot.getState() != RobotWorker.RobotState.EMERGENCY) {
                    handleStateTransition(robot, RobotWorker.RobotState.RESTING);
                }
            }

            // 건강 상태 업데이트
            updateHealthStatus(robot);
        });
    }

    /**
     * 상태 전환 처리
     */
    private void handleStateTransition(RobotWorker robot, RobotWorker.RobotState newState) {
        robot.changeState(newState);

        // 목표 구역 결정
        String targetZoneId = getTargetZoneForState(robot, newState);

        if (targetZoneId != null && !targetZoneId.equals(robot.getCurrentZoneId())) {
            // 목표 구역으로 이동 시작
            startMovingToZone(robot, targetZoneId);
        }
    }

    /**
     * 상태에 따른 목표 구역 결정
     */
    private String getTargetZoneForState(RobotWorker robot, RobotWorker.RobotState state) {
        if (state == RobotWorker.RobotState.WORKING) {
            return robot.getAssignedZoneId();  // 할당된 작업 구역
        }
        return STATE_ZONE_MAP.get(state);
    }

    /**
     * 특정 구역으로 이동 시작
     */
    private void startMovingToZone(RobotWorker robot, String targetZoneId) {
        Zone targetZone = world.getMap().getZone(targetZoneId);
        if (targetZone == null) return;

        Location targetLocation = targetZone.getCenterLocation();
        if (targetLocation == null) return;

        // 이동 상태로 변경 (실제 경로는 A* 알고리즘으로 계산)
        robot.changeState(RobotWorker.RobotState.MOVING);

        // 간단한 직선 이동 시뮬레이션 (추후 A* 경로로 대체)
        // 여기서는 목표 위치를 plannedPath에 추가
        java.util.List<Location> path = new java.util.ArrayList<>();
        path.add(targetLocation);
        robot.setPath(path);
    }

    /**
     * 현재 상태에 따른 행동 수행
     */
    private void performBehavior(RobotWorker robot) {
        switch (robot.getState()) {
            case WORKING:
                performWorking(robot);
                break;
            case RESTING:
                performResting(robot);
                break;
            case EATING:
                performEating(robot);
                break;
            case MOVING:
                performMoving(robot);
                break;
            case EMERGENCY:
                performEmergency(robot);
                break;
            case EVACUATING:
                performEvacuating(robot);
                break;
            case OFFLINE:
                // 아무 것도 하지 않음
                break;
        }
    }

    /**
     * 작업 행동
     */
    private void performWorking(RobotWorker robot) {
        // 작업 중 랜덤하게 위치 변경 (작업장 내 이동)
        if (random.nextDouble() < 0.1) {  // 10% 확률로 위치 변경
            Location currentLocation = robot.getCurrentLocation();
            if (currentLocation != null) {
                // 약간의 랜덤 이동 (±2미터)
                Location newLocation = Location.builder()
                        .latitude(currentLocation.getLatitude() + (random.nextDouble() - 0.5) * 2)
                        .longitude(currentLocation.getLongitude() + (random.nextDouble() - 0.5) * 2)
                        .altitude(currentLocation.getAltitude())
                        .build();

                world.updateRobotLocation(robot.getRobotId(), newLocation);
            }
        }

        // 작업 중 긴급 상황 발생 (낮은 확률)
        if (random.nextDouble() < 0.0001) {  // 0.01% 확률
            robot.triggerEmergency();
        }
    }

    /**
     * 휴식 행동
     */
    private void performResting(RobotWorker robot) {
        // 휴게실에서 배터리 충전
        if ("ZONE_E".equals(robot.getCurrentZoneId())) {
            robot.getBattery().charge();
        }

        // 휴식 중 건강 상태 회복
        RobotWorker.HealthSimulation health = robot.getHealth();
        if (health.getStatus() == RobotWorker.HealthSimulation.HealthStatus.WARNING) {
            // 주의 상태에서 정상으로 회복
            health.setStatus(RobotWorker.HealthSimulation.HealthStatus.NORMAL);
        }
    }

    /**
     * 식사 행동
     */
    private void performEating(RobotWorker robot) {
        // 식사 중 배터리 충전 및 건강 회복
        if ("ZONE_F".equals(robot.getCurrentZoneId())) {
            robot.getBattery().charge();

            // 건강 상태 개선
            RobotWorker.HealthSimulation health = robot.getHealth();
            if (health.getHeartRate() > 80) {
                health.setHeartRate(health.getHeartRate() - 5);  // 심박수 안정화
            }
        }
    }

    /**
     * 이동 행동
     */
    private void performMoving(RobotWorker robot) {
        if (robot.getPlannedPath() == null || robot.getPlannedPath().isEmpty()) {
            // 경로가 없으면 목표 상태로 복귀
            robot.changeState(robot.getSchedule().getStateAtTime(LocalTime.now()));
            return;
        }

        // 목표 위치로 이동
        Location targetLocation = robot.getPlannedPath().get(0);
        Location currentLocation = robot.getCurrentLocation();

        if (currentLocation == null) {
            // 현재 위치가 없으면 목표 위치로 바로 이동
            world.updateRobotLocation(robot.getRobotId(), targetLocation);
            robot.getPlannedPath().remove(0);
            return;
        }

        // 목표 위치까지의 거리 계산
        double distance = currentLocation.distanceTo(targetLocation);
        double speed = robot.getSpeed() != null ? robot.getSpeed() : 1.0;  // 기본 속도 1m/s

        if (distance <= speed) {
            // 목표 위치 도달
            world.updateRobotLocation(robot.getRobotId(), targetLocation);
            robot.getPlannedPath().remove(0);

            // 모든 경로를 완료하면 목표 상태로 전환
            if (robot.getPlannedPath().isEmpty()) {
                RobotWorker.RobotState targetState = robot.getSchedule().getStateAtTime(LocalTime.now());
                robot.changeState(targetState);
            }
        } else {
            // 목표 방향으로 이동
            double ratio = speed / distance;
            double newLat = currentLocation.getLatitude() +
                    (targetLocation.getLatitude() - currentLocation.getLatitude()) * ratio;
            double newLon = currentLocation.getLongitude() +
                    (targetLocation.getLongitude() - currentLocation.getLongitude()) * ratio;

            Location newLocation = Location.builder()
                    .latitude(newLat)
                    .longitude(newLon)
                    .altitude(currentLocation.getAltitude())
                    .build();

            world.updateRobotLocation(robot.getRobotId(), newLocation);
        }
    }

    /**
     * 긴급 상황 행동
     */
    private void performEmergency(RobotWorker robot) {
        // 의무실로 이동
        if (!"ZONE_G".equals(robot.getCurrentZoneId())) {
            startMovingToZone(robot, "ZONE_G");
        } else {
            // 의무실에서 치료 중 (건강 상태 회복)
            RobotWorker.HealthSimulation health = robot.getHealth();
            if (health.getStatus() == RobotWorker.HealthSimulation.HealthStatus.DANGER) {
                // 위험 상태에서 주의 상태로 회복
                health.setStatus(RobotWorker.HealthSimulation.HealthStatus.WARNING);
            } else if (health.getStatus() == RobotWorker.HealthSimulation.HealthStatus.WARNING) {
                // 주의 상태에서 정상 상태로 회복
                health.setStatus(RobotWorker.HealthSimulation.HealthStatus.NORMAL);
                // 정상 상태가 되면 작업으로 복귀
                robot.changeState(RobotWorker.RobotState.WORKING);
            }
        }
    }

    /**
     * 대피 행동
     */
    private void performEvacuating(RobotWorker robot) {
        // 대피소로 이동
        if (!"ZONE_H".equals(robot.getCurrentZoneId())) {
            startMovingToZone(robot, "ZONE_H");
        }
        // 대피소에 도착하면 대기
    }

    /**
     * 건강 상태 업데이트
     */
    private void updateHealthStatus(RobotWorker robot) {
        RobotWorker.HealthSimulation health = robot.getHealth();

        // 작업 중 체력 소모 시뮬레이션
        if (robot.getState() == RobotWorker.RobotState.WORKING) {
            // 심박수 증가
            if (health.getHeartRate() < 100) {
                health.setHeartRate(health.getHeartRate() + (int) (random.nextDouble() * 2));
            }

            // 체온 상승
            if (health.getBodyTemperature() < 37.5) {
                health.setBodyTemperature(health.getBodyTemperature() + random.nextDouble() * 0.05);
            }

            // 위험 구역에서 건강 상태 악화
            if ("ZONE_C".equals(robot.getCurrentZoneId())) {
                if (random.nextDouble() < 0.001) {  // 0.1% 확률
                    health.setStatus(RobotWorker.HealthSimulation.HealthStatus.WARNING);
                }
            }
        }

        // 휴식/식사 중 체력 회복
        if (robot.getState() == RobotWorker.RobotState.RESTING ||
            robot.getState() == RobotWorker.RobotState.EATING) {

            // 심박수 감소
            if (health.getHeartRate() > 70) {
                health.setHeartRate(health.getHeartRate() - 1);
            }

            // 체온 정상화
            if (health.getBodyTemperature() > 36.5) {
                health.setBodyTemperature(health.getBodyTemperature() - 0.01);
            }
        }

        // 건강 이상 자동 감지
        if (health.getHeartRate() > 120 || health.getBodyTemperature() > 38.0) {
            if (health.getStatus() == RobotWorker.HealthSimulation.HealthStatus.NORMAL) {
                health.setStatus(RobotWorker.HealthSimulation.HealthStatus.WARNING);
            }
        }

        if (health.getHeartRate() > 140 || health.getBodyTemperature() > 39.0) {
            if (health.getStatus() != RobotWorker.HealthSimulation.HealthStatus.DANGER) {
                robot.triggerEmergency();
            }
        }
    }

    /**
     * 로봇 생성 헬퍼 메서드
     */
    public static RobotWorker createRobot(String robotId, String name, String assignedZoneId) {
        return RobotWorker.builder()
                .robotId(robotId)
                .name(name)
                .state(RobotWorker.RobotState.OFFLINE)
                .assignedZoneId(assignedZoneId)
                .schedule(RobotWorker.RobotSchedule.createDefaultSchedule())
                .health(RobotWorker.HealthSimulation.createNormal())
                .battery(RobotWorker.BatterySimulation.createFull())
                .speed(1.0)  // 1m/s
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }
}
