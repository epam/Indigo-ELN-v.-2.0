package com.epam.indigoeln.common.exception;

import java.util.List;
import java.util.UUID;

public class DictionaryNotFoundException extends RuntimeException {

    public DictionaryNotFoundException(Enum<?> dictionary, UUID id, String name, List<?> available) {
        super(String.format("%s %s (%s) not found, available values: %s", dictionary, name, id, available));
    }

    public DictionaryNotFoundException(Enum<?> dictionary, UUID id) {
        super(String.format("%s %s not found", dictionary, id));
    }
}
