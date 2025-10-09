package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.*;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@Slf4j
@ApplicationScoped
public class SetSchemeHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoAPI indigo;
    @Inject
    IndigoRendererAPI indigoRenderer;
    @Inject
    ExperimentModelHelperService experimentModelHelperService;

    public void handle(ExperimentEntity experiment, ExperimentModel model, ReactionMutation.SetScheme mutation) {
        Reaction reaction = model.locate(mutation);
        reaction.setRxnfile(mutation.molFile());
        // TODO match into existing inputs/outputs
        reaction.setInputs(new ArrayList<>());
        reaction.setOutputs(new ArrayList<>());

        IndigoReaction indigoReaction = indigo.loadReaction(mutation.molFile());
        for (IndigoMolecule reactant : indigoReaction.reactants()) {
            reaction.getInputs().add(createInputLine(reaction, reactant, ReactionRole.REACTANT));
        }
        for (IndigoMolecule catalyst : indigoReaction.catalysts()) {
            reaction.getInputs().add(createInputLine(reaction, catalyst, ReactionRole.CATALYST));
        }
        for (IndigoMolecule product : indigoReaction.products()) {
            reaction.getOutputs().add(createOutputLine(reaction, product));
        }
        if (!reaction.getInputs().isEmpty() && reaction.getLimitingInput() == null) {
            reaction.getInputs().getFirst().setLimiting(true);
        }
        experimentModelHelperService.setReactionScheme(experiment, reaction, indigoReaction);
    }

    public void handle(ExperimentEntity experiment, ExperimentModel model, ReactionMutation.AddEmptyInput mutation) {
        Reaction reaction = model.locate(mutation);
        reaction.getInputs().add(createInputLine(reaction, null, ReactionRole.REACTANT));
    }

    public void handle(ExperimentEntity experiment, ExperimentModel model, ReactionMutation.RemoveInput mutation) {
        Reaction reaction = model.locate(mutation);
        ReactionInput input = model.locateReactionInput(mutation.inputRow());
        reaction.getInputs().remove(input);
        experimentModelHelperService.rebuildReactionScheme(experiment, reaction, Set.of(input.getRole()));
    }

    private ReactionInput createInputLine(Reaction reaction, @Nullable IndigoMolecule molecule, ReactionRole role) {
        ReactionInput row = new ReactionInput(reaction, UUID.randomUUID(), role);
        row.setCompound(molecule != null
                ? compoundService.virtualCompoundRef(molecule, null, null, null)
                : compoundService.unknownCompoundRef());
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = new ReactionInputSample(row, UUID.randomUUID());
        reactionInputSample.setPurity(DEFAULT_ONE);
        row.setSamples(List.of(reactionInputSample));
        return row;
    }

    private ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule) {
        ReactionOutput row = new ReactionOutput(reaction, UUID.randomUUID(), reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL);
        row.setCompound(compoundService.virtualCompoundRef(molecule, null, null, null));
        row.setEq(DEFAULT_ONE);
        row.setSamples(List.of());
        return row;
    }
}
