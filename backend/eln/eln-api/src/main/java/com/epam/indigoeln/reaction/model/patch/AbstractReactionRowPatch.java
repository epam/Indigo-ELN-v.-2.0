package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
public abstract class AbstractReactionRowPatch<A extends Anchor> {

    @Nullable
    private Patched<CompoundRef, CompoundRefPatch> compound;

    @Nullable
    private Patched<EnteredValue<NoUnit>, EnteredValuePatch<NoUnit>> eq;
}
