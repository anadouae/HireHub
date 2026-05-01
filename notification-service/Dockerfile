# Stage 1: Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy parent pom and common module for dependency resolution
COPY pom.xml .
COPY hirehub-common/ hirehub-common/
COPY notification-service/pom.xml notification-service/

# Build the project
RUN apk add --no-cache maven && \
    mvn -f hirehub-common/pom.xml clean install -DskipTests && \
    mvn -f notification-service/pom.xml clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR from builder stage
COPY --from=builder /app/notification-service/target/notification-service-*.jar app.jar

# Create a non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -D -u 1001 -G appgroup appuser && \
    chown -R appuser:appgroup /app

USER appuser

# Expose port (Spring Boot default)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=docker"]

