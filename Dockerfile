# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx400m"

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/lcms-app-1.0.0-SNAPSHOT.jar app.jar

ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xmx384m -Dserver.port=${PORT:-10000} -jar app.jar"]
