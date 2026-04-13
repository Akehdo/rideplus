FROM gradle:8.14-jdk21-alpine AS build

WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./
COPY identity-service identity-service
COPY gateway-service gateway-service
COPY passenger-service passenger-service
COPY observability-starter observability-starter

RUN chmod +x gradlew && ./gradlew :gateway-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/gateway-service/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
