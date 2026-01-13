package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutationContext;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class SnapshotMapper extends AbstractMapper {

    @Inject
    ObjectMapper objectMapper;

    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "acl", ignore = true)
    @Mapping(target = "model", ignore = true)
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

    public ProjectSnapshot createSnapshot(ProjectEntity project, ProjectMutationContext context) {
        ProjectSnapshot snapshot = copyBasicFields(project);
        if (context.isAffectsAttachments()) {
            snapshot.setAttachments(copyAttachments(project.getAttachments()));
        }
        if (context.isAffectsACL()) {
            snapshot.setAcl(copyACL(project.getFullACL()));
        }
        return snapshot;
    }

    public NotebookSnapshot createSnapshot(NotebookEntity notebook, NotebookMutationContext context) {
        NotebookSnapshot snapshot = copyBasicFields(notebook);
        if (context.isAffectsAttachments()) {
            snapshot.setAttachments(copyAttachments(notebook.getAttachments()));
        }
        if (context.isAffectsACL()) {
            snapshot.setAcl(copyACL(notebook.getFullACL()));
        }
        return snapshot;
    }
}
