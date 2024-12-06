FROM public.ecr.aws/docker/library/maven:3-eclipse-temurin-17 as builder

COPY . /sources/
WORKDIR /sources

#RUN --mount=type=cache,id=cache_maven,target=/root/.m2 mvn package --batch-mode
RUN mvn package --batch-mode

FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre-noble

WORKDIR /opt/jars
COPY --from=builder /sources/target/crs.jar crs.jar

ENTRYPOINT sh -c "java $JAVA_OPTS -Djna.debug_load=true --add-opens=java.base/java.lang=ALL-UNNAMED  -jar crs.jar"
