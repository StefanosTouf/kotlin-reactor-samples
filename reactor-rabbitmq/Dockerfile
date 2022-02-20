FROM gradle:7.3.3-jdk8 AS builder

ARG JAR_NAME=doggo-app

COPY . /usr/src

WORKDIR /usr/src

RUN gradle --no-daemon clean build

FROM openjdk:8-jdk

COPY --from=builder /usr/src/build/libs/*-doggo-app.jar /app/app.jar

WORKDIR /app

ENTRYPOINT java -jar /app/app.jar
