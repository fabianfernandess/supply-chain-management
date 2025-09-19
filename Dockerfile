# Dockerfile
# Use a slim Java 17 image as the base
FROM eclipse-temurin:17-jre-focal

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the target directory
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]