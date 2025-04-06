FROM maven:3.8-openjdk-17 AS builder

WORKDIR /app
COPY . .

# Ejecutar el build de Maven
RUN mvn clean package -Pproduction -DskipTests

FROM openjdk:17-slim

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
