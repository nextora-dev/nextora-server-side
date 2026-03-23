# ── Stage 1: Build ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache dependencies – this layer is rebuilt only when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B \
    && mv target/*.jar target/app.jar

# Extract Spring Boot layered JAR for optimal Docker layer caching
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

# ── Stage 2: Production ────────────────────────────────────────
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Security: non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy layers in order of change frequency (least → most)
COPY --from=builder /app/target/extracted/dependencies/ ./
COPY --from=builder /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/target/extracted/application/ ./

# Drop privileges
USER spring

EXPOSE 8080

# JVM tuning for containers
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Exec form — Java receives signals directly (graceful shutdown works)
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "org.springframework.boot.loader.launch.JarLauncher"]

# Actuator health check (requires management endpoints enabled)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1