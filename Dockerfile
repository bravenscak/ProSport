FROM eclipse-temurin:21-jdk-alpine
MAINTAINER hr.java
COPY target/ProSport-0.0.1-SNAPSHOT.jar demo.jar
COPY data /data
COPY uploads /uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/demo.jar"]