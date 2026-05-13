package com.epam.indigoeln.common.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jspecify.annotations.Nullable;

@Slf4j
public class APISecretFilter {

    @ConfigProperty(name = "eln.api.secret")
    String apiSecret;

    @Nullable
    @ServerRequestFilter
    public Response responseFilter(ContainerRequestContext requestContext) {
        if (!apiSecret.equals(requestContext.getHeaders().getFirst("X-API-Secret"))) {
            log.debug("Invalid API secret: {}", requestContext.getHeaderString("X-API-Secret"));
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid API secret")
                    .build();
        }
        return null;
    }
}
