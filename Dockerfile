# Use OpenJDK 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy jar (assume Maven build)
COPY target/wallet-api-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run jar
ENTRYPOINT ["java","-jar","app.jar"]