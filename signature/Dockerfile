FROM maven:3-jdk-8

COPY . /sources/
WORKDIR /sources

RUN mvn war:war --batch-mode

ENTRYPOINT mvn tomcat7:run-war
