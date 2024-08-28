FROM maven:3-alpine as builder

COPY . /sources/
WORKDIR /sources

RUN mvn clean package -P release --batch-mode

FROM openjdk:8-jre

WORKDIR /opt/jars
COPY --from=builder /sources/target/bingodb.war bingodb.war

EXPOSE 9999
ENTRYPOINT java -jar bingodb.war
