package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;

import java.util.EnumSet;
import java.util.Set;

@ApplicationScoped
public class InputMutationHandler extends AbstractMutationHandler {

    @Inject
    ExperimentModelHelperService modelHelperService;

    public void handle(ExperimentEntity experiment, ExperimentModel model, ReactionInput row, ReactionInputMutation.SetInputRole mutation) {
        StreamEx.of(row.getReaction().getInputs())
                .filter(x -> x != row && x.getRole() == row.getRole() && x.getCompound().equals(row.getCompound()))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalStateException("Input with the same role and compound already exists");
                });

        Set<ReactionRole> affectedRoles = EnumSet.noneOf(ReactionRole.class);
        affectedRoles.add(row.getRole());
        affectedRoles.add(mutation.role());
        row.setRole(mutation.role());

        modelHelperService.rebuildReactionScheme(experiment, row.getReaction(), affectedRoles);
    }

    public void handle(ExperimentModel model, ReactionInput row, ReactionInputMutation.SetInputMol mutation) {
        row.setMol(EnteredValue.userLastEntered(mutation.mol(), mutation.molUnit()));
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
    }

    public void handle(ExperimentModel model, ReactionInput row, ReactionInputMutation.SetLimiting mutation) {
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
    }

    public void handle(ExperimentModel model, ReactionInput row, ReactionInputMutation.SetInputEQ mutation) {
        row.setEq(EnteredValue.userLastEntered(MoreObjects.firstNonNull(mutation.eq(), 1.0), NoUnit.NO_UNIT));
    }
}
