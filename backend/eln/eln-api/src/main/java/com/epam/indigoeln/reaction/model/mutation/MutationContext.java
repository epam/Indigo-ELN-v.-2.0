package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.ReactionRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class MutationContext {

    private boolean affectsAttachments = false;
    private boolean affectsACL = false;
    private boolean affectsModel = false;

    private final Set<ReactionRole> affectedRoles = EnumSet.noneOf(ReactionRole.class);

    public static MutationContext createFull() {
        return new MutationContext(true, true, true);
    }
}
