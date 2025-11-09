package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionRole;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumSet;
import java.util.Set;

public abstract class AbstractMutationHandler {

    @Setter
    protected ExperimentEntity experiment;
    @Setter
    protected ExperimentModel model;

    @Getter
    protected boolean compoundsAffected = false;
    @Getter
    protected Set<ReactionRole> affectedRoles = EnumSet.noneOf(ReactionRole.class);
}
