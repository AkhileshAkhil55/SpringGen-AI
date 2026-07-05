# Stage 1: Build the React frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build the Spring Boot backend
FROM maven:3.9.6-eclipse-temurin-21 AS backend-builder
WORKDIR /app
# Copy the Maven project files
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src/
# Copy static frontend assets into Spring Boot's static folder
COPY --from=frontend-builder /app/frontend/dist /app/backend/src/main/resources/static/
# Build the Spring Boot application JAR
RUN mvn -f backend/pom.xml clean package -DskipTests

# Stage 3: Run the application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=backend-builder /app/backend/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
