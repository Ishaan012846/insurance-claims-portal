# Stage 1: Build stage with Maven and OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application JAR
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Runtime stage with minimal Eclipse Temurin JRE 17 Alpine
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create upload directory and non-root system user for security
RUN mkdir -p /app/uploads && \
    addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

USER appuser

# Copy compiled JAR artifact from build stage
COPY --from=build /app/target/insurance-claims-portal-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
