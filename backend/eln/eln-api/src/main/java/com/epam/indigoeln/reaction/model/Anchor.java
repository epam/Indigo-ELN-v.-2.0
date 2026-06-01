package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
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

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof Anchor anchor)) return false;
        return value.equals(anchor.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
