package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.util.ExperimentModelUtil;
import com.epam.indigoeln.reaction.model.*;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class SnapshotMapper extends AbstractMapper {

    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "acl", ignore = true)
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "compoundRefs", ignore = true)
    @Mapping(target = "rxnFiles", ignore = true)
    @Mapping(target = "templateId", source = "template.id")
    public abstract ExperimentSnapshot copyBasicFields(ExperimentEntity entity);

    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "acl", ignore = true)
    public abstract ProjectSnapshot copyBasicFields(ProjectEntity entity);

    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "acl", ignore = true)
    public abstract NotebookSnapshot copyBasicFields(NotebookEntity entity);

    protected abstract AttachmentDTO convertAttachment(AttachmentEntity entity);

    protected abstract Set<AttachmentDTO> copyAttachments(List<AttachmentEntity> attachments);

    protected abstract Set<ACLDetailsEntryDTO> copyACL(ACLEntry[] aclEntries);

    protected Set<String> convertKeywords(List<DictionaryItemEntity> keywords) {
        return StreamEx.of(keywords).map(DictionaryItemEntity::getName).toSet();
    }

    public ExperimentSnapshot createSnapshot(ExperimentEntity experiment, boolean copyAttachments, boolean copyACL, @Nullable ExperimentModel model) {
        ExperimentSnapshot snapshot = copyBasicFields(experiment);
        if (copyAttachments) {
            snapshot.setAttachments(copyAttachments(experiment.getAttachments()));
        }
        if (copyACL) {
            snapshot.setAcl(copyACL(experiment.getFullACL()));
        }
        snapshot.setModel(model);
        if (model != null) {
            snapshot.setCompoundRefs(ExperimentModelUtil.collectCompoundRefs(model));
            snapshot.setRxnFiles(StreamEx.of(model.getReactions())
                    .mapToEntry(Reaction::getAnchor, Reaction::getRxnfile)
                    .nonNullValues()
                    .toMap()
            );
        }
        return snapshot;
    }

    public ProjectSnapshot createSnapshot(ProjectEntity project, boolean copyAttachments, boolean copyACL) {
        ProjectSnapshot snapshot = copyBasicFields(project);
        if (copyAttachments) {
            snapshot.setAttachments(copyAttachments(project.getAttachments()));
        }
        if (copyACL) {
            snapshot.setAcl(copyACL(project.getFullACL()));
        }
        return snapshot;
    }

    public NotebookSnapshot createSnapshot(NotebookEntity notebook, boolean copyAttachments, boolean copyACL) {
        NotebookSnapshot snapshot = copyBasicFields(notebook);
        if (copyAttachments) {
            snapshot.setAttachments(copyAttachments(notebook.getAttachments()));
        }
        if (copyACL) {
            snapshot.setAcl(copyACL(notebook.getFullACL()));
        }
        return snapshot;
    }
}
