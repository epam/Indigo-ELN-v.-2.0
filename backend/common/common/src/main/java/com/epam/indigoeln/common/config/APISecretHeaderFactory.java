package com.epam.indigoeln.common.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

public abstract class APISecretHeaderFactory implements ClientHeadersFactory {

    protected abstract String getSecret();

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("X-API-Secret", getSecret());
        if (incomingHeaders.containsKey(UserHolder.X_TEST_AUTHORIZATION)) {
            result.add(UserHolder.X_TEST_AUTHORIZATION, incomingHeaders.getFirst(UserHolder.X_TEST_AUTHORIZATION));
        }
        if (incomingHeaders.containsKey(HttpHeaders.AUTHORIZATION)) {
            result.add(HttpHeaders.AUTHORIZATION, incomingHeaders.getFirst(HttpHeaders.AUTHORIZATION));
        }
        return result;
    }

    @ApplicationScoped
    public static class Public extends APISecretHeaderFactory {

        @Getter
//        @ConfigProperty(name = "eln.api.secret")
        private String secret = "integrationTestsAPISecret";
    }

    @ApplicationScoped
    public static class Internal extends APISecretHeaderFactory {

        @Getter
        @ConfigProperty(name = "eln.internal.api.secret")
        String secret;
    }
}
