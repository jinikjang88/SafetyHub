package com.safetyhub.simulator.core;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 로봇 작업자의 일일 스케줄
 */
public class RobotSchedule {
    private final List<ScheduleEntry> entries = new ArrayList<>();

    public void addEntry(LocalTime startTime, LocalTime endTime, RobotState state, String zoneId) {
        entries.add(new ScheduleEntry(startTime, endTime, state, zoneId));
    }

    public ScheduleEntry getCurrentEntry(LocalTime currentTime) {
        for (ScheduleEntry entry : entries) {
            if (isTimeBetween(currentTime, entry.getStartTime(), entry.getEndTime())) {
                return entry;
            }
        }
        return null;
    }

    public RobotState getScheduledState(LocalTime currentTime) {
        ScheduleEntry entry = getCurrentEntry(currentTime);
        return entry != null ? entry.getState() : RobotState.IDLE;
    }

    public String getScheduledZone(LocalTime currentTime) {
        ScheduleEntry entry = getCurrentEntry(currentTime);
        return entry != null ? entry.getZoneId() : null;
    }

    private boolean isTimeBetween(LocalTime time, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        } else {
            // 자정을 넘어가는 경우
            return !time.isBefore(start) || time.isBefore(end);
        }
    }

    public List<ScheduleEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public static class ScheduleEntry {
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final RobotState state;
        private final String zoneId;

        public ScheduleEntry(LocalTime startTime, LocalTime endTime, RobotState state, String zoneId) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.state = state;
            this.zoneId = zoneId;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public RobotState getState() {
            return state;
        }

        public String getZoneId() {
            return zoneId;
        }
    }

    public static RobotSchedule createDefaultSchedule() {
        RobotSchedule schedule = new RobotSchedule();
        // 오전 작업
        schedule.addEntry(LocalTime.of(8, 0), LocalTime.of(12, 0), RobotState.WORKING, "ZONE_A");
        // 점심 시간
        schedule.addEntry(LocalTime.of(12, 0), LocalTime.of(13, 0), RobotState.EATING, "ZONE_F");
        // 오후 작업
        schedule.addEntry(LocalTime.of(13, 0), LocalTime.of(15, 0), RobotState.WORKING, "ZONE_B");
        // 휴식
        schedule.addEntry(LocalTime.of(15, 0), LocalTime.of(15, 30), RobotState.RESTING, "ZONE_E");
        // 오후 작업 계속
        schedule.addEntry(LocalTime.of(15, 30), LocalTime.of(18, 0), RobotState.WORKING, "ZONE_C");
        return schedule;
    }
}
