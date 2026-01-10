package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class InputAnchor extends Anchor {

    public InputAnchor(int number) {
        super("I", number);
    }

    @JsonCreator
    InputAnchor(String str) {
        super("I", str);
    }
}
