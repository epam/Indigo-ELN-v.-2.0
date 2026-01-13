package com.epam.indigoeln.reaction.model.mutation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProjectMutationContext {

    private boolean affectsAttachments;
    private boolean affectsACL;

    public static ProjectMutationContext createFull() {
        return new ProjectMutationContext(true, true);
    }
}
