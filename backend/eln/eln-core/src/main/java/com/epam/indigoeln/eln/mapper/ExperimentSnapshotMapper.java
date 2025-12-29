package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class ExperimentSnapshotMapper extends AbstractMapper {

    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "acl", ignore = true)
    @Mapping(target = "model", ignore = true)
    public abstract ExperimentSnapshot copyBasicFields(ExperimentEntity entity);

    protected abstract AttachmentDTO convertAttachment(AttachmentEntity entity);

    public abstract Set<AttachmentDTO> copyAttachments(List<AttachmentEntity> attachments);

    public abstract Set<ACLEntryDTO> copyACL(ACLEntry[] aclEntries);
}
