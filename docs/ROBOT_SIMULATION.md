# SafetyHub 로봇 시뮬레이션 시스템

> **목적**: 실제 작업자 투입 전, 로봇 더미를 활용하여 대규모 산업 현장 환경을 시뮬레이션하고 SafetyHub 시스템의 안정성과 성능을 사전 검증

---

## 개요

### 왜 로봇 시뮬레이션인가?

| 구분 | 실제 작업자 테스트 | 로봇 시뮬레이션 |
|------|-------------------|----------------|
| 비용 | 높음 (인건비, 보험) | 낮음 (초기 투자 후 재사용) |
| 안전성 | 위험 요소 존재 | 완전 안전 |
| 규모 | 제한적 (수십 명) | 대규모 (수천 대) |
| 반복성 | 어려움 | 동일 시나리오 무한 반복 |
| 극한 상황 | 테스트 불가 | 화재/가스누출 시뮬레이션 가능 |
| 24시간 운영 | 불가 | 가능 |

### 시뮬레이션 목표

1. **대규모 부하 테스트**: 10,000+ 동시 접속 장치 처리 검증
2. **긴급 상황 대응**: 화재, 가스 누출, 낙상 등 비상 시나리오 검증
3. **알고리즘 검증**: 대피 경로, 위험 구역 감지 로직 최적화
4. **성능 튜닝**: 응답 시간, 처리량 벤치마크

---

## 시뮬레이션 환경 구성

### 가상 공장 (Virtual Factory)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Virtual Factory Map                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                                                               │   │
│  │   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐       │   │
│  │   │ Zone A  │  │ Zone B  │  │ Zone C  │  │ Zone D  │       │   │
│  │   │ 작업장1 │  │ 작업장2 │  │ 위험구역│  │ 창고    │       │   │
│  │   │ 🤖🤖🤖 │  │ 🤖🤖   │  │ ⚠️🤖   │  │ 🤖     │       │   │
│  │   └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘       │   │
│  │        │            │            │            │             │   │
│  │   ═════╧════════════╧════════════╧════════════╧═════        │   │
│  │                    Main Corridor (복도)                       │   │
│  │   ═════╤════════════╤════════════╤════════════╤═════        │   │
│  │        │            │            │            │             │   │
│  │   ┌────┴────┐  ┌────┴────┐  ┌────┴────┐  ┌────┴────┐       │   │
│  │   │ Zone E  │  │ Zone F  │  │ Zone G  │  │ Zone H  │       │   │
│  │   │ 휴게실  │  │ 식당    │  │ 의무실  │  │ 대피소  │       │   │
│  │   │ 🤖🤖   │  │ 🤖🤖🤖 │  │ 🏥     │  │ 🚨      │       │   │
│  │   └─────────┘  └─────────┘  └─────────┘  └─────────┘       │   │
│  │                                                               │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  🤖 Robot Worker (로봇 더미)   ⚠️ 위험 감지   🚨 긴급 상황        │
└─────────────────────────────────────────────────────────────────────┘
```

### 구역 정의

| 구역 | 타입 | 위험도 | 최대 수용 | 설명 |
|------|------|--------|----------|------|
| Zone A | WORK_AREA | MEDIUM | 50 | 일반 작업장 1 |
| Zone B | WORK_AREA | MEDIUM | 50 | 일반 작업장 2 |
| Zone C | DANGER_ZONE | HIGH | 10 | 위험물 취급 구역 |
| Zone D | WORK_AREA | LOW | 30 | 창고/물류 구역 |
| Zone E | REST_AREA | LOW | 20 | 휴게실 |
| Zone F | REST_AREA | LOW | 50 | 식당 |
| Zone G | REST_AREA | LOW | 5 | 의무실 |
| Zone H | ASSEMBLY_POINT | LOW | 200 | 비상 대피소 |

---

## 로봇 더미 (Robot Worker) 설계

### 로봇 상태 모델

```java
public class RobotWorker {
    private String robotId;
    private RobotState state;
    private Location currentLocation;
    private String currentZoneId;
    private Schedule dailySchedule;
    private HealthSimulation health;
    private BatterySimulation battery;

    enum RobotState {
        WORKING,      // 작업 중
        RESTING,      // 휴식 중
        EATING,       // 식사 중
        MOVING,       // 이동 중
        EMERGENCY,    // 긴급 상황
        EVACUATING,   // 대피 중
        OFFLINE       // 오프라인
    }
}
```

### 일일 스케줄 시뮬레이션

```
┌──────────────────────────────────────────────────────────────┐
│                    Robot Worker Daily Schedule                │
├──────────┬───────────────────────────────────────────────────┤
│ 08:00    │ 🏭 출근 - Zone A/B/C/D 배치                       │
│ 08:00-10:00 │ 💼 작업 (WORKING)                              │
│ 10:00-10:15 │ ☕ 휴식 (RESTING) - Zone E 이동                │
│ 10:15-12:00 │ 💼 작업 (WORKING)                              │
│ 12:00-13:00 │ 🍽️ 식사 (EATING) - Zone F 이동                │
│ 13:00-15:00 │ 💼 작업 (WORKING)                              │
│ 15:00-15:15 │ ☕ 휴식 (RESTING) - Zone E 이동                │
│ 15:15-17:00 │ 💼 작업 (WORKING)                              │
│ 17:00    │ 🏠 퇴근                                           │
└──────────┴───────────────────────────────────────────────────┘
```

### 행동 시뮬레이션

| 행동 | 설명 | 생성 이벤트 |
|------|------|------------|
| **작업 (WORKING)** | 할당된 구역에서 작업 수행 | 위치 업데이트, 센서 데이터 |
| **휴식 (RESTING)** | 휴게실로 이동 후 대기 | 구역 변경 이벤트 |
| **식사 (EATING)** | 식당으로 이동 후 대기 | 구역 변경 이벤트 |
| **이동 (MOVING)** | A* 알고리즘 기반 경로 이동 | 실시간 위치 업데이트 |
| **긴급 (EMERGENCY)** | SOS 버튼, 낙상, 건강 이상 | 긴급 상황 이벤트 |
| **대피 (EVACUATING)** | 대피 경로 따라 대피소 이동 | 대피 상태 이벤트 |

---

## 시뮬레이션 시나리오

### Scenario 1: 일상 운영

```yaml
name: daily_operation
description: 평상시 공장 운영 시뮬레이션
duration: 8_hours
robots: 500
zones: 8

events:
  - type: SCHEDULE
    pattern: 정규 근무 스케줄

metrics:
  - 구역별 인원 현황
  - 실시간 위치 추적
  - 장치 하트비트 안정성
```

### Scenario 2: 화재 발생

```yaml
name: fire_emergency
description: Zone C 화재 발생 시뮬레이션
duration: 30_minutes
robots: 500

timeline:
  - time: 0:00
    event: Zone C 화재 감지
    trigger: SafetyKit 온도/연기 센서

  - time: 0:01
    event: 긴급 알림 발송
    action: 전체 대시보드 경보, 해당 구역 작업자 알림

  - time: 0:02
    event: 대피 명령
    action: Zone C 인접 구역 대피 시작

  - time: 0:05
    event: 119 자동 신고
    action: Emergency119ApiClient 호출

  - time: 0:10
    event: 대피 완료 확인
    action: Zone H 집결 인원 카운트

expected_results:
  - 긴급 알림 < 3초
  - 대피 완료 < 10분
  - 119 신고 < 5분
  - 인원 파악 정확도 100%
```

### Scenario 3: 작업자 낙상

```yaml
name: worker_fall
description: 작업자 낙상 감지 시뮬레이션
duration: 10_minutes
robots: 100

timeline:
  - time: 0:00
    event: Robot #42 낙상 감지
    trigger: LifeGuard 가속도계 급격한 변화

  - time: 0:01
    event: 건강 상태 DANGER 전환
    action: Hot Path 긴급 처리

  - time: 0:02
    event: 주변 작업자 알림
    action: 반경 50m 내 로봇에 알림

  - time: 0:03
    event: 관리자 대시보드 표시
    action: WebSocket 실시간 알림

expected_results:
  - 낙상 감지 < 1초
  - 알림 전달 < 3초
  - 대시보드 표시 < 5초
```

### Scenario 4: 가스 누출

```yaml
name: gas_leak
description: 유해 가스 누출 시뮬레이션
duration: 20_minutes
robots: 500

timeline:
  - time: 0:00
    event: Zone C 가스 농도 상승 감지
    trigger: SafetyKit 가스 센서 임계치 초과

  - time: 0:00
    event: 즉시 설비 차단
    action: Zone C 내 모든 SafetyKit 릴레이 OFF

  - time: 0:01
    event: 긴급 대피 명령
    action: Zone C, 인접 구역 즉시 대피

  - time: 0:02
    event: 환기 시스템 가동
    action: 연동 설비 제어

expected_results:
  - 설비 차단 < 100ms (Hot Path)
  - 대피 명령 < 2초
  - 전원 대피 < 5분
```

### Scenario 5: 대규모 부하 테스트

```yaml
name: load_test
description: 10,000대 동시 접속 테스트
duration: 1_hour
robots: 10000

patterns:
  - 초당 10,000 위치 업데이트
  - 초당 1,000 센서 데이터
  - 초당 100 상태 변경
  - 초당 10 긴급 이벤트

expected_results:
  - 평균 응답 시간 < 100ms
  - 99th percentile < 500ms
  - 데이터 손실 0%
  - CPU 사용률 < 70%
```

---

## 기술 아키텍처

### 시뮬레이터 컴포넌트

```
┌─────────────────────────────────────────────────────────────────┐
│                    Robot Simulation System                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ Scenario Engine │  │  Robot Factory  │  │  Event Generator │  │
│  │                 │  │                 │  │                 │  │
│  │ - YAML 파싱     │  │ - 로봇 생성     │  │ - 센서 데이터   │  │
│  │ - 타임라인 실행 │  │ - 상태 관리     │  │ - 위치 업데이트 │  │
│  │ - 이벤트 트리거 │  │ - 스케줄 관리   │  │ - 긴급 이벤트   │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
│           │                    │                    │            │
│           └────────────────────┼────────────────────┘            │
│                                │                                  │
│                    ┌───────────▼───────────┐                     │
│                    │   Virtual World       │                     │
│                    │                       │                     │
│                    │ - 2D Grid Map         │                     │
│                    │ - Zone Management     │                     │
│                    │ - Path Finding (A*)   │                     │
│                    │ - Collision Detection │                     │
│                    └───────────┬───────────┘                     │
│                                │                                  │
│           ┌────────────────────┼────────────────────┐            │
│           │                    │                    │            │
│  ┌────────▼────────┐  ┌───────▼────────┐  ┌───────▼────────┐   │
│  │  MQTT Publisher │  │ Kafka Producer │  │ WebSocket Emit │   │
│  │                 │  │                │  │                │   │
│  │ → SafetyHub     │  │ → SafetyHub    │  │ → Dashboard    │   │
│  │   Gateway       │  │   Event Stream │  │   (시각화)     │   │
│  └─────────────────┘  └────────────────┘  └────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 시뮬레이터 모듈 구조

```
simulator/
├── robot-simulator/
│   ├── src/main/java/com/safetyhub/simulator/
│   │   ├── RobotSimulatorApplication.java
│   │   ├── core/
│   │   │   ├── RobotWorker.java          # 로봇 더미 엔티티
│   │   │   ├── RobotState.java           # 상태 정의
│   │   │   ├── RobotSchedule.java        # 일일 스케줄
│   │   │   └── RobotBehavior.java        # 행동 정의
│   │   ├── world/
│   │   │   ├── VirtualWorld.java         # 가상 세계
│   │   │   ├── GridMap.java              # 2D 그리드 맵
│   │   │   ├── Zone.java                 # 구역 정의
│   │   │   └── PathFinder.java           # A* 경로 탐색
│   │   ├── scenario/
│   │   │   ├── ScenarioEngine.java       # 시나리오 엔진
│   │   │   ├── ScenarioLoader.java       # YAML 로더
│   │   │   └── TimelineExecutor.java     # 타임라인 실행
│   │   ├── event/
│   │   │   ├── EventGenerator.java       # 이벤트 생성
│   │   │   ├── SensorDataGenerator.java  # 센서 데이터 생성
│   │   │   └── EmergencySimulator.java   # 긴급 상황 시뮬레이션
│   │   └── publisher/
│   │       ├── MqttEventPublisher.java   # MQTT 발행
│   │       ├── KafkaEventPublisher.java  # Kafka 발행
│   │       └── WebSocketPublisher.java   # WebSocket 발행
│   └── src/main/resources/
│       └── scenarios/
│           ├── daily_operation.yaml
│           ├── fire_emergency.yaml
│           ├── worker_fall.yaml
│           ├── gas_leak.yaml
│           └── load_test.yaml
```

---

## 메트릭 및 모니터링

### 수집 메트릭

| 카테고리 | 메트릭 | 설명 |
|----------|--------|------|
| **성능** | response_time_ms | 이벤트 처리 응답 시간 |
| | throughput_per_sec | 초당 처리량 |
| | queue_depth | 메시지 큐 깊이 |
| **정확도** | location_accuracy | 위치 추적 정확도 |
| | event_delivery_rate | 이벤트 전달 성공률 |
| | false_alarm_rate | 오탐지율 |
| **커버리지** | zone_coverage | 구역별 모니터링 커버리지 |
| | robot_online_rate | 로봇 온라인율 |
| **긴급대응** | emergency_response_time | 긴급 상황 응답 시간 |
| | evacuation_completion_time | 대피 완료 시간 |

### 대시보드 시각화

```
┌─────────────────────────────────────────────────────────────────┐
│                 SafetyHub Simulation Dashboard                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────────┐  ┌─────────────────────────┐   │
│  │      Real-time Map          │  │     System Metrics      │   │
│  │  ┌─────┬─────┬─────┬─────┐ │  │                         │   │
│  │  │ 🤖45│ 🤖38│ ⚠️8 │ 🤖12│ │  │  Active Robots: 503    │   │
│  │  ├─────┴─────┴─────┴─────┤ │  │  Events/sec: 1,234     │   │
│  │  │      Main Corridor     │ │  │  Avg Response: 45ms    │   │
│  │  ├─────┬─────┬─────┬─────┤ │  │  Alerts: 2             │   │
│  │  │ 🤖15│ 🤖67│ 🏥3 │ 🚨0 │ │  │                         │   │
│  │  └─────┴─────┴─────┴─────┘ │  │  ████████░░ 80% CPU    │   │
│  └─────────────────────────────┘  └─────────────────────────┘   │
│                                                                   │
│  ┌─────────────────────────────┐  ┌─────────────────────────┐   │
│  │     Event Timeline          │  │    Zone Statistics      │   │
│  │                             │  │                         │   │
│  │  10:05 🤖 Robot #42 이동   │  │  Zone A: ████████ 45   │   │
│  │  10:04 📍 Zone C 경보      │  │  Zone B: ██████░░ 38   │   │
│  │  10:03 ✓ 대피 완료         │  │  Zone C: ██░░░░░░ 8    │   │
│  │  10:02 ⚠️ 가스 감지        │  │  Zone D: ███░░░░░ 12   │   │
│  │                             │  │  Zone E: ████░░░░ 15   │   │
│  └─────────────────────────────┘  └─────────────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 실행 방법

### 1. 시뮬레이터 실행

```bash
# 인프라 실행
docker-compose up -d

# 시뮬레이터 빌드 및 실행
cd simulator/robot-simulator
./gradlew bootRun

# 특정 시나리오 실행
./gradlew bootRun --args="--scenario=fire_emergency"
```

### 2. 시나리오 실행

```bash
# 일상 운영 시나리오 (8시간, 500대)
curl -X POST http://localhost:8081/api/simulation/start \
  -H "Content-Type: application/json" \
  -d '{"scenario": "daily_operation", "robots": 500}'

# 화재 시나리오
curl -X POST http://localhost:8081/api/simulation/start \
  -H "Content-Type: application/json" \
  -d '{"scenario": "fire_emergency"}'

# 부하 테스트 (10,000대)
curl -X POST http://localhost:8081/api/simulation/start \
  -H "Content-Type: application/json" \
  -d '{"scenario": "load_test", "robots": 10000}'
```

### 3. 결과 확인

```bash
# 시뮬레이션 상태 조회
curl http://localhost:8081/api/simulation/status

# 메트릭 조회
curl http://localhost:8081/api/simulation/metrics

# 시뮬레이션 중지
curl -X POST http://localhost:8081/api/simulation/stop
```

---

## 예상 효과

| 항목 | 효과 |
|------|------|
| **비용 절감** | 실제 인력 투입 대비 90% 비용 절감 |
| **안전성** | 위험 시나리오 무제한 테스트 가능 |
| **품질 향상** | 사전 검증으로 버그 조기 발견 |
| **성능 최적화** | 대규모 부하 테스트로 병목 해소 |
| **규정 준수** | 안전 인증 획득을 위한 증빙 자료 확보 |

---

## 향후 계획

1. **Phase 1**: 기본 시뮬레이터 구현 (로봇 이동, 상태 변경)
2. **Phase 2**: 시나리오 엔진 구현 (YAML 기반)
3. **Phase 3**: 3D 시각화 대시보드 (Three.js)
4. **Phase 4**: AI 기반 이상 패턴 학습
5. **Phase 5**: Digital Twin 연동

---

**문서 버전:** v1.0

**최종 수정:** 2026-01-13
