# Use an image with JDK 8 and Apache Maven installed
FROM maven:3.8.4-openjdk-11-slim AS build

# Set the working directory in the container
WORKDIR /app
# Copy the source code into the container
COPY . .

# Copy the Maven project file
COPY pom.xml .

# Build the application using Maven
RUN mvn clean install -DskipTests

# Create a new stage for the final image
FROM openjdk:11.0-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the compiled application from the build stage to the final image
COPY --from=build /app/target/*.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# Define the command to run your application
CMD ["java", "-jar", "app.jar"]
