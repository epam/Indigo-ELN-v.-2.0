package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;

@Dependent
public class InputMutationHandler extends AbstractMutationHandler {

    @Inject
    ExperimentModelHelperService modelHelperService;

    public void handle(ReactionInput row, ReactionInputMutation.SetInputRowRole mutation) {
        StreamEx.of(row.getReaction().getInputs())
                .filter(x -> x != row && x.getRole() == row.getRole() && x.getCompound().equals(row.getCompound()))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalStateException("Input with the same role and compound already exists");
                });

        affectedRoles.add(row.getRole());
        affectedRoles.add(mutation.role());
        row.setRole(mutation.role());
    }

    public void handle(ReactionInput row, ReactionInputMutation.SetInputRowMol mutation) {
        row.setMol(EnteredValue.userLastEntered(mutation.mol(), mutation.molUnit()));
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
    }

    public void handle(ReactionInput row, ReactionInputMutation.SetInputRowChemicalName mutation) {
        row.setChemicalName(mutation.chemicalName());
    }

    public void handle(ReactionInput row, ReactionInputMutation.SetInputRowLimiting mutation) {
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
    }

    public void handle(ReactionInput row, ReactionInputMutation.SetInputRowEQ mutation) {
        row.setEq(EnteredValue.userLastEntered(MoreObjects.firstNonNull(mutation.eq(), 1.0), NoUnit.NO_UNIT));
    }
}
