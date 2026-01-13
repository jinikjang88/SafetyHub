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

### Week 3-4: Simulator MVP

| 태스크 | 상태 | 우선순위 | 담당 | 비고 |
| --- | --- | --- | --- | --- |
| Virtual Robot 구현 | ⬜ 대기 | P0 | - | 기본 로봇 엔티티 |
| Virtual World (그리드 맵) | ⬜ 대기 | P0 | - | 2D 그리드 |
| 기본 이벤트 발생 | ⬜ 대기 | P1 | - | 이동/충돌 |
| WebSocket 실시간 통신 | 🟢 완료 | P1 | - | STOMP 설정 완료 |
| 간단한 대시보드 | ⬜ 대기 | P2 | - | 위치 표시 |

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
| Phase 1: Foundation | Week 1-4 | 10 | 5 | 50% |
| Phase 2: Control System | Week 5-8 | 10 | 0 | 0% |
| Phase 3: SafetyKit | Week 9-12 | 10 | 0 | 0% |
| Phase 4: LifeGuard | Week 13-16 | 10 | 0 | 0% |
| Phase 5: Integration | Week 17-20 | 9 | 0 | 0% |
| **전체** | **20주** | **49** | **5** | **10%** |

---

## 최근 완료 항목 (2026-01-13)

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

**문서 버전:** v1.1

**최종 수정:** 2026-01-13