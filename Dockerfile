FROM openjdk:17-alpine

ARG JAR_FILE=/build/libs/gamegoo-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} /gamegoo.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod", "/gamegoo.jar"]