FROM eclipse-temurin:25-jdk-jammy

WORKDIR /app

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Copy pom.xml
COPY pom.xml .

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime image
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

# Copy the built jar from builder
COPY --from=0 /app/target/user-service-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/auth/validate || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

