package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.eln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.eln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.eln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.util.ExperimentModelUtil;
import com.google.common.collect.Iterables;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@ApplicationScoped
public class ExperimentModelHelperService {

    private static final ReactionRole[] SCHEMA_ORDER = new ReactionRole[]{ReactionRole.REACTANT, ReactionRole.REAGENT, ReactionRole.CATALYST, ReactionRole.OUTPUT};

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoAPI indigo;
    @Inject
    IndigoRendererAPI indigoRenderer;

    public byte[] rebuildReactionPicture(ExperimentEntity experiment, IndigoReaction indigoReaction) {
        indigoRenderer.setRenderOptions("svg", 500, 200);
        return indigoRenderer.renderToBuffer(indigoReaction);
    }

    public List<Object> makeReactionKey(Reaction reaction) {
        List<Object> key = new ArrayList<>();
        for (ReactionRow row : Iterables.concat(reaction.getInputs(), reaction.getOutputs())) {
            ReactionRole role = ExperimentModelUtil.getRoleInSchema(row);
            if (role != null) {
                key.add(role);
                key.add(checkNotNull(row.getCompound().getCompoundID()));
            }
        }
        return key;
    }

    public IndigoReaction rebuildReactionRxnFile(List<ReactionInput> inputs, List<ReactionOutput> outputs) {
        IndigoReaction indigoReaction = indigo.createReaction();
        for (ReactionRow row : Iterables.concat(inputs, outputs)) {
            row.setRxnPosition(null);
        }
        for (ReactionRole role : SCHEMA_ORDER) {
            int rxnPosition = -1;
            for (ReactionRow row : Iterables.concat(inputs, outputs)) {
                if (ExperimentModelUtil.getRoleInSchema(row) == role) {
                    row.setRxnPosition(++rxnPosition);
                    CompoundEntity compound = compoundService.getCompound(checkNotNull(row.getCompound().getCompoundID()));
                    IndigoMolecule molecule = indigo.loadMolecule(compound.getMolFile());
                    switch (role) {
                        case REACTANT -> indigoReaction.addReactant(molecule);
                        case REAGENT, CATALYST -> indigoReaction.addCatalyst(molecule);
                        case OUTPUT -> indigoReaction.addProduct(molecule);
                    }
                }
            }
        }
        return indigoReaction;
    }
}
