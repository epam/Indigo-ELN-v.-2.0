FROM maven:3-alpine as builder

RUN apk update && apk add git
RUN git clone https://github.com/epam/Indigo-ELN-v.-2.0.git
WORKDIR Indigo-ELN-v.-2.0
RUN git checkout indigo-eln-server
RUN mvn clean package -P release

FROM openjdk:8-alpine

WORKDIR /opt/jars
COPY --from=builder Indigo-ELN-v.-2.0/target/indigoeln.war indigoeln.war

ENTRYPOINT java -jar indigoeln.war

