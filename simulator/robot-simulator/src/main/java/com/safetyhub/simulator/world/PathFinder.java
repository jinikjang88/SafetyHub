package com.safetyhub.simulator.world;

import com.safetyhub.simulator.core.Location;

import java.util.*;

/**
 * A* 경로 탐색 알고리즘 구현
 */
public class PathFinder {
    private final GridMap gridMap;
    private static final int[][] DIRECTIONS = {
            {0, 1}, {1, 0}, {0, -1}, {-1, 0},  // 상하좌우
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // 대각선
    };

    public PathFinder(GridMap gridMap) {
        this.gridMap = gridMap;
    }

    /**
     * A* 알고리즘을 사용한 경로 탐색
     */
    public List<Location> findPath(Location start, Location goal) {
        if (!gridMap.isWalkable(goal)) {
            return Collections.emptyList();
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Location, Node> allNodes = new HashMap<>();
        Set<Location> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.location.equals(goal)) {
                return reconstructPath(current);
            }

            closedSet.add(current.location);

            for (int[] dir : DIRECTIONS) {
                int newX = current.location.getX() + dir[0];
                int newY = current.location.getY() + dir[1];
                Location neighbor = new Location(newX, newY);

                if (closedSet.contains(neighbor) || !gridMap.isWalkable(neighbor)) {
                    continue;
                }

                // 대각선 이동 시 코너 체크
                if (dir[0] != 0 && dir[1] != 0) {
                    if (!gridMap.isWalkable(current.location.getX() + dir[0], current.location.getY()) ||
                        !gridMap.isWalkable(current.location.getX(), current.location.getY() + dir[1])) {
                        continue;
                    }
                }

                double moveCost = (dir[0] != 0 && dir[1] != 0) ? 1.414 : 1.0;
                double tentativeGScore = current.gScore + moveCost;

                Node neighborNode = allNodes.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = new Node(neighbor, current, tentativeGScore, tentativeGScore + heuristic(neighbor, goal));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeGScore < neighborNode.gScore) {
                    openSet.remove(neighborNode);
                    neighborNode.parent = current;
                    neighborNode.gScore = tentativeGScore;
                    neighborNode.fScore = tentativeGScore + heuristic(neighbor, goal);
                    openSet.add(neighborNode);
                }
            }
        }

        return Collections.emptyList(); // 경로를 찾지 못함
    }

    /**
     * 특정 구역까지의 경로 탐색
     */
    public List<Location> findPathToZone(Location start, String zoneId) {
        Zone zone = gridMap.getZone(zoneId);
        if (zone == null) {
            return Collections.emptyList();
        }
        return findPath(start, zone.getEntrance());
    }

    /**
     * 가장 가까운 대피소까지의 경로 탐색
     */
    public List<Location> findEvacuationPath(Location start) {
        Zone nearestShelter = null;
        double minDistance = Double.MAX_VALUE;

        for (Zone zone : gridMap.getAllZones().values()) {
            if (zone.isEvacuationTarget()) {
                double distance = start.distanceTo(zone.getCenter());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestShelter = zone;
                }
            }
        }

        if (nearestShelter == null) {
            return Collections.emptyList();
        }

        return findPath(start, nearestShelter.getEntrance());
    }

    private double heuristic(Location a, Location b) {
        // 유클리디안 거리 사용
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        return Math.sqrt(dx * dx + dy * dy);
    }

    private List<Location> reconstructPath(Node endNode) {
        List<Location> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            String zoneId = gridMap.getZoneId(current.location);
            path.add(new Location(current.location.getX(), current.location.getY(), zoneId));
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static class Node {
        Location location;
        Node parent;
        double gScore;
        double fScore;

        Node(Location location, Node parent, double gScore, double fScore) {
            this.location = location;
            this.parent = parent;
            this.gScore = gScore;
            this.fScore = fScore;
        }
    }
}
