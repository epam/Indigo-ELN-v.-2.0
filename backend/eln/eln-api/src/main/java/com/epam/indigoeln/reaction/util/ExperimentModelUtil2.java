package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.reaction.model.*;
import org.jspecify.annotations.Nullable;

public class ExperimentModelUtil2 {

    @Nullable
    public static ReactionRole getRoleInSchema(ReactionRow row) {
        return switch (row) {
            case ReactionInput i -> i.getRole() != ReactionRole.SOLVENT && i.getCompound() instanceof CompoundRef.StoredOrVirtual ? i.getRole() : null;
            case ReactionOutput o -> o.isIntended() ? ReactionRole.OUTPUT : null;
        };
    }
}
