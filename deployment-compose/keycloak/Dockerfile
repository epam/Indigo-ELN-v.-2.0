FROM keycloak/keycloak:26.1.2 AS builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres

WORKDIR /opt/keycloak
# for demonstration purposes only, please make sure to use proper certificates in production instead
#RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=keycloak.indigoeln.local" -alias keycloak -ext "SAN:c=DNS:keycloak.indigoeln.local" -keystore conf/server.keystore
COPY local.keystore /opt/keycloak/conf/server.keystore
RUN /opt/keycloak/bin/kc.sh build

FROM keycloak/keycloak:26.1.2
COPY --from=builder /opt/keycloak/ /opt/keycloak/

COPY --chmod=777 healthcheck.sh /

COPY indigo-eln-realm.json /opt/keycloak/data/import/
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start", "--optimized", "--import-realm"]
