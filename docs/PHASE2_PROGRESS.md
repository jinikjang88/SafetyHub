# Phase 2 진행 상황 기록

> **작업 기간:** 2026-01-16 ~
> **목표:** Control System 구축 (Unified Gateway, Event Pipeline, Task Dispatch)

---

## 📋 Phase 2 개요

Phase 2는 SafetyHub의 핵심 관제 시스템을 구축하는 단계입니다.

### 주요 목표

```
┌─────────────────────────────────────────────────────────────────┐
│                     Phase 2 Architecture                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐                   │
│  │   MQTT    │  │ WebSocket │  │   REST    │                   │
│  │  (Device) │  │  (Robot)  │  │   (API)   │                   │
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘                   │
│        └──────────────┼──────────────┘                          │
│                       │                                          │
│                       ▼                                          │
│          ┌────────────────────────┐                             │
│          │   Unified Gateway      │  ← 멀티 프로토콜 통합       │
│          │   (Message Router)     │                             │
│          └────────────┬───────────┘                             │
│                       │                                          │
│         ┌─────────────┼─────────────┐                           │
│         │             │             │                            │
│         ▼             ▼             ▼                            │
│    ┌────────┐   ┌─────────┐   ┌────────┐                       │
│    │🔴 Hot  │   │🟡 Warm  │   │🔵 Cold │                       │
│    │< 10ms  │   │< 500ms  │   │ Async  │                       │
│    └────────┘   └─────────┘   └────────┘                       │
│    • 긴급정지    • 태스크분배   • 로그저장                        │
│    • 충돌회피    • 경로계산     • 통계분석                        │
│    • 119신고     • 알림발송     • 리포팅                          │
│                                                                  │
│                       ▼                                          │
│          ┌────────────────────────┐                             │
│          │   Event Stream (Kafka) │                             │
│          └────────────────────────┘                             │
│                       │                                          │
│                       ▼                                          │
│          ┌────────────────────────┐                             │
│          │   State Cache (Redis)  │                             │
│          └────────────────────────┘                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📌 개발 가이드라인

> **Phase 2 개발 원칙 및 작업 방식**

### 작업 방식
1. **점진적 개발**
   - 큰 작업은 작은 단위로 나누어 진행
   - 각 작업 전후로 리포트 작성 (이 문서에 기록)
   - 1단계부터 순차적으로 작업

2. **협의 기반 진행**
   - 작업 범위가 큰 경우 사용자와 협의
   - 작업 계획 수립 후 승인 받고 진행
   - 하나씩 완료 후 다음 단계 진행

3. **일괄 커밋/푸시**
   - 각 Stage 또는 큰 작업 단위 완료 시 한 번에 커밋
   - 의미 있는 단위로 git 이력 관리
   - 커밋 메시지는 한글로 상세히 작성

### 개발 원칙
1. **보안 우선**
   - 입력 검증 필수 (null 체크, 크기 제한)
   - DoS 공격 방지 (타임아웃, 리소스 제한)
   - 민감정보 로깅 방지
   - 예외 처리 시 정보 노출 최소화

2. **코딩 규칙 준수**
   - Clean Architecture 레이어 분리
   - Port & Adapter 패턴 적용
   - 단방향 의존성 (Core ← Application ← Adapter)
   - 불변 객체 우선 (final fields)
   - 테스트 코드 필수 작성

3. **성능 고려**
   - Hot Path: < 10ms 목표
   - Warm Path: < 500ms 목표
   - Cold Path: 비동기 처리
   - 캐시 적극 활용

4. **문서화**
   - 작업 전: 할 일 목록 작성
   - 작업 후: 결과 리포트 작성
   - 보안 조치 명시
   - 테스트 커버리지 기록

---

## 📊 작업 체크리스트

### Week 5-6: Command Processing (이벤트 처리)

#### 1️⃣ Unified Gateway 구현
- [ ] **Gateway 인터페이스 설계**
  - [ ] ProtocolAdapter 인터페이스 정의
  - [ ] Message 통합 모델 설계
  - [ ] Gateway 라우팅 로직 구현

- [ ] **멀티 프로토콜 어댑터 구현**
  - [ ] MQTT Adapter (IoT 장치)
  - [ ] WebSocket Adapter (실시간 통신)
  - [ ] REST Adapter (API 호출)
  - [ ] Simulator Adapter (시뮬레이터)

#### 2️⃣ 이벤트 수집 파이프라인
- [ ] **Kafka Producer 구현**
  - [ ] 이벤트 발행 서비스
  - [ ] 토픽 구조 설계
  - [ ] 직렬화/역직렬화 설정

- [ ] **Kafka Consumer 구현**
  - [ ] 이벤트 소비자 그룹 설정
  - [ ] 에러 핸들링 및 재시도
  - [ ] DLQ (Dead Letter Queue) 구현

#### 3️⃣ Hot/Warm/Cold Path 분리
- [ ] **Hot Path (< 10ms)**
  - [ ] 긴급 정지 처리
  - [ ] 충돌 회피 로직
  - [ ] 119 자동 신고
  - [ ] In-Memory 처리 (Redis)

- [ ] **Warm Path (< 500ms)**
  - [ ] 태스크 분배 로직
  - [ ] 경로 계산 (A* 활용)
  - [ ] 알림 발송
  - [ ] DB 조회/업데이트

- [ ] **Cold Path (비동기)**
  - [ ] 로그 저장 (Kafka → DB)
  - [ ] 통계 분석
  - [ ] 리포팅
  - [ ] 배치 처리

#### 4️⃣ Redis 상태 캐시
- [ ] **Redis 캐시 구조 설계**
  - [ ] 키 네이밍 전략
  - [ ] TTL 정책 수립
  - [ ] 캐시 무효화 전략

- [ ] **캐시 레이어 구현**
  - [ ] Robot 상태 캐싱
  - [ ] Zone 상태 캐싱
  - [ ] Device 상태 캐싱
  - [ ] 캐시 업데이트 리스너

#### 5️⃣ Kafka 이벤트 스트림
- [ ] **토픽 설계**
  - [ ] `robot.events` - 로봇 이벤트
  - [ ] `device.events` - 장치 이벤트
  - [ ] `worker.events` - 작업자 이벤트
  - [ ] `emergency.events` - 긴급 이벤트

- [ ] **이벤트 스키마 정의**
  - [ ] EventEnvelope 공통 구조
  - [ ] 도메인별 이벤트 타입
  - [ ] Avro/JSON Schema

---

### Week 7-8: Task Dispatch (작업 분배)

#### 6️⃣ 분배 알고리즘 구현
- [ ] **작업 큐 관리**
  - [ ] Priority Queue 구현
  - [ ] Task Entity 모델
  - [ ] 우선순위 정책

- [ ] **분배 전략**
  - [ ] Round-Robin 분배
  - [ ] Load-Based 분배
  - [ ] Location-Based 분배
  - [ ] Skill-Based 분배

#### 7️⃣ 경로 계획 통합
- [ ] **PathFinder 서비스화**
  - [ ] PathPlanning UseCase (이미 A* 구현됨)
  - [ ] 경로 최적화
  - [ ] 동적 장애물 회피

- [ ] **경로 캐싱**
  - [ ] 자주 사용하는 경로 캐싱
  - [ ] 경로 재사용 로직

#### 8️⃣ 충전 스케줄링
- [ ] **배터리 모니터링**
  - [ ] 배터리 레벨 추적
  - [ ] 저배터리 알림
  - [ ] 충전 필요 예측

- [ ] **충전 스케줄러**
  - [ ] 충전소 관리
  - [ ] 충전 대기열
  - [ ] 충전 시간 최적화

#### 9️⃣ 카오스 시나리오 테스트
- [ ] **장애 시나리오 구현**
  - [ ] MASS_FAILURE (30% 로봇 고장)
  - [ ] NETWORK_PARTITION (통신 두절)
  - [ ] TRAFFIC_SPIKE (대량 요청)
  - [ ] CHARGING_STATION_FAILURE (충전소 고장)

- [ ] **복구 로직 검증**
  - [ ] Failover 동작 확인
  - [ ] 작업 재분배 확인
  - [ ] 데이터 일관성 검증

#### 🔟 성능 측정/튜닝
- [ ] **벤치마크 설정**
  - [ ] JMH 벤치마크 작성
  - [ ] 응답 시간 측정
  - [ ] TPS (Throughput) 측정

- [ ] **최적화**
  - [ ] Hot Path 지연시간 최소화
  - [ ] DB 쿼리 최적화
  - [ ] 캐시 히트율 개선
  - [ ] 메모리 사용량 최적화

---

## 📐 작업 우선순위 제안

Phase 2는 범위가 크므로, 다음 순서로 진행을 제안합니다:

### 🎯 1단계: 핵심 인프라 (Week 5 전반)
```
우선순위: P0 (필수)
기간: 2-3일

1. Unified Gateway 인터페이스 설계
2. Hot/Warm/Cold Path 기본 구조
3. Kafka 이벤트 스트림 기본 구현
4. Redis 캐시 기본 구조
```

### 🎯 2단계: 이벤트 처리 파이프라인 (Week 5 후반)
```
우선순위: P0 (필수)
기간: 2-3일

1. Kafka Producer/Consumer 구현
2. 이벤트 수집 및 발행
3. 토픽 및 스키마 정의
4. 에러 핸들링
```

### 🎯 3단계: 상태 관리 (Week 6 전반)
```
우선순위: P1 (중요)
기간: 2일

1. Redis 캐시 레이어 완성
2. 로봇/장치 상태 캐싱
3. 캐시 무효화 로직
```

### 🎯 4단계: 작업 분배 시스템 (Week 6 후반 ~ Week 7)
```
우선순위: P0 (필수)
기간: 3-4일

1. 작업 큐 및 분배 알고리즘
2. PathFinder 서비스 통합
3. 충전 스케줄링
```

### 🎯 5단계: 테스트 및 최적화 (Week 8)
```
우선순위: P1 (중요)
기간: 3-4일

1. 카오스 시나리오 테스트
2. 성능 벤치마크
3. 튜닝 및 최적화
```

---

## ❓ 협의 필요 사항

### 1. 작업 시작 순서
각 단계가 2-4일 정도 소요될 것으로 예상됩니다. 어떤 단계부터 시작할까요?

**추천:** 1단계 (핵심 인프라)부터 시작

### 2. 구현 범위 조정
일부 기능은 Phase 3 이후로 미룰 수 있습니다:
- 카오스 시나리오 테스트 → Phase 5로 이동 가능
- 성능 튜닝 → 필요시 추가 가능
- 고급 분배 알고리즘 → 기본 Round-Robin만 우선 구현

### 3. 기술 스택 확인
- **Kafka:** Embedded Kafka vs Docker Kafka?
- **Redis:** Redis Standalone vs Redis Cluster?
- **직렬화:** JSON vs Avro?

---

## 📝 진행 로그

### 2026-01-16

#### 00:00 - Phase 2 계획 수립
- Phase 2 작업 범위 분석 완료
- PHASE2_PROGRESS.md 문서 생성
- 5단계 작업 계획 수립
- 사용자 협의 완료 → 1단계부터 순차적 진행 결정

#### 01:00 - 1-1. Gateway 도메인 모델 설계 완료 ✅

**생성 파일:**
- `/backend/safetyhub-core/src/main/java/com/safetyhub/core/gateway/Protocol.java`
- `/backend/safetyhub-core/src/main/java/com/safetyhub/core/gateway/MessageType.java`
- `/backend/safetyhub-core/src/main/java/com/safetyhub/core/gateway/MessageEnvelope.java`
- `/backend/safetyhub-core/src/test/java/com/safetyhub/core/gateway/MessageEnvelopeTest.java`

**구현 내용:**

1. **Protocol Enum (프로토콜 타입)**
   - MQTT: IoT 장치 (SafetyKit, LifeGuard)
   - WEBSOCKET: 실시간 양방향 통신 (로봇, 대시보드)
   - REST: 일반 API 호출
   - GRPC: 고성능 내부 서비스 통신 (미래 확장)
   - SIMULATOR: 개발/테스트 환경

2. **MessageType Enum (메시지 타입)**
   - COMMAND: 외부 → 내부 명령 (설비 제어, 작업 지시)
   - EVENT: 이벤트 알림 (센서 데이터, 상태 변경)
   - QUERY: 상태 조회 요청
   - RESPONSE: 쿼리 응답
   - HEARTBEAT: 연결 유지 및 생존 확인

3. **MessageEnvelope (통합 메시지 봉투)**
   - 불변 객체 설계 (final fields, Builder 패턴)
   - 필수 필드: messageType, protocol, source, priority, payload
   - 선택 필드: messageId (자동 생성), correlationId, target, timestamp (자동 생성), metadata
   - 페이로드 크기 제한: 1MB (DoS 공격 방지)
   - Hot/Warm/Cold Path 판별 메서드
   - 응답 메시지 생성 헬퍼 메서드

**보안 조치:**
- ✅ 입력 검증: 필수 필드 null 체크, 빈 문자열 검증
- ✅ 페이로드 크기 제한 (1MB) - DoS 방지
- ✅ 불변 객체 (final fields) - 스레드 안전성
- ✅ 민감정보 로깅 방지 (`@ToString(exclude = "payload")`)
- ✅ 빌더 패턴으로 필수 필드 강제
- ✅ 메시지 ID 자동 생성 (UUID)

**테스트 커버리지:**
- 총 24개 테스트 케이스 작성
- 정상 생성 테스트 (2개)
- 필수 필드 검증 테스트 (6개)
- 보안 검증 테스트 (2개)
- 응답 메시지 생성 테스트 (1개)
- 유틸리티 메서드 테스트 (7개)
- 다양한 프로토콜 테스트 (3개)

#### 02:00 - 1-2. ProtocolAdapter 인터페이스 정의 완료 ✅

**생성 파일:**
- `/backend/safetyhub-core/src/main/java/com/safetyhub/core/gateway/ProtocolAdapter.java`
- `/backend/safetyhub-core/src/main/java/com/safetyhub/core/gateway/AbstractProtocolAdapter.java`
- `/backend/safetyhub-core/src/main/java/com/safetyhub/core/gateway/MessageConversionException.java`
- `/backend/safetyhub-adapter/adapter-simulator/src/main/java/com/safetyhub/adapter/simulator/SimulatorProtocolAdapter.java`
- `/backend/safetyhub-core/src/test/java/com/safetyhub/core/gateway/ProtocolAdapterTest.java`

**구현 내용:**

1. **ProtocolAdapter 인터페이스 (Port & Adapter 패턴의 핵심)**
   - `toEnvelope(T message)`: 프로토콜별 메시지 → MessageEnvelope 변환
   - `fromEnvelope(MessageEnvelope envelope)`: MessageEnvelope → 프로토콜별 메시지 변환
   - `getSupportedProtocol()`: 지원 프로토콜 타입 반환
   - `supports(T message)`: 메시지 지원 여부 확인

2. **AbstractProtocolAdapter 추상 클래스**
   - ProtocolAdapter 구현을 위한 기본 기능 제공
   - 공통 검증 로직 (null 체크, 페이로드 크기 검증)
   - 예외 처리 및 래핑 (MessageConversionException)
   - 템플릿 메서드 패턴: `doToEnvelope()`, `doFromEnvelope()` 구현 위임

3. **MessageConversionException**
   - 메시지 변환 실패 시 발생하는 예외
   - 프로토콜 정보 포함
   - 원인 예외 체이닝

4. **SimulatorProtocolAdapter (예제 구현)**
   - JSON 기반 메시지 변환
   - Jackson ObjectMapper 사용
   - 개발/테스트 환경에서 사용

**보안 조치:**
- ✅ 입력 검증: null 체크, 메시지 타입 검증
- ✅ 페이로드 크기 검증 (1MB 제한)
- ✅ 예외 처리: 민감정보 노출 방지
- ✅ 프로토콜 일치 검증
- ✅ 템플릿 메서드 패턴으로 안전한 확장
- ✅ final 메서드로 검증 로직 우회 방지

**설계 원칙:**
- Port & Adapter 패턴: Core는 Adapter를 모름
- 단방향 의존성: Adapter → Core
- 느슨한 결합: 프로토콜 변경이 Core에 영향 없음
- 확장 가능: 새 프로토콜 추가 시 인터페이스만 구현

**테스트 커버리지:**
- 총 20개 테스트 케이스 작성
- toEnvelope 테스트 (3개)
- fromEnvelope 테스트 (3개)
- 프로토콜 지원 테스트 (2개)
- supports 테스트 (2개)
- 페이로드 크기 검증 (2개)
- MessageConversionException 테스트 (4개)

#### 03:00 - 1-3. MessageRouter 개선 (Hot/Warm/Cold Path 분리) 완료 ✅

**생성 파일:**
- `/backend/safetyhub-core/src/main/java/com/safetyhub/core/gateway/MessageHandler.java`
- `/backend/safetyhub-core/src/main/java/com/safetyhub/core/gateway/MessageHandlingException.java`
- `/backend/safetyhub-gateway/src/main/java/com/safetyhub/gateway/ImprovedMessageRouter.java`
- `/backend/safetyhub-gateway/src/main/java/com/safetyhub/gateway/handler/HotPathHandler.java`
- `/backend/safetyhub-gateway/src/main/java/com/safetyhub/gateway/handler/WarmPathHandler.java`
- `/backend/safetyhub-gateway/src/main/java/com/safetyhub/gateway/handler/ColdPathHandler.java`
- `/backend/safetyhub-gateway/src/test/java/com/safetyhub/gateway/ImprovedMessageRouterTest.java`

**구현 내용:**

1. **MessageHandler 인터페이스**
   - 함수형 인터페이스 (`@FunctionalInterface`)
   - `handle(MessageEnvelope envelope)`: 메시지 처리 메서드
   - Hot/Warm/Cold Path 핸들러 추상화

2. **MessageHandlingException**
   - 메시지 처리 실패 시 발생하는 커스텀 예외
   - Path 정보 포함 (HOT/WARM/COLD)
   - 원인 예외 체이닝

3. **ImprovedMessageRouter (핵심)**
   - 우선순위 기반 메시지 라우팅
   - Hot Path (CRITICAL, HIGH): 동기, < 10ms
   - Warm Path (NORMAL): 동기, < 500ms
   - Cold Path (LOW): 비동기, 응답 시간 제약 없음
   - 전략 패턴: 핸들러 교체 가능
   - 성능 모니터링: 목표 시간 초과 경고 로깅

4. **HotPathHandler (긴급 처리)**
   - 긴급 정지, 충돌 회피, 119 신고
   - In-Memory 처리 (Redis)
   - DB 접근 최소화
   - 빠른 실패 (Fail-Fast)

5. **WarmPathHandler (일반 처리)**
   - 태스크 분배, 경로 계산, 알림 발송
   - DB 읽기/쓰기 허용
   - Redis 캐시 활용
   - 복잡한 비즈니스 로직

6. **ColdPathHandler (배치 처리)**
   - 로그 저장, 통계 분석, 리포팅
   - 비동기 처리
   - 배치 최적화
   - 에러가 메인 플로우에 영향 없음

**보안 조치:**
- ✅ 입력 검증: null 체크, 핸들러 검증
- ✅ 예외 처리: 민감정보 노출 방지
- ✅ 스레드 안전성: 불변 객체, ExecutorService
- ✅ 리소스 관리: shutdown() 메서드로 정리
- ✅ 에러 격리: Cold Path 에러가 메인 플로우에 영향 없음

**설계 패턴:**
- 전략 패턴: Path별 핸들러 교체 가능
- 템플릿 메서드 패턴: 공통 로직 재사용
- 의존성 주입: 생성자 주입으로 느슨한 결합

**성능 최적화:**
- Hot Path: In-Memory 처리, 최소 로직
- Warm Path: DB 접근 허용, 캐시 활용
- Cold Path: 비동기 처리, 별도 스레드 풀
- 성능 모니터링: 목표 시간 초과 경고

**테스트 커버리지:**
- 총 18개 테스트 케이스 작성
- Hot Path 라우팅 (3개)
- Warm Path 라우팅 (2개)
- Cold Path 라우팅 (2개)
- 입력 검증 (4개)
- 성능 테스트 (2개)
- MessageHandlingException (4개)

#### 04:00 - 1-4. Kafka Producer 기본 구현 완료 ✅

**생성 파일:**
- `/backend/safetyhub-infrastructure/messaging/src/main/java/com/safetyhub/infrastructure/messaging/EventTopic.java`
- `/backend/safetyhub-infrastructure/messaging/src/main/java/com/safetyhub/infrastructure/messaging/EventPublisher.java`
- `/backend/safetyhub-infrastructure/messaging/src/main/java/com/safetyhub/infrastructure/messaging/EventPublishException.java`
- `/backend/safetyhub-infrastructure/messaging/src/main/java/com/safetyhub/infrastructure/messaging/kafka/KafkaEventPublisher.java`
- `/backend/safetyhub-infrastructure/messaging/src/main/java/com/safetyhub/infrastructure/messaging/kafka/KafkaEventMessage.java`
- `/backend/safetyhub-infrastructure/messaging/src/main/java/com/safetyhub/infrastructure/messaging/kafka/KafkaConfig.java`
- `/backend/safetyhub-infrastructure/messaging/src/test/java/com/safetyhub/infrastructure/messaging/kafka/KafkaEventPublisherTest.java`

**구현 내용:**

1. **EventTopic Enum (토픽 정의)**
   - ROBOT_EVENTS: 로봇 이벤트
   - DEVICE_EVENTS: 장치 이벤트 (SafetyKit)
   - WORKER_EVENTS: 작업자 이벤트 (LifeGuard)
   - EMERGENCY_EVENTS: 긴급 이벤트 (Hot Path)
   - SYSTEM_EVENTS: 시스템 이벤트
   - ANALYTICS_EVENTS: 분석용 이벤트 (Cold Path)

2. **EventPublisher 인터페이스 (Port)**
   - `publish(envelope)`: 토픽 자동 결정
   - `publish(topic, envelope)`: 토픽 명시
   - `publish(topic, partitionKey, envelope)`: 파티션 키 지정
   - 비동기 발행 (CompletableFuture)
   - 기술 독립적 인터페이스

3. **EventPublishException**
   - 이벤트 발행 실패 시 발생
   - 토픽 정보 포함
   - 원인 예외 체이닝

4. **KafkaEventPublisher (구현체)**
   - Spring Kafka 사용
   - JSON 직렬화
   - 토픽 자동 결정 로직
   - 파티션 키: 소스 ID (순서 보장)
   - 비동기 발행
   - 에러 처리 및 로깅

5. **KafkaEventMessage (DTO)**
   - MessageEnvelope ↔ Kafka 메시지 변환
   - JSON 직렬화 가능
   - payload를 Base64로 인코딩
   - 양방향 변환 지원

6. **KafkaConfig (설정)**
   - Producer 설정
   - acks=all (안정성)
   - retries=3 (재시도)
   - compression=lz4 (압축)
   - linger.ms=10 (배치 처리)
   - idempotence=true (멱등성)

**보안 조치:**
- ✅ 입력 검증: null 체크, 토픽/키/envelope 검증
- ✅ 직렬화 에러 처리
- ✅ 타임아웃 설정 (max.block.ms=5초)
- ✅ 민감정보 로깅 방지
- ✅ 멱등성 보장 (중복 전송 방지)

**토픽 전략:**
- 도메인별 분리 (확장성)
- 이벤트 타입별 분리 (필터링 용이)
- 파티션 키: 소스 ID (순서 보장)
- 긴급/분석 이벤트 별도 토픽

**성능 최적화:**
- 비동기 발행 (CompletableFuture)
- 배치 전송 (linger.ms=10)
- 압축 (lz4)
- 버퍼 메모리 (32MB)

**테스트 커버리지:**
- 총 14개 테스트 케이스 작성
- 토픽 자동 결정 (4개)
- 토픽 명시 (2개)
- 파티션 키 지정 (1개)
- 입력 검증 (3개)
- 메시지 변환 (2개)

#### 05:00 - 1-5. Redis 캐시 서비스 기본 구현 완료 ✅

**생성 파일:**
- `/backend/safetyhub-infrastructure/persistence/src/main/java/com/safetyhub/infrastructure/cache/CacheService.java`
- `/backend/safetyhub-infrastructure/persistence/src/main/java/com/safetyhub/infrastructure/cache/CacheKey.java`
- `/backend/safetyhub-infrastructure/persistence/src/main/java/com/safetyhub/infrastructure/cache/CacheTTL.java`
- `/backend/safetyhub-infrastructure/persistence/src/main/java/com/safetyhub/infrastructure/cache/redis/RedisCacheService.java`
- `/backend/safetyhub-infrastructure/persistence/src/main/java/com/safetyhub/infrastructure/cache/redis/CacheSerializationException.java`
- `/backend/safetyhub-infrastructure/persistence/src/main/java/com/safetyhub/infrastructure/cache/redis/RedisConfig.java`
- `/backend/safetyhub-infrastructure/persistence/src/test/java/com/safetyhub/infrastructure/cache/redis/RedisCacheServiceTest.java`

**구현 내용:**

1. **CacheService 인터페이스 (Port)**
   - `put(key, value)`: 캐시 저장
   - `put(key, value, ttl)`: TTL과 함께 저장
   - `get(key, type)`: 타입 안전 조회
   - `exists(key)`: 키 존재 확인
   - `delete(key)`: 키 삭제
   - `deleteByPattern(pattern)`: 패턴 기반 삭제
   - `keys(pattern)`: 패턴 기반 조회
   - `expire(key, ttl)`: TTL 설정
   - `getExpire(key)`: 남은 TTL 조회

2. **CacheKey (키 네이밍 유틸리티)**
   - 일관된 키 네이밍: `{domain}:{type}:{id}`
   - 키 생성 메서드:
     - `robotState(id)`: robot:state:{id}
     - `robotLocation(id)`: robot:location:{id}
     - `robotBattery(id)`: robot:battery:{id}
     - `deviceState(id)`: device:state:{id}
     - `workerLocation(id)`: worker:location:{id}
     - `workerHealth(id)`: worker:health:{id}
     - `heartbeat(id)`: heartbeat:{id}
     - `emergency(id)`: emergency:{id}
   - 패턴 생성: `allByDomain()`, `allByDomainAndType()`
   - 키 검증: 길이 제한 (512자), 특수문자 제한

3. **CacheTTL (TTL 정책)**
   - ROBOT_STATE: 1분
   - ROBOT_LOCATION: 30초 (실시간 추적)
   - ROBOT_BATTERY: 5분
   - DEVICE_STATE: 2분
   - WORKER_LOCATION: 30초
   - WORKER_HEALTH: 1분
   - HEARTBEAT: 1분 (타임아웃 감지)
   - EMERGENCY: 10분
   - SESSION: 30분
   - DEFAULT: 5분

4. **RedisCacheService (구현체)**
   - Spring Data Redis 사용
   - JSON 직렬화/역직렬화
   - 타입 안전 조회 (제네릭)
   - 에러 처리: 역직렬화 실패 시 캐시 삭제
   - 로깅: trace 레벨

5. **CacheSerializationException**
   - JSON 변환 실패 시 발생
   - 원인 예외 체이닝

6. **RedisConfig (설정)**
   - Lettuce 사용 (비동기, 스레드 안전)
   - String 직렬화 (키, 값)
   - host: localhost
   - port: 6379
   - database: 0

**보안 조치:**
- ✅ 입력 검증: null 체크, 빈 문자열 체크
- ✅ 키 검증: 길이 제한, 특수문자 제한
- ✅ 직렬화 에러 처리
- ✅ 손상된 캐시 자동 삭제
- ✅ 민감정보 로깅 방지 (trace 레벨)
- ✅ TTL 자동 만료 (보안 강화)

**키 네이밍 전략:**
- 일관성: {domain}:{type}:{id}
- 가독성: robot:state:robot-001
- 패턴 삭제: robot:state:*
- 충돌 방지: 도메인 분리

**TTL 정책:**
- Hot 데이터: 30초~1분 (실시간)
- Warm 데이터: 2~5분 (중간)
- Cold 데이터: 10~30분 (장기)

**성능 최적화:**
- Lettuce 사용 (비동기 I/O)
- Connection Pool
- JSON 직렬화 (가벼움)

**테스트 커버리지:**
- 총 20개 테스트 케이스 작성
- put 테스트 (5개)
- get 테스트 (3개)
- exists 테스트 (2개)
- delete 테스트 (2개)
- keys 테스트 (1개)
- expire 테스트 (2개)
- CacheKey 테스트 (5개)

---

## 📊 Phase 2 - 1단계 완료 요약

### ✅ 완료된 모든 작업 (5/5)

1. ✅ **1-1. Gateway 도메인 모델 설계**
   - Protocol, MessageType, MessageEnvelope
   - 24개 테스트 케이스

2. ✅ **1-2. ProtocolAdapter 인터페이스 정의**
   - ProtocolAdapter, AbstractProtocolAdapter
   - SimulatorProtocolAdapter 예제
   - 20개 테스트 케이스

3. ✅ **1-3. MessageRouter 개선**
   - ImprovedMessageRouter (Hot/Warm/Cold Path)
   - HotPathHandler, WarmPathHandler, ColdPathHandler
   - 18개 테스트 케이스

4. ✅ **1-4. Kafka Producer 기본 구현**
   - EventPublisher, KafkaEventPublisher
   - 6개 토픽, JSON 직렬화
   - 14개 테스트 케이스

5. ✅ **1-5. Redis 캐시 서비스 기본 구현**
   - CacheService, RedisCacheService
   - 키 네이밍, TTL 정책
   - 20개 테스트 케이스

### 📈 통계

- **총 파일:** 30개
- **총 테스트:** 96개
- **보안 조치:** 30+ 항목
- **설계 패턴:** Port & Adapter, 전략, 템플릿 메서드

---

## 📋 Stage 2 작업 계획 (Week 7-8: Task Dispatch)

> **작성일:** 2026-01-16
> **상태:** 계획 수립 완료, 구현 대기 중

### 목표
로봇 작업 분배 시스템 구축 및 성능 최적화

---

### 6️⃣ 분배 알고리즘 구현

#### 작업 큐 관리
```
[ ] Priority Queue 구현
    - 우선순위 기반 작업 대기열
    - 긴급 작업 우선 처리

[ ] Task Entity 모델
    - 작업 정보 구조 설계 (ID, 타입, 위치, 우선순위)
    - 작업 상태 관리 (PENDING, ASSIGNED, IN_PROGRESS, COMPLETED)

[ ] 우선순위 정책
    - 긴급도 기반 우선순위
    - 작업 대기 시간 고려
```

#### 분배 전략
```
[ ] Round-Robin 분배 (P0 - 필수)
    - 로봇에 순차적으로 작업 할당
    - 가장 단순한 분배 방식

[ ] Load-Based 분배 (P2 - 선택)
    - 로봇의 현재 작업 부하 고려
    - 유휴 로봇 우선 할당

[ ] Location-Based 분배 (P1 - 중요)
    - 작업 위치와 로봇 위치 거리 계산
    - 가장 가까운 로봇 할당

[ ] Skill-Based 분배 (P2 - 선택)
    - 작업 유형에 맞는 로봇 능력 매칭
    - 로봇 스킬셋 관리
```

**예상 결과물:**
- Task.java (도메인 모델)
- TaskStatus.java (상태 enum)
- TaskPriority.java (우선순위 정책)
- TaskQueue.java (우선순위 큐)
- DispatchStrategy.java (전략 인터페이스)
- RoundRobinStrategy.java (구현체)
- LocationBasedStrategy.java (구현체)

---

### 7️⃣ 경로 계획 통합

#### PathFinder 서비스화
```
[ ] PathPlanning UseCase 활용
    - Phase 1에서 이미 A* 알고리즘 구현 완료
    - 기존 PathFinder를 서비스로 통합

[ ] 경로 최적화
    - 최단 경로 계산
    - 복수 경로 평가 및 선택

[ ] 동적 장애물 회피
    - 실시간 장애물 감지
    - 경로 재계산 로직
```

#### 경로 캐싱
```
[ ] 자주 사용하는 경로 캐싱
    - Redis에 경로 저장
    - 캐시 키: path:{startZone}:{endZone}
    - TTL: 5분 (지도 변경 고려)

[ ] 경로 재사용 로직
    - 캐시 히트 시 재계산 생략
    - 성능 개선 (경로 계산은 비용 높음)
```

**예상 결과물:**
- PathService.java (서비스 인터페이스)
- PathServiceImpl.java (구현체)
- CachedPathService.java (캐싱 데코레이터)
- PathCache.java (캐시 관리)

---

### 8️⃣ 충전 스케줄링

#### 배터리 모니터링
```
[ ] 배터리 레벨 추적 (P0 - 필수)
    - 실시간 배터리 상태 조회
    - Redis 캐시 활용 (robot:battery:{id})

[ ] 저배터리 알림 (P0 - 필수)
    - 임계값 설정 (예: 20%)
    - Hot Path 이벤트 발행

[ ] 충전 필요 예측 (P1 - 중요)
    - 작업 완료까지 필요한 배터리 계산
    - 예방적 충전 스케줄링
```

#### 충전 스케줄러
```
[ ] 충전소 관리 (P1 - 중요)
    - 충전소 위치 및 상태
    - 사용 가능 충전 슬롯 관리

[ ] 충전 대기열 (P1 - 중요)
    - 우선순위 기반 대기열
    - 긴급 작업 중인 로봇 우선

[ ] 충전 시간 최적화 (P2 - 선택)
    - 작업 스케줄과 충전 시간 조율
    - 비어있는 시간대 활용
```

**예상 결과물:**
- BatteryMonitor.java (모니터링 서비스)
- ChargingStation.java (도메인 모델)
- ChargingScheduler.java (스케줄러)
- ChargingQueue.java (대기열 관리)

---

### 9️⃣ 카오스 시나리오 테스트 (P2 - 선택)

#### 장애 시나리오 구현
```
[ ] MASS_FAILURE
    - 30% 로봇 동시 고장 시뮬레이션
    - 작업 재분배 동작 확인

[ ] NETWORK_PARTITION
    - 일부 로봇 통신 두절
    - Heartbeat 타임아웃 처리

[ ] TRAFFIC_SPIKE
    - 대량 요청 동시 발생
    - Hot/Warm Path 부하 테스트

[ ] CHARGING_STATION_FAILURE
    - 충전소 고장
    - 대체 충전소 할당
```

#### 복구 로직 검증
```
[ ] Failover 동작 확인
    - 고장 로봇 자동 감지
    - 작업 자동 재할당

[ ] 작업 재분배 확인
    - 실패한 작업 복구
    - 데이터 일관성 유지

[ ] 데이터 일관성 검증
    - 캐시-DB 동기화
    - 이벤트 순서 보장
```

**예상 결과물:**
- ChaosScenario.java (시나리오 인터페이스)
- MassFailureScenario.java
- NetworkPartitionScenario.java
- TrafficSpikeScenario.java
- ChargingStationFailureScenario.java

---

### 🔟 성능 측정/튜닝 (P1 - 중요)

#### 벤치마크 설정
```
[ ] JMH 벤치마크 작성
    - 분배 알고리즘 성능 측정
    - 경로 계산 성능 측정

[ ] 응답 시간 측정
    - Hot Path: < 10ms 검증
    - Warm Path: < 500ms 검증

[ ] TPS (Throughput) 측정
    - 초당 처리 가능한 작업 수
    - 목표: 1000+ TPS
```

#### 최적화
```
[ ] Hot Path 지연시간 최소화
    - 불필요한 로직 제거
    - 캐시 적중률 개선

[ ] DB 쿼리 최적화
    - 인덱스 추가
    - N+1 쿼리 문제 해결

[ ] 캐시 히트율 개선
    - TTL 정책 조정
    - 캐시 워밍 전략

[ ] 메모리 사용량 최적화
    - 객체 풀링
    - 불필요한 객체 생성 제거
```

**예상 결과물:**
- DispatchBenchmark.java (JMH)
- PathPlanningBenchmark.java (JMH)
- PerformanceTest.java
- LoadTest.java

---

### 📊 Stage 2 예상 통계

- **예상 파일:** 35개
- **예상 테스트:** 80개
- **예상 기간:** 7-10일
- **우선순위:**
  - P0 (필수): Round-Robin 분배, PathFinder 통합, 배터리 모니터링
  - P1 (중요): Location-Based 분배, 충전 스케줄러, 성능 측정
  - P2 (선택): 고급 분배 전략, 카오스 테스트

---

### 🔗 Phase 1 연계 포인트

**활용 가능한 기존 구현:**
1. **PathFinder (A* 알고리즘)** ← Phase 1 완료
   - `backend/safetyhub-application/src/main/java/com/safetyhub/application/usecase/robot/PathFinder.java`
   - 그대로 서비스로 통합 가능

2. **Robot 도메인 모델** ← Phase 1 완료
   - 배터리 상태, 위치 정보 이미 존재
   - 확장만 하면 됨

3. **Zone 맵** ← Phase 1 완료
   - 경로 계산에 필요한 지도 데이터
   - PathFinder가 이미 활용 중

**Phase 2-1 (Stage 1) 연계:**
1. **MessageRouter** → 긴급 작업은 Hot Path 처리
2. **Kafka EventPublisher** → 작업 분배 이벤트 발행
3. **Redis CacheService** → 로봇 상태, 작업 큐 캐싱

---

### ❓ 다음 단계 협의 필요

1. **시작 작업 선택**
   - 추천: 6️⃣ 분배 알고리즘 (Round-Robin부터)
   - 또는: 7️⃣ 경로 계획 통합 (기존 코드 활용)

2. **구현 범위**
   - 전체 구현 vs P0만 우선
   - 카오스 테스트 포함 여부

3. **작업 분할 방식**
   - Stage 2를 여러 세션에 나누어 진행
   - 각 작업을 독립적으로 커밋

---

## 🔗 참고 문서

- [DEVROADMAP.md](./DEVROADMAP.md) - 전체 개발 로드맵
- [PHASE1_PROGRESS.md](./PHASE1_PROGRESS.md) - Phase 1 진행 기록
- [ACHITECTURE.md](./ACHITECTURE.md) - 아키텍처 설계
- [TECH_INFO.md](./TECH_INFO.md) - 기술 스택 정보

---

## 📝 세션 정보

### 현재 세션
- **목적:** Phase 2 구현을 위한 준비 세션
- **완료 작업:**
  - ✅ Phase 2 Stage 1 완료 (5개 작업, 30개 파일, 96개 테스트)
  - ✅ Stage 2 작업 계획 수립 및 문서화
  - ✅ 개발 가이드라인 문서화
- **다음 세션 시작점:** Stage 2 - 6️⃣ 분배 알고리즘 구현부터 시작

### 세션 이력
- **Session 1 (2026-01-16):** Phase 2 계획 수립 및 Stage 1 완료

---

**문서 버전:** v1.1
**최종 업데이트:** 2026-01-16 (Stage 2 계획 추가)
