FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root build/nativelibs/linux-x86_64/* /work/nativelibs/linux-x86_64/
COPY --chown=1001:root build/*runner* /work/application

# Enable IPv6 in the container
RUN echo 'net.ipv6.conf.all.disable_ipv6 = 0' >> /etc/sysctl.conf
RUN echo 'net.ipv6.conf.default.disable_ipv6 = 0' >> /etc/sysctl.conf

EXPOSE 8080
USER 1001

ENV NATIVE_LIB_PATH=/work/nativelibs
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0", "-Djava.net.preferIPv6Addresses=true"]
