package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class OutputSampleAnchor extends Anchor {

    public static OutputSampleAnchor create() {
        return new OutputSampleAnchor(generate());
    }

    @JsonCreator
    public OutputSampleAnchor(String str) {
        super(str);
    }

    @Override
    @JsonValue
    public String getValue() {
        return super.getValue();
    }
}
