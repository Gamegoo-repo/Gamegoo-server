FROM openjdk:17-alpine

ARG JAR_FILE=/build/libs/gamegoo-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} /gamegoo.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=prod", "-jar", "/gamegoo.jar"]
