# syntax=docker/dockerfile:1

# ── Build stage: JDK 21 로 실행 가능한 bootJar 생성 ──
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 레이어 캐시: 빌드 스크립트/래퍼 먼저 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# 소스 복사 후 jar 빌드 (테스트는 CI(test 잡)에서 이미 수행하므로 여기선 스킵)
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ── Run stage: JRE 21 로 실행 (이미지 경량화) ──
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

# 비루트 사용자로 실행 (보안)
RUN useradd -r -u 1001 spring && chown spring:spring /app/app.jar
USER spring

EXPOSE 8080

# 프리티어(1GB) EC2 대비 힙 상한. 컨테이너 메모리의 75%까지만 사용.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
