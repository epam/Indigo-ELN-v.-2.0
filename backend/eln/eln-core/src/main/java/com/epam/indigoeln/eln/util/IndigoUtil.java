package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputRole;
import jakarta.validation.constraints.Null;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IndigoUtil {

    public static Iterable<IndigoMolecule> reactionIterable(IndigoReaction reaction, @Nullable ReactionInputRole role) {
        if (role == null) {
            return reaction.products();
        } else if (role == ReactionInputRole.REACTANT) {
            return reaction.reactants();
        } else if (role == ReactionInputRole.CATALYST) {
            return reaction.catalysts();
        } else {
            throw new IllegalArgumentException(role.name());
        }
    }

    public static void addToReaction(IndigoReaction reaction, @Nullable ReactionInputRole role, IndigoMolecule molecule) {
        if (role == null) {
            reaction.addProduct(molecule);
        } else if (role == ReactionInputRole.REACTANT) {
            reaction.addReactant(molecule);
        } else if (role == ReactionInputRole.CATALYST) {
            reaction.addCatalyst(molecule);
        } else {
            throw new IllegalArgumentException(role.name());
        }
    }

    public static void rebuildReactionScheme(IndigoReaction reaction, ReactionInputRole role, List<IndigoMolecule> molecules) {
        reactionIterable(reaction, role).forEach(IndigoMolecule::remove);
        // Indigo adds reaction components in the beginning of the list, so adding in the reverse order
        molecules.reversed().forEach(molecule -> addToReaction(reaction, role, molecule));
    }
}
