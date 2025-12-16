package com.epam.indigoeln.reaction.model.units;

public enum EnteredValueSource {

    // high-priority must be on top
    FIXED,
    USER_LAST_ENTERED,
    USER_ENTERED,
    CALCULATED_FROM_LAST_ENTERED,
    CALCULATED,
    DEFAULT;
}
