package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class InputSampleAnchor extends Anchor {

    public static InputSampleAnchor create() {
        return new InputSampleAnchor(generate());
    }

    @JsonCreator
    public InputSampleAnchor(String str) {
        super(str);
    }

    @Override
    @JsonValue
    public String getValue() {
        return super.getValue();
    }
}
