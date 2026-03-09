# ====== Etapa 1: Build ======
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos todo el proyecto (root + módulos)
COPY . .

# Compilamos solo api-gateway y sus dependencias
RUN mvn clean package -DskipTests -pl api-gateway -am

# ====== Etapa 2: Runtime ======
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl bash

WORKDIR /app

COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]