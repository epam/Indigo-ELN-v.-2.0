package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class InputAnchor extends Anchor {

    public static InputAnchor create() {
        return new InputAnchor(generate());
    }

    @JsonCreator
    public InputAnchor(String str) {
        super(str);
    }

    @Override
    @JsonValue
    public String getValue() {
        return super.getValue();
    }
}
