package com.epam.indigoeln.eln.util;

import feign.Response;
import lombok.Value;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

@Value
public class ResponseWithHeaders {

    InputStream content;
    Map<String, Collection<String>> headers;
}
