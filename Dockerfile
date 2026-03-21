# Stage 1: Build the JAR with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight production image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Security: non-root user
RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=builder /app/target/*.jar app.jar

USER spring
EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]