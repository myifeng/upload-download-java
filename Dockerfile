FROM adoptopenjdk:11-jre-openj9

ENV TZ "Asia/Shanghai"

COPY ./build/libs/upload-download-java-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]