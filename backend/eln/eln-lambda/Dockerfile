#FROM public.ecr.aws/docker/library/gradle:8.14-jdk24 AS builder
#FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-24 AS builder
#USER root
#RUN microdnf install -y yum-utils
#RUN yum-config-manager --add-repo https://download.docker.com/linux/rhel/docker-ce.repo
#RUN microdnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
#RUN systemctl start docker
#RUN docker ps
#USER 1001
#COPY --chown=1001:root . /sources
#WORKDIR /sources
#RUN --mount=type=cache,id=cache_maven,uid=1001,gid=1001,target=/home/quarkus/.m2 \
#    --mount=type=cache,id=cache_gradle,uid=1001,gid=1001,target=/home/quarkus/.gradle \
#    ./gradlew build
    # ":eln:eln-lambda:quarkusIntTest" --info --stacktrace

FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
#COPY --chown=1001:root --from=builder build/native-libs/* /work/nativelibs
#COPY --chown=1001:root --from=builder build/*-runner /work/application
COPY --chown=1001:root build/nativelibs/linux-x86_64/* /work/nativelibs/linux-x86_64/
#COPY --chown=1001:root build/*.so /work/
COPY --chown=1001:root build/*runner* /work/application

# Enable IPv6 in the container
RUN echo 'net.ipv6.conf.all.disable_ipv6 = 0' >> /etc/sysctl.conf
RUN echo 'net.ipv6.conf.default.disable_ipv6 = 0' >> /etc/sysctl.conf

EXPOSE 8080
USER 1001

ENV NATIVE_LIB_PATH=/work/nativelibs
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0", "-Djava.net.preferIPv6Addresses=true"]
