package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class InputSampleAnchor extends Anchor {

    public InputSampleAnchor(int number) {
        super("S", number);
    }

    @JsonCreator
    InputSampleAnchor(String str) {
        super("S", str);
    }
}
