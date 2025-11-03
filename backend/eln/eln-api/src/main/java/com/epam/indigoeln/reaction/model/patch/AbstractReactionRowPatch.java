package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@Getter
@Setter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractReactionRowPatch<A extends Anchor> extends AbstractListElementPatch<A> {

    @Nullable
    private Optional<CompoundRef> compound;

    @Nullable
    private Optional<EnteredValuePatch<NoUnit>> eq;
}
