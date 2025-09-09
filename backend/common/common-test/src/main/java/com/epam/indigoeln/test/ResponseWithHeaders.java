package com.epam.indigoeln.test;

import lombok.Value;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

@Value
public class ResponseWithHeaders {

    InputStream content;
    Map<String, Collection<String>> headers;
}
