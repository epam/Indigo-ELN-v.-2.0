FROM maven:3-alpine as builder

COPY . /sources/
WORKDIR /sources

RUN mvn package --batch-mode

FROM tomcat:7.0.109-jdk8-adoptopenjdk-hotspot
COPY --from=builder /sources/target/crs.war /usr/local/tomcat/webapps/crs.war
