# -----------------------------
# Stage 1: Build
# -----------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper files first to leverage Docker layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .

RUN chmod +x gradlew

# Download Gradle distribution
RUN ./gradlew --version --no-daemon

# Copy source code and build executable Spring Boot JAR skipping tests
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# -----------------------------
# Stage 2: Runtime
# -----------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
