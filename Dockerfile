FROM openjdk:8-jre-buster

LABEL maintainer="myifeng <myifengs@gmail.com>"

ENV UPLOAD-FOLDER /appendix

COPY build/libs/upload-download-java-1.0.0-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]