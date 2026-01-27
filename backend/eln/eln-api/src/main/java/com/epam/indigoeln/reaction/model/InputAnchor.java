package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.UUID;

public class InputAnchor extends Anchor {

    public InputAnchor(UUID value) {
        super(value);
    }

    @JsonCreator
    public InputAnchor(String str) {
        super(str);
    }
}
