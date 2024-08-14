FROM maven:3-alpine as builder

COPY . /sources/
WORKDIR /sources

RUN mvn package --batch-mode

FROM openjdk:8-jre
WORKDIR /opt/jars
COPY --from=builder /sources/target/signatureservice.war signatureservice.war

ENTRYPOINT sh -c "java $JAVA_OPTS -jar signatureservice.war"
