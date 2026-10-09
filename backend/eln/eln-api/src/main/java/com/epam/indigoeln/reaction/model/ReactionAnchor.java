package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class ReactionAnchor extends Anchor {

    public static ReactionAnchor create() {
        return new ReactionAnchor(generate());
    }

    @JsonCreator
    public ReactionAnchor(String str) {
        super(str);
    }

    @Override
    @JsonValue
    public String getValue() {
        return super.getValue();
    }
}
