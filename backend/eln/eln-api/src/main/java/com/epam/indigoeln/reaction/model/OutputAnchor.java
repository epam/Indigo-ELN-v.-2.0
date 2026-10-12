package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class OutputAnchor extends Anchor {

    public static OutputAnchor create() {
        return new OutputAnchor(generate());
    }

    @JsonCreator
    public OutputAnchor(String str) {
        super(str);
    }

    @Override
    @JsonValue
    public String getValue() {
        return super.getValue();
    }
}
