package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.indigowrapper.IndigoWrapper;
import com.epam.indigoeln.reaction.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.eln.util.IndigoUtil.addToReaction;
import static com.epam.indigoeln.eln.util.IndigoUtil.reactionIterable;

@ApplicationScoped
public class ExperimentModelHelperService {

    private static final @Nullable ReactionInputRole COMPONENT_ORDER[] = {null, ReactionInputRole.CATALYST, ReactionInputRole.REACTANT};

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoWrapper indigoWrapper;

    public void rebuildReactionScheme(ExperimentEntity experiment, Reaction reaction, Set<ReactionInputRole> affectedRoles) {
        if (affectedRoles.isEmpty()) {
            return;
        }
        indigoWrapper.withSession(indigo -> {
            IndigoReaction reactionScheme = indigo.loadReaction(reaction.getRxnfile());

            for (ReactionInputRole role : COMPONENT_ORDER) {
                if (!affectedRoles.contains(role)) {
                    continue;
                }

                List<IndigoMolecule> molecules = new ArrayList<>();
                // noinspection rawtypes,unchecked
                Iterable<ReactionRow> rows = role != null ? (Iterable) reaction.getInputsOfType(role) : (Iterable) reaction.getOutputs();
                for (ReactionRow input : rows) {
                    String molfile = switch (input.getCompound()) {
                        case CompoundRef.Stored stored -> compoundService.getCompound(stored.getCompoundID()).getMolFile();
                        case CompoundRef.Virtual virtual -> virtual.getMolFile();
                        case CompoundRef.Unknown unknown -> null;
                    };
                    if (molfile != null) {
                        molecules.add(indigo.loadMolecule(molfile));
                    }
                }

                reactionIterable(reactionScheme, role).forEach(IndigoMolecule::remove);
                // TODO sometimes it adds in reverse order, sometimes not
                molecules.reversed().forEach(molecule -> addToReaction(reactionScheme, role, molecule));
            }

            byte[] picture = indigo.renderToBuffer(reactionScheme);
            reaction.setRxnfile(reactionScheme.rxnfile());
            experiment.setPicture(picture);
        });
    }
}
