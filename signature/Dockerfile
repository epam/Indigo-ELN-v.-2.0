FROM public.ecr.aws/docker/library/maven:3-eclipse-temurin-17 as builder

COPY . /sources/
WORKDIR /sources

#RUN --mount=type=cache,id=cache_maven,target=/root/.m2 mvn package --batch-mode
RUN mvn package --batch-mode

FROM public.ecr.aws/docker/library/tomcat:10-jre17
COPY --from=builder /sources/target/signatureservice.war /usr/local/tomcat/webapps/signatureservice.war
