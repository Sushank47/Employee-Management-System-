# Multi-stage build for Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy server directory and build jar
COPY server ./server
WORKDIR /app/server
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/server/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
