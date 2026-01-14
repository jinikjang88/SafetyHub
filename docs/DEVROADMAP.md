> **총 개발 기간: 20주 (5개월)**
>

---

## Phase 1: Foundation (Week 1-4) ✅ 진행중

### Week 1-2: Core Infrastructure ✅ 완료

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| 프로젝트 스케폴딩 | 🟢 완료 | P0 | - | Gradle 멀티모듈 6개 모듈 |
| 클린 아키텍처 기본 구조 | 🟢 완료 | P0 | - | Core/Application/Adapter/Infrastructure 레이어 |
| Port/Adapter 인터페이스 정의 | 🟢 완료 | P0 | - | Repository, EventPublisher 포트 |
| Docker 개발 환경 구성 | 🟢 완료 | P1 | - | MySQL, Redis, Kafka, HiveMQ |
| CI/CD 파이프라인 | ⬜ 대기 | P1 | - | GitHub Actions |

### Week 3-4: Robot Simulator MVP ✅ 완료

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| RobotWorker 엔티티 구현 | 🟢 완료 | P0 | - | 상태/스케줄/위치/건강/배터리 모델 |
| Virtual World 구현 | 🟢 완료 | P0 | - | 2D 그리드 맵, 8개 Zone 관리 |
| 로봇 행동 시뮬레이션 | 🟢 완료 | P0 | - | 작업/휴식/식사/이동/긴급/대피 |
| A* 경로 탐색 구현 | 🟢 완료 | P1 | - | PathFinder 알고리즘 |
| 이벤트 생성기 구현 | 🟢 완료 | P1 | - | 센서/위치/긴급 이벤트 |
| WebSocket 실시간 통신 | 🟢 완료 | P1 | - | STOMP 설정 완료 |
| 시나리오 엔진 | 🟢 완료 | P1 | - | 5개 시나리오 (daily/fire/fall/gas/load) |
| 시뮬레이션 대시보드 | ⬜ 대기 | P2 | - | 실시간 맵/상태 표시 (Frontend) |

#### 로봇 시뮬레이션 상세

```
Robot Worker 행동 모델:
├── WORKING (작업)     → 센서 데이터 생성, 위치 업데이트
├── RESTING (휴식)     → Zone E (휴게실) 이동
├── EATING (식사)      → Zone F (식당) 이동
├── MOVING (이동)      → A* 경로 탐색, 실시간 위치 전송
├── EMERGENCY (긴급)   → SOS/낙상/건강이상 이벤트
└── EVACUATING (대피)  → Zone H (대피소) 이동

테스트 시나리오:
├── daily_operation.yaml    → 500대 8시간 정규 운영
├── fire_emergency.yaml     → 화재 발생 대피 시나리오
├── worker_fall.yaml        → 작업자 낙상 감지
├── gas_leak.yaml           → 가스 누출 설비 차단
└── load_test.yaml          → 10,000대 부하 테스트
```

---

## Phase 2: Control System (Week 5-8)

### Week 5-6: Command Processing

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| Unified Gateway 구현 | ⬜ 대기 | P0 | - | 멀티 프로토콜 |
| 이벤트 수집 파이프라인 | ⬜ 대기 | P0 | - | Kafka |
| Hot/Warm/Cold Path 분리 | ⬜ 대기 | P0 | - | 경로 분리 |
| Redis 상태 캐시 | ⬜ 대기 | P1 | - | Cluster |
| Kafka 이벤트 스트림 | ⬜ 대기 | P1 | - | Producer/Consumer |

### Week 7-8: Task Dispatch

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| 분배 알고리즘 구현 | ⬜ 대기 | P0 | - | 최적화 |
| 경로 계획 (A* 등) | ⬜ 대기 | P0 | - | Path Finding |
| 충전 스케줄링 | ⬜ 대기 | P1 | - | 배터리 관리 |
| 카오스 시나리오 테스트 | ⬜ 대기 | P1 | - | 장애 테스트 |
| 성능 측정/튜닝 | ⬜ 대기 | P2 | - | 벤치마크 |

---

## Phase 3: SafetyKit (Week 9-12)

### Week 9-10: Hardware Prototype

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| ESP32 + SSR 회로 구성 | ⬜ 대기 | P0 | - | 회로 설계 |
| 기본 펀웨어 (릴레이 제어) | ⬜ 대기 | P0 | - | Arduino |
| MQTT 통신 구현 | ⬜ 대기 | P0 | - | HiveMQ |
| 센서 연동 (PIR, 초음파) | ⬜ 대기 | P1 | - | 센서 통합 |
| 3D 프린팅 케이스 | ⬜ 대기 | P2 | - | IP65 |

### Week 11-12: Integration

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| SafetyHub 서버 연동 | ⬜ 대기 | P0 | - | API 연동 |
| 원격 제어 기능 | ⬜ 대기 | P0 | - | 명령 처리 |
| 로컬 자율 판단 로직 | ⬜ 대기 | P1 | - | Edge AI |
| 시뮬레이터 어댑터 | ⬜ 대기 | P1 | - | 어댑터 구현 |
| E2E 테스트 | ⬜ 대기 | P2 | - | 통합 테스트 |

---

## Phase 4: LifeGuard (Week 13-16)

### Week 13-14: Hardware Prototype

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| nRF52840 + MAX30102 구성 | ⬜ 대기 | P0 | - | BLE 기반 |
| BLE 통신 구현 | ⬜ 대기 | P0 | - | BLE 5.0 |
| PPG 신호 수집 | ⬜ 대기 | P0 | - | 심박 측정 |
| 가속도계 낙상 감지 | ⬜ 대기 | P1 | - | MPU6050 |
| 배터리/충전 회로 | ⬜ 대기 | P1 | - | Li-Po |

### Week 15-16: Analytics & Alert

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| PTT 계산 알고리즘 | ⬜ 대기 | P0 | - | 맥파 전달 |
| 이상 감지 AI 모델 | ⬜ 대기 | P0 | - | ML 모델 |
| 스마트폰 앱 (React Native) | ⬜ 대기 | P0 | - | 크로스플랫폼 |
| 119 자동 신고 연동 | ⬜ 대기 | P1 | - | API 연동 |
| 보호자 알림 시스템 | ⬜ 대기 | P1 | - | Push/SMS |

---

## Phase 5: Integration & Polish (Week 17-20)

### Week 17-18: Full Integration

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| SafetyKit + LifeGuard 연동 | ⬜ 대기 | P0 | - | 통합 |
| 통합 대시보드 | ⬜ 대기 | P0 | - | 관제 센터 |
| 다중 현장 관리 | ⬜ 대기 | P1 | - | 멀티 사이트 |
| 알림/리포팅 시스템 | ⬜ 대기 | P1 | - | 자동화 |

### Week 19-20: Production Ready

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| 부하 테스트 (10,000+ 장치) | ⬜ 대기 | P0 | - | 성능 검증 |
| 보안 강화 (인증, 암호화) | ⬜ 대기 | P0 | - | Security |
| 문서화 | ⬜ 대기 | P1 | - | API Docs |
| 데모 시나리오 준비 | ⬜ 대기 | P1 | - | 시연 |
| 인증 준비 (KC 등) | ⬜ 대기 | P2 | - | 인증 서류 |

---

## 진행 상황 요약

| Phase | 기간 | 총 태스크 | 완료 | 진행률 |
| --- | --- | --- | --- | --- |
| Phase 1: Foundation | Week 1-4 | 13 | 12 | 92% |
| Phase 2: Control System | Week 5-8 | 10 | 0 | 0% |
| Phase 3: SafetyKit | Week 9-12 | 10 | 0 | 0% |
| Phase 4: LifeGuard | Week 13-16 | 10 | 0 | 0% |
| Phase 5: Integration | Week 17-20 | 9 | 0 | 0% |
| **전체** | **20주** | **52** | **12** | **23%** |

---

## 최근 완료 항목 (2026-01-14)

### 로봇 시뮬레이터 MVP 구현 (feat: Robot Simulator MVP)
- ✅ **RobotWorker 엔티티**: 상태(7종), 스케줄, 위치, 건강상태, 배터리 모델
- ✅ **Virtual World**: 100x50 그리드 맵, 8개 구역(Zone A-H)
  - 작업장(A,B,D), 위험구역(C), 휴게실(E), 식당(F), 의무실(G), 대피소(H)
- ✅ **로봇 행동 시뮬레이션**: WORKING/RESTING/EATING/MOVING/EMERGENCY/EVACUATING
- ✅ **A* 경로 탐색**: PathFinder 알고리즘 구현
- ✅ **이벤트 생성기**: 위치/상태/센서/긴급 이벤트 자동 생성
- ✅ **시나리오 엔진**: 5개 시나리오 지원
  - `daily_operation` - 일상 운영 (500대, 8시간)
  - `fire_emergency` - 화재 대피 시나리오
  - `worker_fall` - 작업자 낙상 감지
  - `gas_leak` - 가스 누출 설비 차단
  - `load_test` - 부하 테스트 (10,000대)
- ✅ **REST API 컨트롤러**: `/api/v1/simulation/*` 엔드포인트

### 의존성 수정 (fix: 누락된 Gradle 의존성 추가)
- ✅ `messaging` 모듈: `spring-boot-starter-json` 추가 - ObjectMapper 사용을 위한 Jackson 의존성
- ✅ `adapter-rest` 모듈: application UseCase 모듈 의존성 추가
  - `device-control`, `worker-monitoring`, `emergency-response`
- ✅ `adapter-mqtt` 모듈: `device-control` 의존성 추가 - DeviceControlUseCase 사용
- ✅ `adapter-simulator` 모듈: `device-control`, `spring-boot-starter-web` 추가
- ✅ `bootstrap` 모듈: `testcontainers:junit-jupiter` 추가 - JUnit5 통합 테스트 지원

---

## 완료 항목 (2026-01-13)

- ✅ Gradle 멀티모듈 프로젝트 구조 (6개 모듈)
- ✅ 클린 아키텍처 레이어 분리
- ✅ 도메인 모델 구현 (Device, Worker, Zone, Emergency)
- ✅ UseCase 인터페이스 및 서비스 구현
- ✅ REST API Controller 구현
- ✅ JPA Entity 및 Repository 구현
- ✅ Docker Compose 로컬 개발 환경 (MySQL, Redis, Kafka, HiveMQ)
- ✅ WebSocket STOMP 설정

---

## 상태 정의

| 상태 | 설명 |
| --- | --- |
| ⬜ 대기 | 시작 전 |
| 🟡 진행중 | 작업 진행 중 |
| 🟢 완료 | 완료됨 |
| 🔴 차단 | 차단됨 (도움 필요) |

---

**문서 버전:** v1.3

**최종 수정:** 2026-01-14