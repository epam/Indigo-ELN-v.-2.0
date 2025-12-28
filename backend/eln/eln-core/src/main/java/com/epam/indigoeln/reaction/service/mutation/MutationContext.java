package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ReactionRole;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public class MutationContext {

    private final Set<ReactionRole> affectedRoles = EnumSet.noneOf(ReactionRole.class);
}
