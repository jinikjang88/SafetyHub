package com.safetyhub.simulator.core;

import java.util.Objects;

/**
 * 로봇의 위치 정보 (2D 좌표)
 */
public class Location {
    private final int x;
    private final int y;
    private final String zoneId;

    public Location(int x, int y, String zoneId) {
        this.x = x;
        this.y = y;
        this.zoneId = zoneId;
    }

    public Location(int x, int y) {
        this(x, y, null);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getZoneId() {
        return zoneId;
    }

    public Location withZone(String zoneId) {
        return new Location(this.x, this.y, zoneId);
    }

    public double distanceTo(Location other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int manhattanDistance(Location other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return x == location.x && y == location.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("Location(%d, %d, zone=%s)", x, y, zoneId);
    }
}
