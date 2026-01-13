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
├── simulator/                      # 📋 예정
│   ├── robot-simulator/            # 로봇 시뮬레이터
│   ├── device-simulator/           # SafetyKit 시뮬레이터
│   └── worker-simulator/           # LifeGuard 시뮬레이터
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

### Simulator 모듈

| 모듈 | 설명 | 용도 |
| --- | --- | --- |
| **robot-simulator** | 가상 로봇 시뮬레이션 | 대규모 부하 테스트 |
| **device-simulator** | SafetyKit 가상 장치 | 서버 테스트 |
| **worker-simulator** | LifeGuard 가상 장치 | 긴급 시나리오 테스트 |

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

**문서 버전:** v1.1

**최종 수정:** 2026-01-13