package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.UUID;

public class ReactionAnchor extends Anchor {

    public ReactionAnchor(UUID value) {
        super(value);
    }

    @JsonCreator
    public ReactionAnchor(String str) {
        super(str);
    }
}
