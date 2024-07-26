FROM maven:3

COPY . /sources/
WORKDIR /sources

RUN mvn war:war

ENTRYPOINT mvn tomcat7:run-war
