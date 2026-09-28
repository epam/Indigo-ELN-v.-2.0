package com.epam.indigoeln.signature.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Provider
public class LoggingFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logHeaders(requestContext);
        logBody(requestContext);
    }

    private void logHeaders(ContainerRequestContext requestContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request: ").append(requestContext.getMethod())
                .append(" ").append(requestContext.getUriInfo().getRequestUri())
                .append("\nHeaders:");
        for (Map.Entry<String, List<String>> entry : requestContext.getHeaders().entrySet()) {
            sb.append("\n  ").append(entry.getKey()).append(": ").append(String.join(", ", entry.getValue()));
        }
        log.debug(">>> {}", sb);
    }

    private void logBody(ContainerRequestContext requestContext) throws IOException {
        InputStream entityStream = requestContext.getEntityStream();
        if (entityStream == null) {
            return;
        }

        byte[] body = entityStream.readAllBytes();
        if (body.length > 0) {
            log.debug(">>> Body: {}", new String(body, StandardCharsets.UTF_8));
        }

        // Restore the consumed stream so downstream handlers can still read it
        requestContext.setEntityStream(new ByteArrayInputStream(body));
    }
}

