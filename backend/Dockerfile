# Build Stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Gradle 캐시 활용을 위해 의존성 파일 먼저 복사
COPY build.gradle settings.gradle gradle.properties ./
COPY safetyhub-core/build.gradle safetyhub-core/
COPY safetyhub-application/build.gradle safetyhub-application/
COPY safetyhub-application/device-control/build.gradle safetyhub-application/device-control/
COPY safetyhub-application/worker-monitoring/build.gradle safetyhub-application/worker-monitoring/
COPY safetyhub-application/emergency-response/build.gradle safetyhub-application/emergency-response/
COPY safetyhub-adapter/build.gradle safetyhub-adapter/
COPY safetyhub-adapter/adapter-rest/build.gradle safetyhub-adapter/adapter-rest/
COPY safetyhub-adapter/adapter-websocket/build.gradle safetyhub-adapter/adapter-websocket/
COPY safetyhub-adapter/adapter-mqtt/build.gradle safetyhub-adapter/adapter-mqtt/
COPY safetyhub-adapter/adapter-simulator/build.gradle safetyhub-adapter/adapter-simulator/
COPY safetyhub-infrastructure/build.gradle safetyhub-infrastructure/
COPY safetyhub-infrastructure/persistence/build.gradle safetyhub-infrastructure/persistence/
COPY safetyhub-infrastructure/messaging/build.gradle safetyhub-infrastructure/messaging/
COPY safetyhub-infrastructure/external/build.gradle safetyhub-infrastructure/external/
COPY safetyhub-gateway/build.gradle safetyhub-gateway/
COPY safetyhub-bootstrap/build.gradle safetyhub-bootstrap/

# 의존성 다운로드
RUN gradle dependencies --no-daemon || true

# 소스코드 복사
COPY . .

# 빌드
RUN gradle :safetyhub-bootstrap:bootJar --no-daemon -x test

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 비root 사용자 생성
RUN addgroup -g 1000 safetyhub && \
    adduser -u 1000 -G safetyhub -s /bin/sh -D safetyhub

# JAR 파일 복사
COPY --from=build /app/safetyhub-bootstrap/build/libs/*.jar app.jar

# 소유권 변경
RUN chown -R safetyhub:safetyhub /app

USER safetyhub

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
