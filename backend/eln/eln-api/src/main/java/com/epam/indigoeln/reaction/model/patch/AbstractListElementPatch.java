package com.epam.indigoeln.reaction.model.patch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@Getter
@Setter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractListElementPatch<A> {

    // null: not repositioned
    // empty: new item
    // index: repositioned from item with given index
    @Nullable
    @JsonProperty("$from")
    private Optional<Integer> xfrom;

    @Nullable
    private Optional<A> anchor;
}
