package com.epam.indigoeln.common.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.Optional;

@ApplicationScoped
public class APISecretHeaderFactory implements ClientHeadersFactory {

    @ConfigProperty(name = "eln.api.secret")
    Optional<String> secret;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        if (incomingHeaders.containsKey(UserHolder.X_TEST_AUTHORIZATION)) {
            result.add(UserHolder.X_TEST_AUTHORIZATION, incomingHeaders.getFirst(UserHolder.X_TEST_AUTHORIZATION));
        }
        if (incomingHeaders.containsKey(HttpHeaders.AUTHORIZATION)) {
            result.add(HttpHeaders.AUTHORIZATION, incomingHeaders.getFirst(HttpHeaders.AUTHORIZATION));
        }
        secret.ifPresent(s -> result.add("X-API-Secret", s));
        return result;
    }
}
