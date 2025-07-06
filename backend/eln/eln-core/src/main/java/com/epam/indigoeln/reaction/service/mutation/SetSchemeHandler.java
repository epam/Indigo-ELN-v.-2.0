package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.indigowrapper.IndigoWrapper;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@Slf4j
@ApplicationScoped
public class SetSchemeHandler extends AbstractMutationHandler {

    @Inject
    IndigoWrapper indigo;

    public void handle(ExperimentEntity experiment, ExperimentModel model, ReactionMutation.SetScheme mutation) {
        Reaction reaction = model.getReactions().get(mutation.reactionNo());
        reaction.setMolFile(mutation.molFile());
        // TODO match into existing inputs/outputs
        reaction.setInputs(new ArrayList<>());
        reaction.setOutputs(new ArrayList<>());

        indigo.withSession(indigoSession -> {
            IndigoReaction indigoReaction = indigoSession.loadReaction(mutation.molFile());
            for (IndigoMolecule reactant : indigoReaction.reactants()) {
                reaction.getInputs().add(createInputLine(reaction, reactant, ReactionInputRole.REACTANT));
            }
            for (IndigoMolecule catalyst : indigoReaction.catalysts()) {
                reaction.getInputs().add(createInputLine(reaction, catalyst, ReactionInputRole.CATALYST));
            }
            for (IndigoMolecule product : indigoReaction.products()) {
                reaction.getOutputs().add(createOutputLine(reaction, product));
            }
            if (!reaction.getInputs().isEmpty() && reaction.getLimitingInput() == null) {
                reaction.getInputs().getFirst().setLimiting(true);
            }
            indigoSession.setOption("render-output-format", "svg");
            indigoSession.setOption("render-image-size", 500, 200);
            byte[] buf = indigoSession.renderToBuffer(indigoReaction);
            experiment.setPicture(buf);
        });
    }

    private ReactionInput createInputLine(Reaction reaction, IndigoMolecule molecule, ReactionInputRole role) {
        ReactionInput row = new ReactionInput();
        row.setReaction(reaction);
        row.setRole(role);
        row.setCompound(virtualCompoundRef(molecule));
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = new ReactionInputSample();
        reactionInputSample.setRow(row);
        reactionInputSample.setPurity(DEFAULT_ONE);
        row.setSamples(List.of(reactionInputSample));
        return row;
    }

    private ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule) {
        ReactionOutput row = new ReactionOutput();
        row.setReaction(reaction);
        row.setCompound(virtualCompoundRef(molecule));
        row.setEq(DEFAULT_ONE);
        boolean hasFinalProduct = false;
        for (ReactionOutput output : reaction.getOutputs()) {
            if (output.getType() == ReactionOutputType.FINAL) {
                hasFinalProduct = true;
                break;
            }
        }
        row.setType(hasFinalProduct ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL);
        row.setSamples(List.of());
        return row;
    }
}
