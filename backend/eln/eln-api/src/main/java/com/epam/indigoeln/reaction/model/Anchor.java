package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "str")
public abstract class Anchor {

    @JsonValue
    private final String str;

    @Getter
    @JsonIgnore
    private final int number;

    protected Anchor(String prefix, int number) {
        str = prefix + number;
        this.number = number;
    }

    protected Anchor(String prefix, String str) {
        this.str = str;
        int number = -1;
        try {
            number = Integer.parseInt(str.substring(prefix.length()));
        } catch (Exception ignore) {
        }
        if (number < 0 || !str.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid anchor: " + str);
        }
        this.number = number;
    }

    @Override
    public String toString() {
        return str;
    }
}
