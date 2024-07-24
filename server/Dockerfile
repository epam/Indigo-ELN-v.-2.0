FROM maven:3-alpine as builder

COPY . /sources/
WORKDIR /sources

RUN mvn clean package -P release

FROM eclipse-temurin:11-alpine

WORKDIR /opt/jars
COPY --from=builder /sources/target/indigoeln.war indigoeln.war

ENTRYPOINT java -jar indigoeln.war
