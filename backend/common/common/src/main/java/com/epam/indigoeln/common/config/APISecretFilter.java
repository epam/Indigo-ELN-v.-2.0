package com.epam.indigoeln.common.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class APISecretFilter {

    @ConfigProperty(name = "eln.api.secret")
    Optional<String> apiSecret;

    @Nullable
    @ServerRequestFilter
    public Response responseFilter(ContainerRequestContext requestContext) {
        if (apiSecret.isPresent()) {
            if (!apiSecret.get().equals(requestContext.getHeaderString("X-API-Secret"))) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Invalid API secret")
                        .build();
            }
        }
        return null;
    }
}
