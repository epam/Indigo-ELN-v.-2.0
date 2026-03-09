package com.epam.indigoeln.test;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;

@RequiredArgsConstructor
public class ResponseDecoder implements Decoder {

    private final Decoder stringDecoder;
    private final Decoder jsonDecoder;

    @Override
    @Nullable
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (type.getTypeName().equals(jakarta.ws.rs.core.Response.class.getName())) {
            var builder = jakarta.ws.rs.core.Response.status(response.status(), response.reason());
            if (response.body() != null) {
                try (InputStream is = response.body().asInputStream()) {
                    builder.entity(response.body().asInputStream().readAllBytes());
                }
            }
            response.headers().forEach((key, values) -> values.forEach(value -> builder.header(key, value)));
            return builder.build();
        }
        if (response.status() == 404 || response.status() == 204 || response.body() == null) {
            return null;
        }
        String contentType = getHeader(response, HttpHeaders.CONTENT_TYPE);
        if (contentType != null && contentType.startsWith("text/")) {
            return stringDecoder.decode(response, type);
        }
        return jsonDecoder.decode(response, type);
    }

    @Nullable
    private String getHeader(Response response, String headerName) {
        Collection<String> values = response.headers().get(headerName);
        return values != null ? values.stream().findFirst().orElse(null) : null;
    }

}
