FROM maven:3-eclipse-temurin-11-alpine as builder

COPY . /sources/
#COPY settings.xml /root/.m2/
WORKDIR /sources

RUN mvn clean package -P release

FROM eclipse-temurin:11-alpine

WORKDIR /opt/jars
COPY --from=builder /sources/target/indigoeln.war indigoeln.war

ENTRYPOINT sh -c "java $JAVA_OPTS -jar indigoeln.war"
