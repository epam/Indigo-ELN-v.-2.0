package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class ExperimentSnapshotMapper extends AbstractMapper {

    @Inject
    ObjectMapper objectMapper;

    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "acl", ignore = true)
    @Mapping(target = "model", ignore = true)
    public abstract ExperimentSnapshot copyBasicFields(ExperimentEntity entity);

    protected abstract AttachmentDTO convertAttachment(AttachmentEntity entity);

    public abstract Set<AttachmentDTO> copyAttachments(List<AttachmentEntity> attachments);

    public abstract Set<ACLDetailsEntryDTO> copyACL(ACLEntry[] aclEntries);

    public ExperimentSnapshot createSnapshot(ExperimentEntity experiment, MutationContext context, Supplier<ExperimentModel> modelFn) {
        ExperimentSnapshot snapshot = copyBasicFields(experiment);
        if (context.isAffectsAttachments()) {
            snapshot.setAttachments(copyAttachments(experiment.getAttachments()));
        }
        if (context.isAffectsACL()) {
            snapshot.setAcl(copyACL(experiment.getFullACL()));
        }
        if (context.isAffectsModel()) {
            snapshot.setModel(modelFn.get());
        }
        return snapshot;
    }
}
