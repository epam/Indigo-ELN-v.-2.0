package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.config.ErrorDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class APICallException extends RuntimeException {

    private final int statusCode;

    private final String reasonPhrase;

    private final List<ErrorDTO> errors;

    @Override
    public String toString() {
        return "APICallException: " + statusCode + " " + reasonPhrase + ":\n\t"
                + StreamEx.of(errors).joining("\n\t")
                + "\n";
    }
}
