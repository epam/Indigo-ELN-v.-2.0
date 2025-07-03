package com.epam.indigoeln.eln.util;

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;

@RequiredArgsConstructor
public class ResponseWithHeadersDecoder implements Decoder {

    private final Decoder delegate;

    @Override
    public @Nullable Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response.status() == 404 || response.status() == 204) return Util.emptyValueOf(type);
        if (response.body() == null) return null;
        if (type.getTypeName().equals(ResponseWithHeaders.class.getName())) {
            return new ResponseWithHeaders(response.body().asInputStream(), response.headers());
        }
        return delegate.decode(response, type);
    }
}
