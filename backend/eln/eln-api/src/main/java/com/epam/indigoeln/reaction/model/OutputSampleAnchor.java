package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.UUID;

public class OutputSampleAnchor extends Anchor {

    public OutputSampleAnchor(UUID value) {
        super(value);
    }

    @JsonCreator
    public OutputSampleAnchor(String str) {
        super(str);
    }
}
