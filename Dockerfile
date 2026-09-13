FROM amazoncorretto:17-alpine

RUN apk update && \
    apk upgrade --no-cache

WORKDIR /app

COPY target/devops-demo-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
