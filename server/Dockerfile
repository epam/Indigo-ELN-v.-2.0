#FROM maven:3-eclipse-temurin-11-alpine as builder
FROM maven:3-alpine as builder

COPY . /sources/
WORKDIR /sources

RUN mvn clean package -P release --batch-mode

#FROM eclipse-temurin:11-alpine
FROM openjdk:8-jre
#RUN apk add libgcc gcompat

WORKDIR /opt/jars
COPY --from=builder /sources/target/indigoeln.war indigoeln.war

ENTRYPOINT sh -c "java $JAVA_OPTS -jar indigoeln.war"
