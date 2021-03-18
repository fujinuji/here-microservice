FROM openjdk:12-alpine
COPY target/Here-Microservice-*.jar /here-microservice.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "here-microservice.jar"]