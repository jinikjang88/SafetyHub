## 디렉토리 구조

```
safetyhub/
│
├── docs/                           # 문서
│   ├── ACHITECTURE.md              # 아키텍처 설계
│   ├── DEVROADMAP.md               # 개발 로드맵
│   ├── FLOW.md                     # 통합 시나리오
│   ├── PRODUCT_LINE_UP.md          # 제품 라인업
│   ├── PROJECT_INFO.md             # 프로젝트 정보 (본 문서)
│   ├── ROBOT_SIMULATION.md         # 🆕 로봇 시뮬레이션 시스템
│   └── TECH_INFO.md                # 기술 스택
│
├── backend/                        # ✅ 구현 완료
│   ├── build.gradle                # 루트 빌드 설정
│   ├── settings.gradle             # 모듈 설정
│   ├── gradle.properties           # 버전 관리
│   ├── Dockerfile                  # 컨테이너 빌드
│   │
│   ├── safetyhub-core/             # 🟢 도메인 모델, 이벤트 (의존성 없음)
│   │   └── src/main/java/com/safetyhub/core/
│   │       ├── domain/             # Device, Worker, Zone, Emergency, Location
│   │       ├── event/              # DomainEvent, EmergencyDetectedEvent 등
│   │       └── port/out/           # Repository, EventPublisher 인터페이스
│   │
│   ├── safetyhub-application/      # 🟡 UseCase 구현
│   │   ├── device-control/         # DeviceControlUseCase, DeviceControlService
│   │   ├── worker-monitoring/      # WorkerMonitoringUseCase, WorkerMonitoringService
│   │   └── emergency-response/     # EmergencyResponseUseCase, EmergencyResponseService
│   │
│   ├── safetyhub-adapter/          # 🔵 외부 어댑터
│   │   ├── adapter-mqtt/           # MqttMessageHandler
│   │   ├── adapter-websocket/      # WebSocketConfig, WebSocketEventPublisher
│   │   ├── adapter-rest/           # DeviceController, WorkerController, EmergencyController
│   │   └── adapter-simulator/      # DeviceSimulator
│   │
│   ├── safetyhub-infrastructure/   # 🟣 인프라 구현
│   │   ├── persistence/            # JPA Entity, Repository 구현
│   │   ├── messaging/              # KafkaEventPublisher
│   │   └── external/               # Emergency119ApiClient
│   │
│   ├── safetyhub-gateway/          # 🔴 통합 게이트웨이
│   │   └── src/main/java/.../      # MessageRouter (Hot/Warm/Cold Path)
│   │
│   └── safetyhub-bootstrap/        # ⚪ 실행 애플리케이션
│       ├── src/main/java/.../      # SafetyHubApplication
│       └── src/main/resources/     # application.yml
│
├── frontend/                       # 📋 예정
│   ├── dashboard/                  # 관제 대시보드
│   └── mobile-app/                 # React Native 앱
│
├── simulator/                      # 📋 예정 (로봇 시뮬레이션 시스템)
│   └── robot-simulator/            # 🤖 로봇 시뮬레이터
│       ├── src/main/java/.../
│       │   ├── core/               # RobotWorker, RobotState, RobotSchedule
│       │   ├── world/              # VirtualWorld, GridMap, Zone, PathFinder
│       │   ├── scenario/           # ScenarioEngine, ScenarioLoader
│       │   ├── event/              # EventGenerator, SensorDataGenerator
│       │   └── publisher/          # MQTT/Kafka/WebSocket 발행
│       └── src/main/resources/
│           └── scenarios/          # YAML 시나리오 파일
│               ├── daily_operation.yaml   # 일상 운영 시나리오
│               ├── fire_emergency.yaml    # 화재 대피 시나리오
│               ├── worker_fall.yaml       # 낙상 감지 시나리오
│               ├── gas_leak.yaml          # 가스 누출 시나리오
│               └── load_test.yaml         # 부하 테스트 시나리오
│
├── hardware/                       # 📋 예정
│   ├── safetykit/
│   │   ├── firmware/               # ESP32 펌웨어
│   │   ├── pcb/                    # PCB 설계 (KiCad)
│   │   └── case/                   # 케이스 설계 (3D)
│   └── lifeguard/
│       ├── firmware/               # nRF52 펌웨어
│       └── pcb/                    # PCB 설계
│
├── infra/                          # ✅ 구현 완료
│   └── docker/                     # Docker 설정
│       ├── mysql/init/             # MySQL 초기화 스크립트
│       ├── prometheus/             # Prometheus 설정
│       └── grafana/provisioning/   # Grafana 데이터소스
│
└── docker-compose.yml              # ✅ 로컬 개발 환경
```

---

## 모듈 설명

### Backend 모듈

| 모듈 | 설명 | 의존성 |
| --- | --- | --- |
| **safetyhub-core** | 도메인 모델, 이벤트 | 없음 (독립) |
| **safetyhub-gateway** | 통합 게이트웨이 | core |
| **safetyhub-application** | 유스케이스 구현 | core |
| **safetyhub-adapter** | 외부 어댑터 | core, application |
| **safetyhub-infrastructure** | 인프라 구현 | core, application |
| **safetyhub-bootstrap** | 실행 애플리케이션 | 전체 |

### Frontend 모듈

| 모듈 | 설명 | 기술 |
| --- | --- | --- |
| **dashboard** | 웹 관제 대시보드 | React 18, TypeScript |
| **mobile-app** | 모바일 앱 | React Native |

### Simulator 모듈 (로봇 시뮬레이션)

> 📄 상세 문서: [ROBOT_SIMULATION.md](ROBOT_SIMULATION.md)

| 모듈 | 설명 | 용도 |
| --- | --- | --- |
| **robot-simulator** | 가상 로봇 시뮬레이션 | 대규모 부하 테스트, 시나리오 검증 |

#### Robot Simulator 컴포넌트

| 컴포넌트 | 설명 |
| --- | --- |
| **RobotWorker** | 로봇 더미 엔티티 (상태/스케줄/위치) |
| **VirtualWorld** | 가상 공장 환경 (2D 그리드 맵) |
| **ScenarioEngine** | YAML 기반 시나리오 실행 엔진 |
| **EventGenerator** | 센서/위치/긴급 이벤트 생성기 |
| **PathFinder** | A* 경로 탐색 알고리즘 |

#### 테스트 시나리오

| 시나리오 | 설명 | 검증 항목 |
| --- | --- | --- |
| **daily_operation** | 500대 8시간 정규 운영 | 위치 추적, 하트비트 |
| **fire_emergency** | 화재 발생 → 대피 | 알림 < 3초, 대피 < 10분 |
| **worker_fall** | 낙상 감지 → 긴급 대응 | 감지 < 1초 |
| **gas_leak** | 가스 누출 → 설비 차단 | 차단 < 100ms |
| **load_test** | 10,000대 동시 접속 | 응답 < 100ms |

### Hardware 모듈

| 모듈 | 설명 | 툴 |
| --- | --- | --- |
| **safetykit/firmware** | ESP32 펀웨어 | Arduino/PlatformIO |
| **safetykit/pcb** | PCB 회로 설계 | KiCad |
| **safetykit/case** | 케이스 3D 모델 | Fusion 360/FreeCAD |
| **lifeguard/firmware** | nRF52 펀웨어 | Zephyr RTOS |
| **lifeguard/pcb** | PCB 회로 설계 | KiCad |

---

## 의존성 규칙

### 클린 아키텍처 의존성

```
외부 → 내부 (내부는 외부를 모름)

[Infrastructure] → [Application] → [Core]
[Adapter] → [Application] → [Core]
```

### 금지된 의존성

- Core → Infrastructure (❌ 금지)
- Core → Adapter (❌ 금지)
- Application → Infrastructure (❌ 금지)

---

## 코드 컨벤션

| 언어 | 컨벤션 | 도구 |
| --- | --- | --- |
| Java | Google Java Style Guide | Checkstyle |
| TypeScript | ESLint + Prettier | ESLint |
| 커밋 | Conventional Commits | commitlint |

### 커밋 메시지 형식

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Type:**

- feat: 새로운 기능
- fix: 버그 수정
- docs: 문서
- style: 코드 스타일
- refactor: 리팩토링
- test: 테스트
- chore: 빌드/설정

---

## 빠른 시작

```bash
# 1. 인프라 서비스 실행
docker-compose up -d

# 2. 애플리케이션 빌드 및 실행
cd backend
./gradlew :safetyhub-bootstrap:bootRun
```

---

**문서 버전:** v1.2

**최종 수정:** 2026-01-13