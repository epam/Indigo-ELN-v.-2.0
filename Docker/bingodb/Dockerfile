FROM maven:3-alpine as builder

RUN apk update && apk add git
RUN git clone https://github.com/epam/Indigo-ELN-v.-2.0.git
WORKDIR Indigo-ELN-v.-2.0
RUN git checkout indigo-eln-bingodb
RUN mvn clean package -P release

FROM openjdk:8-jre

WORKDIR /opt/jars
COPY --from=builder Indigo-ELN-v.-2.0/target/bingodb.war bingodb.war

EXPOSE 9999
ENTRYPOINT java -jar bingodb.war

