package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.ReactionRole;
import org.jspecify.annotations.Nullable;

public class IndigoUtil {

    public static Iterable<IndigoMolecule> reactionIterable(IndigoReaction reaction, @Nullable ReactionRole role) {
        if (role == null) {
            return reaction.products();
        } else if (role == ReactionRole.REACTANT) {
            return reaction.reactants();
        } else if (role == ReactionRole.CATALYST) {
            return reaction.catalysts();
        } else {
            throw new IllegalArgumentException(role.name());
        }
    }

    public static void addToReaction(IndigoReaction reaction, @Nullable ReactionRole role, IndigoMolecule molecule) {
        if (role == null) {
            reaction.addProduct(molecule);
        } else if (role == ReactionRole.REACTANT) {
            reaction.addReactant(molecule);
        } else if (role == ReactionRole.CATALYST) {
            reaction.addCatalyst(molecule);
        } else {
            throw new IllegalArgumentException(role.name());
        }
    }
}
