package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ExperimentModelHelperService {

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoAPI indigo;
    @Inject
    IndigoRendererAPI indigoRenderer;

    public void rebuildReactionPicture(ExperimentEntity experiment, Reaction reaction, IndigoReaction indigoReaction) {
        indigoRenderer.setRenderOptions("svg", 500, 200);
        byte[] buf = indigoRenderer.renderToBuffer(indigoReaction);
        experiment.setPicture(buf);
    }

    public IndigoReaction rebuildReactionRxnFile(Reaction reaction) {
        IndigoReaction indigoReaction = indigo.createReaction();
        int reactantPosition = -1;
        for (ReactionInput input : reaction.getInputs()) {
            if (input.getRole() == ReactionRole.REACTANT && input.getCompound().getCompoundID() != null) {
                input.setRxnPosition(++reactantPosition);
                CompoundEntity compound = compoundService.getCompound(input.getCompound().getCompoundID());
                indigoReaction.addReactant(indigo.loadMolecule(compound.getMolFile()));
            }
        }
        int catalystPosition = -1;
        for (ReactionInput input : reaction.getInputs()) {
            if (input.getRole() == ReactionRole.CATALYST && input.getCompound().getCompoundID() != null) {
                input.setRxnPosition(++catalystPosition);
                CompoundEntity compound = compoundService.getCompound(input.getCompound().getCompoundID());
                indigoReaction.addReactant(indigo.loadMolecule(compound.getMolFile()));
            }
        }
        int productPosition = -1;
        for (ReactionOutput output : reaction.getOutputs()) {
            if (output.isIntended() && output.getCompound().getCompoundID() != null) {
                output.setRxnPosition(++productPosition);
                CompoundEntity compound = compoundService.getCompound(output.getCompound().getCompoundID());
                indigoReaction.addProduct(indigo.loadMolecule(compound.getMolFile()));
            }
        }
        reaction.setRxnfile(indigoReaction.rxnfile());
        return indigoReaction;
    }
}
