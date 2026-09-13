# =========================================================================
# Multi-stage Dockerfile for InterCollege Spring Boot (Render Deployment)
# Stage 1: Build the JAR using Maven and Java 17
# Stage 2: Lightweight Java 17 JRE runtime container
# =========================================================================

# ----------------- Stage 1: Build Stage -----------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src ./src

# Package the application into an executable JAR (skipping test execution)
RUN mvn clean package -DskipTests

# ----------------- Stage 2: Runtime Stage -----------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Render dynamically sets the PORT environment variable at runtime
# We provide 8080 as the local fallback
ENV PORT=8080

# Ensure data directory exists for JSON file storage
RUN mkdir -p /app/data

# Copy the packaged Spring Boot JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Document standard port
EXPOSE 8080

# Execute Spring Boot with dynamic port binding
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
