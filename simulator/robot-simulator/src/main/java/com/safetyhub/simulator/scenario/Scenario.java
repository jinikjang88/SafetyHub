package com.safetyhub.simulator.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 시뮬레이션 시나리오 정의
 */
public class Scenario {
    private String id;
    private String name;
    private String description;
    private int robotCount;
    private int durationMinutes;
    private List<ScenarioEvent> events;
    private Map<String, Object> parameters;

    public Scenario() {
        this.events = new ArrayList<>();
    }

    public Scenario(String id, String name, String description, int robotCount, int durationMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.robotCount = robotCount;
        this.durationMinutes = durationMinutes;
        this.events = new ArrayList<>();
    }

    public void addEvent(ScenarioEvent event) {
        events.add(event);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getRobotCount() { return robotCount; }
    public void setRobotCount(int robotCount) { this.robotCount = robotCount; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public List<ScenarioEvent> getEvents() { return events; }
    public void setEvents(List<ScenarioEvent> events) { this.events = events; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public static class ScenarioEvent {
        private int triggerTimeSeconds;
        private String eventType;
        private String targetZone;
        private String targetRobot;
        private Map<String, Object> data;

        public ScenarioEvent() {}

        public ScenarioEvent(int triggerTimeSeconds, String eventType) {
            this.triggerTimeSeconds = triggerTimeSeconds;
            this.eventType = eventType;
        }

        // Getters and Setters
        public int getTriggerTimeSeconds() { return triggerTimeSeconds; }
        public void setTriggerTimeSeconds(int triggerTimeSeconds) { this.triggerTimeSeconds = triggerTimeSeconds; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getTargetZone() { return targetZone; }
        public void setTargetZone(String targetZone) { this.targetZone = targetZone; }
        public String getTargetRobot() { return targetRobot; }
        public void setTargetRobot(String targetRobot) { this.targetRobot = targetRobot; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
}
