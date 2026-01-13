## 디렉토리 구조

```
safetyhub/
│
├── docs/                           # 문서
│   ├── architecture/               # 아키텍처 설계
│   ├── api/                        # API 명세
│   └── hardware/                   # 하드웨어 회로도
│
├── backend/
│   ├── safetyhub-core/             # 도메인 모델, 이벤트
│   ├── safetyhub-gateway/          # 통합 게이트웨이
│   ├── safetyhub-application/      # UseCase 구현
│   │   ├── device-control/         # 설비 제어
│   │   ├── worker-monitoring/      # 작업자 모니터링
│   │   └── emergency-response/     # 긴급 대응
│   ├── safetyhub-adapter/
│   │   ├── adapter-mqtt/           # MQTT 어댑터
│   │   ├── adapter-websocket/      # WebSocket 어댑터
│   │   ├── adapter-rest/           # REST API
│   │   └── adapter-simulator/      # 시뮬레이터 어댑터
│   ├── safetyhub-infrastructure/
│   │   ├── persistence/            # JPA, Redis
│   │   ├── messaging/              # Kafka
│   │   └── external/               # 119 API, SMS 등
│   └── safetyhub-bootstrap/        # 실행 애플리케이션
│
├── frontend/
│   ├── dashboard/                  # 관제 대시보드
│   └── mobile-app/                 # React Native 앱
│
├── simulator/
│   ├── robot-simulator/            # 로봇 시뮬레이터
│   ├── device-simulator/           # SafetyKit 시뮬레이터
│   └── worker-simulator/           # LifeGuard 시뮬레이터
│
├── hardware/
│   ├── safetykit/
│   │   ├── firmware/               # ESP32 펀웨어
│   │   ├── pcb/                    # PCB 설계 (KiCad)
│   │   └── case/                   # 케이스 설계 (3D)
│   └── lifeguard/
│       ├── firmware/               # nRF52 펀웨어
│       └── pcb/                    # PCB 설계
│
├── infra/
│   ├── docker/                     # Docker 설정
│   ├── k8s/                        # Kubernetes 배포
│   └── terraform/                  # 클라우드 인프라
│
└── docker-compose.yml              # 로컬 개발 환경
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

**문서 버전:** v1.0

**최종 수정:** 2026-01-09