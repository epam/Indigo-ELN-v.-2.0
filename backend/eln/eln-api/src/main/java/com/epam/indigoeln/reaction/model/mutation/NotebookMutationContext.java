package com.epam.indigoeln.reaction.model.mutation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotebookMutationContext {

    private boolean affectsAttachments;
    private boolean affectsACL;

    public static NotebookMutationContext createFull() {
        return new NotebookMutationContext(true, true);
    }
}
