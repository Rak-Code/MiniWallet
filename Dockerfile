# ---- Build stage ----
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Copy source code and build
COPY src ./src
RUN mvn -B -DskipTests clean package


# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /opt/app

# Copy built jar from build stage and rename
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Run the app
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
