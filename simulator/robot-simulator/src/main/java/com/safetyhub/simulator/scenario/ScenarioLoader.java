package com.safetyhub.simulator.scenario;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * YAML 시나리오 파일 로더
 */
public class ScenarioLoader {

    @SuppressWarnings("unchecked")
    public Scenario loadFromYaml(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);

        Scenario scenario = new Scenario();
        scenario.setId((String) data.get("id"));
        scenario.setName((String) data.get("name"));
        scenario.setDescription((String) data.get("description"));
        scenario.setRobotCount(getInt(data, "robotCount", 100));
        scenario.setDurationMinutes(getInt(data, "durationMinutes", 60));

        if (data.containsKey("parameters")) {
            scenario.setParameters((Map<String, Object>) data.get("parameters"));
        }

        List<Map<String, Object>> eventsList = (List<Map<String, Object>>) data.get("events");
        if (eventsList != null) {
            for (Map<String, Object> eventData : eventsList) {
                Scenario.ScenarioEvent event = new Scenario.ScenarioEvent();
                event.setTriggerTimeSeconds(getInt(eventData, "triggerTimeSeconds", 0));
                event.setEventType((String) eventData.get("eventType"));
                event.setTargetZone((String) eventData.get("targetZone"));
                event.setTargetRobot((String) eventData.get("targetRobot"));
                event.setData((Map<String, Object>) eventData.get("data"));
                scenario.addEvent(event);
            }
        }

        return scenario;
    }

    public Scenario loadFromResource(String resourcePath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalArgumentException("Scenario file not found: " + resourcePath);
        }
        return loadFromYaml(is);
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return defaultValue;
    }
}
