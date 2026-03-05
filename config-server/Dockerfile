# ====== Etapa 1: Build ======
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos todo el proyecto (root + módulos)
COPY . .

# Compilamos solo config-server y sus dependencias
RUN mvn clean package -DskipTests -pl config-server -am

# ====== Etapa 2: Runtime ======
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl bash

WORKDIR /app

COPY --from=build /app/config-server/target/*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]