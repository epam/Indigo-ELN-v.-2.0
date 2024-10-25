FROM maven:3-eclipse-temurin-11-alpine as builder
#FROM maven:3-alpine as builder

COPY . /sources/
WORKDIR /sources

#RUN --mount=type=cache,id=cache_maven,target=/root/.m2 mvn clean package -P release --batch-mode
RUN mvn clean package -P release --batch-mode

FROM eclipse-temurin:11-noble

#FROM openjdk:8-jre
#RUN apk add libgcc gcompat
#RUN apk add valgrind

WORKDIR /opt/jars
COPY --from=builder /sources/target/indigoeln.war indigoeln.war

ENTRYPOINT sh -c "java $JAVA_OPTS -Djna.debug_load=true -jar indigoeln.war"
