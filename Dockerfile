# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---------- Stage 2: runtime enxuto ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S springuser -u 1001 -G spring
COPY --from=builder /build/target/*.jar /app/app.jar
RUN chown -R springuser:spring /app
EXPOSE 8080
USER springuser
# Usa ate 75% da memoria do container (sem isso a JVM pode estourar o limite e ser morta).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
