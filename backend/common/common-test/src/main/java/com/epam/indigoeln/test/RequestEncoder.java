package com.epam.indigoeln.test;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;

@RequiredArgsConstructor
public class RequestEncoder implements Encoder {

    private final Encoder delegate;

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        delegate.encode(object, bodyType, template);
        if (object instanceof String string) {
            template.body(string);
        }
    }
}
