package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.service.mutation.AbstractMutationContext;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@ToString
public class ExperimentMutationContext extends AbstractMutationContext<ExperimentEntity, ExperimentSnapshot, ExperimentRevisionEntity, ExperimentMutationContext> {

    private boolean affectsModel;
    private boolean affectsAttachments;
    private boolean affectsACL;
    private boolean requiresEditSession;
    private boolean schemaAffected;

    @Nullable
    private Integer createdVersion;
}
