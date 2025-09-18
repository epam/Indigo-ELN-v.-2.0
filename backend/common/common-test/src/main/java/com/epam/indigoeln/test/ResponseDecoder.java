package com.epam.indigoeln.test;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

@RequiredArgsConstructor
public class ResponseDecoder implements Decoder {

    private final Decoder delegate;

    @Override
    public @Nullable Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
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
        return delegate.decode(response, type);
    }
}
