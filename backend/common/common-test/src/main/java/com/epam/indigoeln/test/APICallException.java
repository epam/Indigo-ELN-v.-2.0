package com.epam.indigoeln.test;

import com.epam.indigoeln.common.config.ErrorDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class APICallException extends RuntimeException {

    private final String method;
    private final String url;

    private final int statusCode;
    private final String reasonPhrase;

    private final List<ErrorDTO> errors;

    @Override
    public String toString() {
        return "APICallException: %s %s: %s %s:\n\t%s\n".formatted(
                method, url, statusCode, reasonPhrase, StreamEx.of(errors).joining("\n\t")
        );
    }
}
