package com.safetyhub.adapter.simulator.robot;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

/**
 * 로봇 작업자 일일 스케줄
 */
@Getter
@Builder
public class RobotSchedule {

    private final List<ScheduleEntry> entries;

    /**
     * 현재 시간에 해당하는 스케줄 항목 반환
     */
    public ScheduleEntry getCurrentEntry(LocalTime currentTime) {
        return entries.stream()
                .filter(entry -> !currentTime.isBefore(entry.getStartTime())
                        && currentTime.isBefore(entry.getEndTime()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 기본 8시간 근무 스케줄 생성
     */
    public static RobotSchedule createDefaultSchedule() {
        return RobotSchedule.builder()
                .entries(List.of(
                        // 08:00-10:00 작업
                        ScheduleEntry.builder()
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(10, 0))
                                .activity(RobotState.WORKING)
                                .targetZoneType(ZoneType.WORK_AREA)
                                .build(),
                        // 10:00-10:15 휴식
                        ScheduleEntry.builder()
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(10, 15))
                                .activity(RobotState.RESTING)
                                .targetZoneType(ZoneType.REST_AREA)
                                .build(),
                        // 10:15-12:00 작업
                        ScheduleEntry.builder()
                                .startTime(LocalTime.of(10, 15))
                                .endTime(LocalTime.of(12, 0))
                                .activity(RobotState.WORKING)
                                .targetZoneType(ZoneType.WORK_AREA)
                                .build(),
                        // 12:00-13:00 식사
                        ScheduleEntry.builder()
                                .startTime(LocalTime.of(12, 0))
                                .endTime(LocalTime.of(13, 0))
                                .activity(RobotState.EATING)
                                .targetZoneType(ZoneType.CAFETERIA)
                                .build(),
                        // 13:00-15:00 작업
                        ScheduleEntry.builder()
                                .startTime(LocalTime.of(13, 0))
                                .endTime(LocalTime.of(15, 0))
                                .activity(RobotState.WORKING)
                                .targetZoneType(ZoneType.WORK_AREA)
                                .build(),
                        // 15:00-15:15 휴식
                        ScheduleEntry.builder()
                                .startTime(LocalTime.of(15, 0))
                                .endTime(LocalTime.of(15, 15))
                                .activity(RobotState.RESTING)
                                .targetZoneType(ZoneType.REST_AREA)
                                .build(),
                        // 15:15-17:00 작업
                        ScheduleEntry.builder()
                                .startTime(LocalTime.of(15, 15))
                                .endTime(LocalTime.of(17, 0))
                                .activity(RobotState.WORKING)
                                .targetZoneType(ZoneType.WORK_AREA)
                                .build()
                ))
                .build();
    }

    @Getter
    @Builder
    public static class ScheduleEntry {
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final RobotState activity;
        private final ZoneType targetZoneType;
    }
}
