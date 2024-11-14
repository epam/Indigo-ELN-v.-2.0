FROM maven:3-eclipse-temurin-17 as builder

COPY . /sources/
WORKDIR /sources

#RUN --mount=type=cache,id=cache_maven,target=/root/.m2 mvn package --batch-mode
RUN mvn package --batch-mode

FROM tomcat:9-jdk17
COPY --from=builder /sources/target/crs.war /usr/local/tomcat/webapps/crs.war
