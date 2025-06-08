package com.epam.indigoeln.eln.util;

import feign.Response;
import lombok.Value;

import java.util.Collection;
import java.util.Map;

@Value
public class ResponseWithHeaders {

    Response.Body value;
    Map<String, Collection<String>> headers;
}
