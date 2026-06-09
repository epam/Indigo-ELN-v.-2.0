package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.UUID;

public class InputSampleAnchor extends Anchor {

    public InputSampleAnchor(UUID value) {
        super(value);
    }

    public static InputSampleAnchor create() {
        return new InputSampleAnchor(UUID.randomUUID());
    }

    @JsonCreator
    public InputSampleAnchor(String str) {
        super(str);
    }
}
