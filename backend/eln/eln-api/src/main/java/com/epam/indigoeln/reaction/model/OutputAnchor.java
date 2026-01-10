package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OutputAnchor extends Anchor {

    public OutputAnchor(int number) {
        super("O", number);
    }

    @JsonCreator
    OutputAnchor(String str) {
        super("O", str);
    }
}
