package com.safetyhub.adapter.simulator.world;

import com.safetyhub.adapter.simulator.robot.Position;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * A* 경로 탐색 알고리즘
 */
@RequiredArgsConstructor
public class PathFinder {

    private final GridMap gridMap;

    // 이동 방향 (상하좌우)
    private static final int[][] DIRECTIONS = {
            {0, -1},  // 상
            {0, 1},   // 하
            {-1, 0},  // 좌
            {1, 0}    // 우
    };

    /**
     * A* 알고리즘으로 최단 경로 찾기
     *
     * @param start 시작 위치
     * @param goal  목표 위치
     * @return 경로 (시작점 제외, 목표점 포함), 경로 없으면 빈 리스트
     */
    public List<Position> findPath(Position start, Position goal) {
        if (start.equals(goal)) {
            return Collections.emptyList();
        }

        if (!gridMap.isWalkable(goal)) {
            return Collections.emptyList();
        }

        // 우선순위 큐 (f값 기준 정렬)
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));

        // 방문한 노드
        Set<Position> closedSet = new HashSet<>();

        // 각 위치까지의 최단 거리
        Map<Position, Integer> gScore = new HashMap<>();

        // 각 위치의 부모 노드
        Map<Position, Position> cameFrom = new HashMap<>();

        // 시작 노드 추가
        gScore.put(start, 0);
        openSet.add(new Node(start, 0, heuristic(start, goal)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.position.equals(goal)) {
                return reconstructPath(cameFrom, goal);
            }

            if (closedSet.contains(current.position)) {
                continue;
            }
            closedSet.add(current.position);

            // 인접 노드 탐색
            for (int[] dir : DIRECTIONS) {
                int nx = current.position.getX() + dir[0];
                int ny = current.position.getY() + dir[1];
                Position neighbor = Position.of(nx, ny);

                if (closedSet.contains(neighbor) || !gridMap.isWalkable(neighbor)) {
                    continue;
                }

                int tentativeG = gScore.get(current.position) + 1;

                if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current.position);
                    gScore.put(neighbor, tentativeG);
                    int f = tentativeG + heuristic(neighbor, goal);
                    openSet.add(new Node(neighbor, tentativeG, f));
                }
            }
        }

        // 경로 없음
        return Collections.emptyList();
    }

    /**
     * 휴리스틱 함수 (맨해튼 거리)
     */
    private int heuristic(Position a, Position b) {
        return a.manhattanDistance(b);
    }

    /**
     * 경로 재구성
     */
    private List<Position> reconstructPath(Map<Position, Position> cameFrom, Position current) {
        List<Position> path = new ArrayList<>();
        path.add(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }

        // 시작점 제거
        if (!path.isEmpty()) {
            path.remove(0);
        }

        return path;
    }

    /**
     * A* 노드
     */
    private static class Node {
        Position position;
        int g;  // 시작점에서 현재까지의 비용
        int f;  // g + h (총 예상 비용)

        Node(Position position, int g, int f) {
            this.position = position;
            this.g = g;
            this.f = f;
        }
    }
}
