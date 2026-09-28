package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

public class OutputAnchor extends Anchor {

    public OutputAnchor(UUID value) {
        super(value);
    }

    public static OutputAnchor create() {
        return new OutputAnchor(UUID.randomUUID());
    }

    @JsonCreator
    public OutputAnchor(String str) {
        super(str);
    }

    @Override
    @JsonValue
    public UUID getValue() {
        return super.getValue();
    }
}
