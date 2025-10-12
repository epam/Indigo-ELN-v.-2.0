package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@Slf4j
@Dependent
public class SetSchemeHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoAPI indigo;
    @Inject
    ExperimentModelHelperService experimentModelHelperService;

    public void handle(Reaction reaction, ReactionMutation.SetScheme mutation) {
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

    public void handle(Reaction reaction, ReactionMutation.AddEmptyInput mutation) {
        reaction.getInputs().add(createInputLine(reaction, null, ReactionRole.REACTANT));
    }

    public void handle(Reaction reaction, ReactionMutation.RemoveInput mutation) {
        ReactionInput input = model.locate(mutation.input());
        reaction.getInputs().remove(input);
        experimentModelHelperService.rebuildReactionScheme(experiment, reaction, Set.of(input.getRole()));
    }

    private ReactionInput createInputLine(Reaction reaction, @Nullable IndigoMolecule molecule, ReactionRole role) {
        ReactionInput row = ReactionInput.create(reaction, role);
        row.setCompound(molecule != null
                ? compoundService.virtualCompoundRef(molecule, null, null, null)
                : compoundService.unknownCompoundRef());
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = ReactionInputSample.create(row);
        reactionInputSample.setPurity(DEFAULT_ONE);
        row.setSamples(List.of(reactionInputSample));
        return row;
    }

    private ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule) {
        ReactionOutput row = ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL);
        row.setCompound(compoundService.virtualCompoundRef(molecule, null, null, null));
        row.setEq(DEFAULT_ONE);
        row.setSamples(List.of());
        return row;
    }
}
