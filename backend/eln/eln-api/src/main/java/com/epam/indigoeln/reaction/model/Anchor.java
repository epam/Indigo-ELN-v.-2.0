package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(of = "value")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Anchor {

    @Getter
    @JsonValue
    private final UUID value;

    protected Anchor(String str) {
        this.value = UUID.fromString(str);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
