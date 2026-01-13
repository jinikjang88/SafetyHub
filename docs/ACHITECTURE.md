## 클린 아키텍처 원칙

SafetyHub는 클린 아키텍처(Clean Architecture) 원칙에 따라 설계되었습니다.

### 레이어 구조

| 레이어 | 설명 | 구성 요소 |
| --- | --- | --- |
| **Enterprise Business Rules** | 핵심 도메인 로직 | Entities, Domain Events |
| **Application Business Rules** | 애플리케이션 유스케이스 | Use Cases |
| **Interface Adapters** | 외부 인터페이스 어댑터 | Controllers, Gateways, Presenters |
| **Frameworks & Drivers** | 외부 프레임워크 | MQTT, REST, DB, Kafka |

**의존성 방향:** 바깥 → 안쪽 (내부 레이어는 외부를 모름)

---

## 뇌-신경계 아키텍처 비유

인간의 신경계처럼 상황의 긴급도에 따라 다른 경로로 처리합니다.

### 이벤트 처리 경로

| 경로 | 비유 | SLA | 대상 작업 | 처리 방식 |
| --- | --- | --- | --- | --- |
| 🔴 **Hot Path** | 척수 반사 | < 10ms | 긴급 정지, 충돌 회피, 119 신고 | In-Memory Only |
| 🟡 **Warm Path** | 대뇌 판단 | < 500ms | 태스크 분배, 경로 계산, 알림 발송 | Service + DB |
| 🔵 **Cold Path** | 기억 저장 | Async | 로그 저장, 통계 분석, 리포팅 | Kafka + Batch |

**설계 철학:**

- 뜨거우면 손을 떴다 (생각 안 함)
- 판단, 계획은 시간이 걸림
- 경험 저장은 느려도 됨

---

## Unified Safety Gateway

### 멀티 프로토콜 지원

| 프로토콜 | 어댑터 | 용도 |
| --- | --- | --- |
| MQTT | MQTT Adapter | IoT 장치 연결 |
| WebSocket | WebSocket Adapter | 실시간 대시보드 |
| REST | REST Adapter | 외부 API 연동 |
| gRPC | gRPC Adapter | 고성능 서비스 간 통신 |

### Port Layer

- 프로토콜 무관 인터페이스
- 모든 어댑터가 동일한 포트 계층을 통해 Core Domain과 통신
- 프로토콜 변경 시 어댑터만 교체

---

## 도메인 엔티티

| 엔티티 | 설명 | 주요 속성 |
| --- | --- | --- |
| **Device Entity** | 설비 장치 | deviceId, status, location, lastHeartbeat |
| **Worker Entity** | 작업자 | workerId, vitalSigns, location, emergencyContact |
| **Task Entity** | 작업 | taskId, priority, assignedDevice, deadline |
| **Alert Entity** | 경고 | alertId, severity, source, timestamp |

---

## Port & Adapter 패턴

시뮬레이터와 실제 로봇이 **같은 인터페이스**를 사용하므로, 어댑터만 교체하면 됩니다.

**프로파일 스위칭:**

- 개발: `spring.profiles.active=simulation`
- 운영: `spring.profiles.active=production`

---

## 인프라스트럭처 레이어

| 구성요소 | 기술 | 역할 |
| --- | --- | --- |
| 메인 DB | MySQL 8.x | 데이터 영속화 |
| 캐시 | Redis 7.x | 상태 실시간 조회 |
| 메시징 | Apache Kafka 3.7.x | 이벤트 스트림 |
| MQTT Broker | HiveMQ CE | IoT 장치 연결 |

---

## 구현된 모듈 구조

```
safetyhub-core/              → 도메인 모델 (Device, Worker, Zone, Emergency)
                             → 도메인 이벤트 (EmergencyDetectedEvent 등)
                             → 포트 인터페이스 (Repository, EventPublisher)

safetyhub-application/       → UseCase 구현
  ├── device-control/        → 설비 제어 서비스
  ├── worker-monitoring/     → 작업자 모니터링 서비스
  └── emergency-response/    → 긴급 대응 서비스 (Hot Path)

safetyhub-adapter/           → 외부 어댑터
  ├── adapter-rest/          → REST API (DeviceController, WorkerController, EmergencyController)
  ├── adapter-websocket/     → WebSocket (STOMP 실시간 통신)
  ├── adapter-mqtt/          → MQTT (IoT 장치 연결)
  └── adapter-simulator/     → 시뮬레이터 (개발/테스트용)

safetyhub-infrastructure/    → 인프라 구현
  ├── persistence/           → JPA Entity, Repository 구현 (MySQL)
  ├── messaging/             → KafkaEventPublisher
  └── external/              → Emergency119ApiClient

safetyhub-gateway/           → 통합 게이트웨이
                             → MessageRouter (Hot/Warm/Cold Path 라우팅)

safetyhub-bootstrap/         → 실행 애플리케이션
                             → SafetyHubApplication, application.yml
```

---

**문서 버전:** v1.1

**최종 수정:** 2026-01-13