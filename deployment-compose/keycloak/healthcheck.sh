#!/bin/bash
set -e
if [ ! -f /opt/keycloak/.keycloak/kcadm.config ]; then
  /opt/keycloak/bin/kcadm.sh config credentials --server https://keycloak.indigoeln.local:8443 --realm master --user admin --password admin --truststore /opt/keycloak/conf/server.keystore --trustpass password
fi
/opt/keycloak/bin/kcadm.sh get realms/indigo-eln --truststore /opt/keycloak/conf/server.keystore --trustpass password --fields id
