FROM public.ecr.aws/docker/library/gradle:8.14-jdk24 AS builder
COPY . /sources
WORKDIR /sources
RUN gradle build ":eln:eln-lambda:quarkusIntTest"

FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root --from=builder eln/eln-lambda/build/native-libs/* /work/nativelibs
COPY --chown=1001:root --from=builder eln/eln-lambda/build/*-runner /work/application

EXPOSE 8080
USER 1001

ENV NATIVE_LIB_PATH=/work/nativelibs
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
