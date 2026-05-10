FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl bash

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8096

ENTRYPOINT ["java", "-jar", "app.jar"]
