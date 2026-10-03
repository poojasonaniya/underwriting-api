# Multi-stage build for ratio-code-exercise
ARG GRADLE_BASE_IMAGE=gradle:8.2.1-jdk17-alpine
ARG JAVA_BASE_IMAGE=openjdk:17-jre-slim

# Build stage
FROM ${GRADLE_BASE_IMAGE} as builder

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle.kts settings.gradle gradlew ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build -x test --no-daemon

# Production stage
FROM ${JAVA_BASE_IMAGE} as production

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Development stage (for docker-compose)
FROM ${GRADLE_BASE_IMAGE} as development

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle gradlew ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Expose ports (8080 for app, 8081 for debug)
EXPOSE 8080 8081

# Health check for development
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run with debug enabled for development
ENTRYPOINT ["./gradlew", "bootRun", "--args='--spring.profiles.active=compose'", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081'"]
