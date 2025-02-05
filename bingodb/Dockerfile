FROM public.ecr.aws/docker/library/maven:3-eclipse-temurin-17 as builder

COPY . /sources/
WORKDIR /sources

RUN --mount=type=cache,id=cache_maven,target=/root/.m2 mvn clean package -P release --batch-mode

FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre-noble

WORKDIR /opt/jars
COPY --from=builder /sources/target/bingodb.war bingodb.war

EXPOSE 9999
ENTRYPOINT sh -c "java $JAVA_OPTS -jar bingodb.war"
