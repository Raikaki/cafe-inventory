# ---------- Stage 1: build React frontend ----------
FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend/ ./
# Vite outputs to ../src/main/resources/static  -> /app/src/main/resources/static
RUN npm run build

# ---------- Stage 2: build Spring Boot (bundles the SPA) ----------
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
# Overlay the built SPA into static resources
COPY --from=frontend /app/src/main/resources/static ./src/main/resources/static
RUN mvn -q clean package -DskipTests

# ---------- Stage 3: runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/target/cafe-inventory.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
