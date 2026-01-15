package com.safetyhub.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * A* 경로 탐색 알고리즘
 * 2D 그리드 맵에서 최단 경로를 찾음
 */
public class PathFinder {

    private final GridMap gridMap;

    public PathFinder(GridMap gridMap) {
        this.gridMap = gridMap;
    }

    /**
     * A* 알고리즘으로 경로 찾기
     *
     * @param start 시작 위치
     * @param goal  목표 위치
     * @return 경로 (Location 리스트), 경로를 찾지 못하면 빈 리스트
     */
    public List<Location> findPath(Location start, Location goal) {
        if (start == null || goal == null) {
            return Collections.emptyList();
        }

        // 위치를 그리드 좌표로 변환
        GridMap.GridCoordinate startCoord = gridMap.toGridCoordinate(start);
        GridMap.GridCoordinate goalCoord = gridMap.toGridCoordinate(goal);

        if (startCoord == null || goalCoord == null) {
            return Collections.emptyList();
        }

        // A* 알고리즘 실행
        List<GridMap.GridCoordinate> path = findPathAStar(startCoord, goalCoord);

        // 그리드 좌표를 Location으로 변환
        List<Location> locationPath = new ArrayList<>();
        for (GridMap.GridCoordinate coord : path) {
            locationPath.add(gridMap.toLocation(coord));
        }

        return locationPath;
    }

    /**
     * A* 알고리즘 핵심 로직
     */
    private List<GridMap.GridCoordinate> findPathAStar(
            GridMap.GridCoordinate start,
            GridMap.GridCoordinate goal) {

        // 열린 목록 (탐색할 노드)
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));

        // 닫힌 목록 (이미 탐색한 노드)
        Set<GridMap.GridCoordinate> closedList = new HashSet<>();

        // 각 좌표의 최적 노드 저장
        Map<GridMap.GridCoordinate, Node> nodeMap = new HashMap<>();

        // 시작 노드 생성
        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openList.add(startNode);
        nodeMap.put(start, startNode);

        while (!openList.isEmpty()) {
            // F 값이 가장 낮은 노드 선택
            Node current = openList.poll();

            // 목표에 도달한 경우
            if (current.coord.equals(goal)) {
                return reconstructPath(current);
            }

            // 현재 노드를 닫힌 목록에 추가
            closedList.add(current.coord);

            // 인접 노드 탐색
            for (GridMap.GridCoordinate neighbor : getNeighbors(current.coord)) {
                // 이동 불가능하거나 이미 탐색한 노드는 건너뜀
                if (!gridMap.isWalkable(neighbor.getX(), neighbor.getY()) ||
                    closedList.contains(neighbor)) {
                    continue;
                }

                // 새로운 g 값 계산
                double tentativeG = current.g + distance(current.coord, neighbor);

                Node neighborNode = nodeMap.get(neighbor);

                if (neighborNode == null) {
                    // 새로운 노드 생성
                    neighborNode = new Node(
                            neighbor,
                            current,
                            tentativeG,
                            heuristic(neighbor, goal)
                    );
                    nodeMap.put(neighbor, neighborNode);
                    openList.add(neighborNode);
                } else if (tentativeG < neighborNode.g) {
                    // 더 나은 경로 발견
                    openList.remove(neighborNode);
                    neighborNode.parent = current;
                    neighborNode.g = tentativeG;
                    neighborNode.updateF();
                    openList.add(neighborNode);
                }
            }
        }

        // 경로를 찾지 못한 경우
        return Collections.emptyList();
    }

    /**
     * 인접 노드 가져오기 (상하좌우 4방향)
     */
    private List<GridMap.GridCoordinate> getNeighbors(GridMap.GridCoordinate coord) {
        List<GridMap.GridCoordinate> neighbors = new ArrayList<>();

        int[][] directions = {
                {0, 1},   // 위
                {1, 0},   // 오른쪽
                {0, -1},  // 아래
                {-1, 0}   // 왼쪽
        };

        for (int[] dir : directions) {
            int newX = coord.getX() + dir[0];
            int newY = coord.getY() + dir[1];

            if (newX >= 0 && newX < gridMap.getWidth() &&
                newY >= 0 && newY < gridMap.getHeight()) {
                neighbors.add(new GridMap.GridCoordinate(newX, newY));
            }
        }

        return neighbors;
    }

    /**
     * 인접 노드 가져오기 (8방향 - 대각선 포함)
     */
    private List<GridMap.GridCoordinate> getNeighbors8(GridMap.GridCoordinate coord) {
        List<GridMap.GridCoordinate> neighbors = new ArrayList<>();

        int[][] directions = {
                {0, 1},   // 위
                {1, 1},   // 오른쪽 위 (대각선)
                {1, 0},   // 오른쪽
                {1, -1},  // 오른쪽 아래 (대각선)
                {0, -1},  // 아래
                {-1, -1}, // 왼쪽 아래 (대각선)
                {-1, 0},  // 왼쪽
                {-1, 1}   // 왼쪽 위 (대각선)
        };

        for (int[] dir : directions) {
            int newX = coord.getX() + dir[0];
            int newY = coord.getY() + dir[1];

            if (newX >= 0 && newX < gridMap.getWidth() &&
                newY >= 0 && newY < gridMap.getHeight()) {

                // 대각선 이동 시 양쪽이 막혀있으면 불가
                if (dir[0] != 0 && dir[1] != 0) {
                    if (!gridMap.isWalkable(coord.getX() + dir[0], coord.getY()) ||
                        !gridMap.isWalkable(coord.getX(), coord.getY() + dir[1])) {
                        continue;
                    }
                }

                neighbors.add(new GridMap.GridCoordinate(newX, newY));
            }
        }

        return neighbors;
    }

    /**
     * 휴리스틱 함수 (맨해튼 거리)
     */
    private double heuristic(GridMap.GridCoordinate a, GridMap.GridCoordinate b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * 휴리스틱 함수 (유클리드 거리)
     */
    private double heuristicEuclidean(GridMap.GridCoordinate a, GridMap.GridCoordinate b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 두 노드 간 실제 거리
     */
    private double distance(GridMap.GridCoordinate a, GridMap.GridCoordinate b) {
        // 대각선 이동 시 √2, 직선 이동 시 1
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());

        if (dx == 1 && dy == 1) {
            return Math.sqrt(2);  // 대각선
        }
        return 1.0;  // 직선
    }

    /**
     * 경로 재구성
     */
    private List<GridMap.GridCoordinate> reconstructPath(Node goalNode) {
        List<GridMap.GridCoordinate> path = new ArrayList<>();
        Node current = goalNode;

        while (current != null) {
            path.add(0, current.coord);  // 역순으로 추가
            current = current.parent;
        }

        return path;
    }

    /**
     * A* 노드 클래스
     */
    @Getter
    @AllArgsConstructor
    private static class Node {
        private final GridMap.GridCoordinate coord;
        private Node parent;
        private double g;  // 시작점에서 현재 노드까지의 실제 비용
        private double h;  // 현재 노드에서 목표까지의 추정 비용 (휴리스틱)
        private double f;  // g + h

        public Node(GridMap.GridCoordinate coord, Node parent, double g, double h) {
            this.coord = coord;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        public void updateF() {
            this.f = this.g + this.h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(coord, node.coord);
        }

        @Override
        public int hashCode() {
            return Objects.hash(coord);
        }
    }

    /**
     * 경로 정보 클래스
     */
    @Getter
    @AllArgsConstructor
    public static class PathInfo {
        private List<Location> path;
        private double totalDistance;
        private int steps;

        public boolean isValid() {
            return path != null && !path.isEmpty();
        }
    }

    /**
     * 상세 경로 정보와 함께 경로 찾기
     */
    public PathInfo findPathWithInfo(Location start, Location goal) {
        List<Location> path = findPath(start, goal);

        if (path.isEmpty()) {
            return new PathInfo(Collections.emptyList(), 0.0, 0);
        }

        // 총 거리 계산
        double totalDistance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += path.get(i).distanceTo(path.get(i + 1));
        }

        return new PathInfo(path, totalDistance, path.size());
    }
}
