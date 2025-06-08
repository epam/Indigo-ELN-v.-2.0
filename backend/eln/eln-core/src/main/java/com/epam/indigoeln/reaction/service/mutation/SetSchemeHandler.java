package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.compound.config.IndigoAPI;
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
    IndigoAPI indigo;

    public void handle(ExperimentModel model, ReactionMutation.SetScheme mutation) {
        log.debug("!!! Applying SetScheme mutation: {}", mutation);
        Reaction reaction = model.getReactions().get(mutation.reactionNo());
        reaction.setMolFile(mutation.molFile());
        // TODO match into existing inputs/outputs
        reaction.setInputs(new ArrayList<>());
        reaction.setOutputs(new ArrayList<>());

        log.debug("!!! before indigo");
        IndigoObject indigoReaction = indigo.loadReaction(mutation.molFile());
        log.debug("!!! after indigo");
        for (IndigoObject iter = indigoReaction.iterateReactants(); iter.hasNext(); ) {
            log.debug("!!! indigo 1");
            IndigoObject reactant = iter.next();
            log.debug("!!! indigo 2");
            reaction.getInputs().add(createInputLine(reaction, reactant, ReactionInputRole.REACTANT));
            log.debug("!!! indigo 3");
        }
        log.debug("!!! indigo 4");
        for (IndigoObject iter = indigoReaction.iterateCatalysts(); iter.hasNext(); ) {
            log.debug("!!! indigo 5");
            IndigoObject catalyst = iter.next();
            log.debug("!!! indigo 6");
            reaction.getInputs().add(createInputLine(reaction, catalyst, ReactionInputRole.CATALYST));
            log.debug("!!! indigo 7");
        }
        log.debug("!!! indigo 8");
        for (IndigoObject iter = indigoReaction.iterateProducts(); iter.hasNext(); ) {
            log.debug("!!! indigo 9");
            IndigoObject product = iter.next();
            log.debug("!!! indigo 10");
            reaction.getOutputs().add(createOutputLine(reaction, product));
            log.debug("!!! indigo 11");
        }
        log.debug("!!! indigo 12");
        if (!reaction.getInputs().isEmpty() && reaction.getLimitingInput() == null) {
            reaction.getInputs().getFirst().setLimiting(true);
        }
        log.debug("!!! indigo 13");
    }

    private ReactionInput createInputLine(Reaction reaction, IndigoObject indigoObject, ReactionInputRole role) {
        ReactionInput row = new ReactionInput();
        row.setReaction(reaction);
        row.setRole(role);
        row.setCompound(virtualCompoundRef(indigoObject));
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = new ReactionInputSample();
        reactionInputSample.setRow(row);
        reactionInputSample.setPurity(DEFAULT_ONE);
        row.setSamples(List.of(reactionInputSample));
        return row;
    }

    private ReactionOutput createOutputLine(Reaction reaction, IndigoObject indigoObject) {
        ReactionOutput row = new ReactionOutput();
        row.setReaction(reaction);
        row.setCompound(virtualCompoundRef(indigoObject));
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
