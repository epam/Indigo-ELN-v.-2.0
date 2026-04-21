package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.BaseEntity;
import com.epam.indigoeln.eln.entity.BaseRevisionEntity;
import com.epam.indigoeln.eln.entity.WithRevision;
import com.epam.indigoeln.reaction.service.AbstractUndoHelper;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
public abstract class AbstractMutationContext<E extends BaseEntity & WithRevision, S, R extends BaseRevisionEntity, C extends AbstractMutationContext<E, S, R, C>> {

    private AbstractUndoHelper<E, S, R, C>.@Nullable UndoInfo undoInfo;
}
