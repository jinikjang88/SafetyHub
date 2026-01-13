package com.safetyhub.simulator.world;

import com.safetyhub.simulator.core.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * 가상 공장의 구역 정의
 */
public class Zone {
    private final String id;
    private final String name;
    private final ZoneType type;
    private final int startX;
    private final int startY;
    private final int width;
    private final int height;
    private final int capacity;
    private boolean hazardous;
    private boolean evacuationTarget;

    public Zone(String id, String name, ZoneType type, int startX, int startY, int width, int height, int capacity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.capacity = capacity;
        this.hazardous = (type == ZoneType.HAZARD);
        this.evacuationTarget = (type == ZoneType.SHELTER);
    }

    public boolean contains(Location location) {
        return location.getX() >= startX && location.getX() < startX + width
                && location.getY() >= startY && location.getY() < startY + height;
    }

    public boolean contains(int x, int y) {
        return x >= startX && x < startX + width && y >= startY && y < startY + height;
    }

    public Location getCenter() {
        return new Location(startX + width / 2, startY + height / 2, id);
    }

    public Location getEntrance() {
        // 구역의 입구는 기본적으로 왼쪽 중앙
        return new Location(startX, startY + height / 2, id);
    }

    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                locations.add(new Location(x, y, id));
            }
        }
        return locations;
    }

    public Location getRandomLocation() {
        int x = startX + (int) (Math.random() * width);
        int y = startY + (int) (Math.random() * height);
        return new Location(x, y, id);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public ZoneType getType() { return type; }
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCapacity() { return capacity; }
    public boolean isHazardous() { return hazardous; }
    public boolean isEvacuationTarget() { return evacuationTarget; }

    // Setters
    public void setHazardous(boolean hazardous) { this.hazardous = hazardous; }
    public void setEvacuationTarget(boolean evacuationTarget) { this.evacuationTarget = evacuationTarget; }

    @Override
    public String toString() {
        return String.format("Zone[id=%s, name=%s, type=%s, pos=(%d,%d), size=%dx%d]",
                id, name, type, startX, startY, width, height);
    }
}
