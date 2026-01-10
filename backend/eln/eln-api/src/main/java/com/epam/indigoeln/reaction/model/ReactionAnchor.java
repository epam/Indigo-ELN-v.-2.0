package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ReactionAnchor extends Anchor {

    public ReactionAnchor(int number) {
        super("R", number);
    }

    @JsonCreator
    ReactionAnchor(String str) {
        super("R", str);
    }
}
