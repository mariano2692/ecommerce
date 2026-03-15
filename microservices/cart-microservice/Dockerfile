# FROM eclipse-temurin:21-jdk-alpine AS build

# WORKDIR /app

# # Instalar Maven en Alpine
# RUN apk add --no-cache maven

# COPY . .

# RUN mvn clean package -DskipTests -pl microservices/cart-microservice -am

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl bash

WORKDIR /app

COPY target/*.jar app.jar

# Exponer el puerto del Cart Microservice
EXPOSE 8093

ENTRYPOINT ["java", "-jar", "app.jar"]