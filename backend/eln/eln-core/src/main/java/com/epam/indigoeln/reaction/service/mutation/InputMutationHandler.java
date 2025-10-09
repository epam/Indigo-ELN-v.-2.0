package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;

import java.util.EnumSet;
import java.util.Set;

@ApplicationScoped
public class InputMutationHandler extends AbstractMutationHandler {

    @Inject
    ExperimentModelHelperService modelHelperService;

    public void handle(ExperimentEntity experiment, ExperimentModel model, ReactionInputMutation.SetInputRole mutation) {
        ReactionInput row = model.locate(mutation);

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

    public void handle(ExperimentModel model, ReactionInputMutation.SetLimiting mutation) {
        ReactionInput row = model.locate(mutation);
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
    }

    public void handle(ExperimentModel model, ReactionInputMutation.SetInputEQ mutation) {
        ReactionInput row = model.locate(mutation);
        row.setEq(mutation.eq() != null ? EnteredValue.userLastEntered(mutation.eq(), NoUnit.NO_UNIT) : EnteredValue.DEFAULT_ONE);
    }
}
