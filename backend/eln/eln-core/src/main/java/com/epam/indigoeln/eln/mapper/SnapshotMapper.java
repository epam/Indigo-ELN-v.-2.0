package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class SnapshotMapper extends AbstractMapper {

    @Inject
    ExperimentModelService experimentModelService;

    @Mapping(target = "model", ignore = true)
    @Mapping(target = "acl", source = "fullACL")
    @Mapping(target = "templateId", source = "template.id")
    public abstract ExperimentSnapshot copyBasicFields(ExperimentEntity entity);

    @Mapping(target = "acl", source = "fullACL")
    public abstract ProjectSnapshot createSnapshot(ProjectEntity entity);

    @Mapping(target = "acl", source = "fullACL")
    public abstract NotebookSnapshot createSnapshot(NotebookEntity entity);

    protected abstract AttachmentDTO convertAttachment(AttachmentEntity entity);

    protected abstract Set<AttachmentDTO> convertAttachments(List<AttachmentEntity> attachments);

    protected abstract Set<ACLEntryDTO> convertACLs(ACLEntry[] aclEntries);

    protected Set<String> convertKeywords(List<DictionaryItemEntity> keywords) {
        return StreamEx.of(keywords).map(DictionaryItemEntity::getName).toSet();
    }

    public ExperimentSnapshot createSnapshot(ExperimentEntity experiment, boolean snapshotModel) {
        ExperimentSnapshot snapshot = copyBasicFields(experiment);
        snapshot.setModel(snapshotModel
                ? experimentModelService.deepCopy(experiment.getModel())
                : experiment.getModel()
        );
        return snapshot;
    }
}
