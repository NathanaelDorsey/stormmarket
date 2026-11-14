# Multi-stage Dockerfile for Storm Market Spring Boot Application
# Optimized for production with minimal attack surface

# =============================================================================
# Stage 1: Build the application using Maven
# =============================================================================
FROM docker.io/maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and JWT keys (so they get packaged by Spring Boot plugin)
COPY src ./src
COPY src/test/resources/jwt src/main/resources/jwt

# Build with Spring Boot plugin (packages JWT keys in BOOT-INF/classes/jwt/)
RUN mvn clean package -DskipTests -B

# =============================================================================
# Stage 2: Create minimal runtime image
# =============================================================================
FROM docker.io/eclipse-temurin:25-jre-alpine AS runtime

# Install security updates and required tools
RUN apk add --no-cache \
    curl \
    dumb-init \
    && rm -rf /var/cache/apk/*

# Create non-root user for security
RUN addgroup -g 1000 -S stormmarket \
    && adduser -u 1000 -S stormmarket -G stormmarket

WORKDIR /app

# Copy the built JAR (JWT keys already in BOOT-INF/classes/jwt/)
COPY --from=builder /app/target/stormmarket-*.jar app.jar

# Create directories for file uploads and set permissions
RUN mkdir -p /app/uploads/products \
    && chown -R stormmarket:stormmarket /app

# Switch to non-root user
USER stormmarket

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose application port
EXPOSE 8080

# Use dumb-init for proper signal handling
ENTRYPOINT ["dumb-init", "--"]

# Run the application
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "app.jar"]
