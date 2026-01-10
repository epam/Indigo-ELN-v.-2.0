package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OutputSampleAnchor extends Anchor {

    public OutputSampleAnchor(int number) {
        super("B", number);
    }

    @JsonCreator
    OutputSampleAnchor(String str) {
        super("B", str);
    }
}
