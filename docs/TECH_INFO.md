## Backend

| 영역 | 기술 | 버전 | 선택 이유 |
| --- | --- | --- | --- |
| Framework | Spring Boot + WebFlux | 3.3.5 | 비동기 처리, 고성능 |
| Language | Java | 21 (LTS) | Virtual Threads - 대량 동시 연결 처리 |
| 빌드 도구 | Gradle | 8.5 | Groovy DSL, 멀티모듈 |
| 실시간 통신 | Netty + MQTT (HiveMQ) | - | IoT 장치 연결 최적화 |
| 메시징 | Apache Kafka | 3.7.x | 이벤트 소싱, 높은 TPS |
| 캐시 | Redis | 7.x | 상태 실시간 조회 |
| DB | MySQL | 8.x | 메인 데이터 저장 |
| ORM | Spring Data JPA | - | 데이터 영속화 |

---

## Frontend

| 영역 | 기술 | 버전 | 비고 |
| --- | --- | --- | --- |
| Framework | React | 18 | TypeScript 기반 |
| 상태관리 | Zustand | - | 경량 상태 관리 |
| 실시간 | WebSocket (STOMP) | - | 양방향 통신 |
| 시각화 | D3.js, Recharts | - | 데이터 시각화 |
| 지도 | Leaflet / Mapbox | - | 위치 기반 모니터링 |

---

## Hardware - SafetyKit

| 영역 | 기술 | 상세 |
| --- | --- | --- |
| MCU | ESP32-WROOM-32 | 240MHz Dual Core, WiFi+BLE |
| 릴레이 | SSR (Solid State Relay) | 40A 내전압, 무접점 |
| 통신 | WiFi, LoRa, 4G | 다중 통신 지원 |
| 센서 | PIR, 초음파, CT | 모션/거리/전류 감지 |

### BOM (Bill of Materials)

| 부품 | 단가 |
| --- | --- |
| ESP32 모듈 | 8,000원 |
| SSR 40A | 15,000원 |
| 전류 센서 (CT) | 5,000원 |
| PIR 모션 센서 | 3,000원 |
| 초음파 거리 센서 | 5,000원 |
| 전원부 (AC-DC 5V) | 5,000원 |
| 케이스 (IP65) | 15,000원 |
| 커넥터, PCB, 기타 | 10,000원 |
| **원가 합계** | **66,000원** |
| **판매가** | **200,000원** |

---

## Hardware - LifeGuard Band

| 영역 | 기술 | 상세 |
| --- | --- | --- |
| MCU | nRF52840 | BLE 5.0, 저전력 |
| 심박 | MAX30102 | PPG 광학 심박 센서 |
| 모션 | MPU6050 | 6축 가속도/자이로 |
| 배터리 | Li-Po | 200mAh |

### BOM (Band 1개)

| 부품 | 단가 |
| --- | --- |
| nRF52840 (BLE SoC) | 8,000원 |
| MAX30102 (PPG) | 5,000원 |
| MPU6050 | 3,000원 |
| 온도 센서 | 1,000원 |
| 배터리 (200mAh) | 5,000원 |
| 밴드 (실리콘) | 3,000원 |
| PCB, 케이스, 기타 | 5,000원 |
| **Band 1개 원가** | **30,000원** |
| **세트 (2개) 원가** | **60,000원** |
| **판매가 (세트)** | **150,000~200,000원** |

---

## DevOps & Infrastructure

| 영역 | 기술 |
| --- | --- |
| 컨테이너 | Docker |
| 오케스트레이션 | Kubernetes |
| IaC | Terraform |
| CI/CD | GitHub Actions |
| 모니터링 | Prometheus + Grafana |
| 로깅 | ELK Stack |

---

## Docker Compose 서비스

| 서비스 | 이미지 | 포트 | 용도 |
| --- | --- | --- | --- |
| mysql | mysql:8.0 | 3306 | 메인 데이터베이스 |
| redis | redis:7-alpine | 6379 | 캐시/실시간 상태 |
| zookeeper | confluentinc/cp-zookeeper:7.5.0 | 2181 | Kafka 의존성 |
| kafka | confluentinc/cp-kafka:7.5.0 | 9092, 29092 | 이벤트 스트림 |
| hivemq | hivemq/hivemq-ce:2024.3 | 1883, 8083 | MQTT Broker |
| prometheus | prom/prometheus:v2.47.0 | 9090 | 메트릭 수집 (선택) |
| grafana | grafana/grafana:10.2.0 | 3000 | 모니터링 대시보드 (선택) |

---

**문서 버전:** v1.1

**최종 수정:** 2026-01-13