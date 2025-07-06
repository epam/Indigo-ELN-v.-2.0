package com.epam.indigoeln.eln.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import java.util.List;

public class ContentLengthFilter {

    @Inject
    ObjectMapper objectMapper;

    // In native integration tests, there are SocketTimeoutException errors when response is without Content-Length and is greater than 8191 bytes.
    // Doesn't seem to happen in neither JVM nor in native lambda deployment.
    // To prevent this, set Content-Length header for all JSON responses.
    @SneakyThrows
    @ServerResponseFilter
    public void responseFilter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object entity = responseContext.getEntity();
        if (entity != null && responseContext.getMediaType() != null && responseContext.getMediaType().toString().startsWith(MediaType.APPLICATION_JSON)) {
            byte[] bytes = objectMapper.writeValueAsBytes(entity);
            responseContext.setEntity(bytes);
            responseContext.getHeaders().put(HttpHeaders.CONTENT_LENGTH, List.of(bytes.length));
        }
    }
}
