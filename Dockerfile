# -----------------------------
# Stage 1: Build
# -----------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew

# Build the executable Spring Boot JAR skipping tests during image build
RUN ./gradlew bootJar --no-daemon -x test

# -----------------------------
# Stage 2: Runtime
# -----------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]


