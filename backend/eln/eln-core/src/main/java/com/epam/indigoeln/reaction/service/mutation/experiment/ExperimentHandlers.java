package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ExperimentRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.repository.UserRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reaction.service.mutation.EntityMutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.common.exception.InvalidRequestException.fail;
import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.common.util.ModelUtil.updateCollection;
import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ExperimentMutation.CreateExperiment.class)
class CreateExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.CreateExperiment> {

    @Inject
    DictionaryService dictionaryService;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    ACLService aclService;
    @Inject
    UserService userService;

    @Override
    protected void doValidateAccess(ExperimentEntity experiment, ExperimentMutation.CreateExperiment mutation, ExperimentMutationContext context) {
        aclService.ensureAccess(experiment.getNotebook(), ApplicationPermission.CREATE_EXPERIMENTS);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.CreateExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        experiment.setStatus(ExperimentStatus.OPEN);
        experiment.setDeleted(false);
        experiment.setTherapeuticArea(dictionaryService.lookup(mutation.therapeuticArea()));
        experiment.setProjectCode(dictionaryService.lookup(mutation.projectCode()));
        experiment.setDescription(mutation.description());

        experiment.setName(generateExperimentName(experiment.getNotebook()));
        experiment.setCreatedBy(userService.getCurrentUserEntity());
        experiment.setBatchCreator(experiment.getCreatedBy());
        aclService.initExperimentACL(experiment);
        return new MutationResult("Experiment created");
    }

    private String generateExperimentName(NotebookEntity notebook) {
        String last = experimentRepository.getLastExperimentName(notebook);
        int lastNumber = last == null ? 0 : Integer.parseInt(last.substring(last.lastIndexOf('-') + 1));
        return "%s-%04d".formatted(notebook.getName(), lastNumber + 1);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SetExperimentSignificantFigures.class)
class SetExperimentSignificantFiguresHandler extends ExperimentMutationHandlerBase<ExperimentMutation.SetExperimentSignificantFigures> {

    @Override
    public MutationResult doHandle(ExperimentEntity entity, ExperimentMutation.SetExperimentSignificantFigures mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        entity.getModel().setSignificantFigures(mutation.significantFigures());
        return new MutationResult(formatSetterSummary("significant figures", mutation.significantFigures()));
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.EditExperimentAttributes.class)
class EditExperimentAttributesHandler extends ExperimentMutationHandlerBase<ExperimentMutation.EditExperimentAttributes> {

    @Inject
    DictionaryService dictionaryService;
    @Inject
    EntityMutationHelper entityMutationHelper;
    @Inject
    ExperimentRepository experimentRepository;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.EditExperimentAttributes mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        List<String> summaryList = new ArrayList<>();
        boolean updated = false;
        updated |= editProperty(mutation.title()
                , experiment::setTitle
                , summaryList, "title"
        );
        updated |= editProperty(mutation.therapeuticArea()
                , v -> {
                    DictionaryItemEntity value = dictionaryService.lookup(v);
                    experiment.setTherapeuticArea(value);
                }
                , summaryList
                , "therapeutic area"
        );
        updated |= editProperty(mutation.projectCode()
                , v -> {
                    DictionaryItemEntity value = dictionaryService.lookup(v);
                    experiment.setProjectCode(value);
                }
                , summaryList
                , "project code"
        );
        updated |= editProperty(mutation.description()
                , experiment::setDescription
                , summaryList
                , "description"
        );
        updated |= editProperty(mutation.literature()
                , experiment::setLiterature
                , summaryList
                , "literature"
        );
        updated |= editProperty(mutation.linkedExperiments()
                , v -> {
                    updateCollection(experiment.getLinkedExperiments(), experimentsFromRefs(v));
                }
                , summaryList
                , "linked experiments"
        );
        updated |= editProperty(mutation.continuedFrom()
                , v -> {
                    updateCollection(experiment.getContinuedFrom(), experimentsFromRefs(v));
                }
                , summaryList
                , "continued from"
        );
        updated |= editProperty(mutation.continuedTo()
                , v -> {
                    updateCollection(experiment.getContinuedTo(), experimentsFromRefs(v));
                }
                , summaryList
                , "continued to"
        );
        validate(updated, "Nothing to update");
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList));
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void doRestoreStateAfterUndo(ExperimentEntity experiment, ExperimentSnapshot snapshot, ExperimentMutation.EditExperimentAttributes mutation) {
        experiment.setTitle(snapshot.getTitle());
        experiment.setTherapeuticArea(dictionaryService.lookup(snapshot.getTherapeuticArea()));
        experiment.setProjectCode(dictionaryService.lookup(snapshot.getProjectCode()));
        experiment.setDescription(snapshot.getDescription());
        experiment.setLiterature(snapshot.getLiterature());
        if (mutation.linkedExperiments().isPresent()) {
            updateCollection(experiment.getLinkedExperiments(), experimentsFromRefs(snapshot.getLinkedExperiments()));
        }
        if (mutation.continuedFrom().isPresent()) {
            updateCollection(experiment.getContinuedFrom(), experimentsFromRefs(snapshot.getContinuedFrom()));
        }
        if (mutation.continuedTo().isPresent()) {
            updateCollection(experiment.getContinuedTo(), experimentsFromRefs(snapshot.getContinuedTo()));
        }
    }

    private Set<ExperimentEntity> experimentsFromRefs(Collection<ExperimentRef> refs) {
        return StreamEx.of(refs)
                .map(ref -> experimentRepository.getReference(ref.getId()))
                .toSet();
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SetBatchCreator.class)
class SetBatchCreatorHandler extends ExperimentMutationHandlerBase<ExperimentMutation.SetBatchCreator> {

    @Inject
    UserRepository userRepository;

    @Override
    public MutationResult doHandle(ExperimentEntity entity, ExperimentMutation.SetBatchCreator mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        for (Reaction reaction : entity.getModel().getReactions()) {
            for (ReactionOutput row : reaction.getOutputs()) {
                if (row.hasSamplesWithRegistrationStarted()) {
                    fail("Cannot modify batch creator after at least one batch is submitted for registration");
                }
            }
        }
        UserInfo batchCreator = userService.getUserInfo(mutation.batchCreator());
        entity.setBatchCreator(userRepository.getReference(batchCreator.getId()));
        return new MutationResult(formatSetterSummary("batch creator", batchCreator.getDisplayName()));
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void doRestoreStateAfterUndo(ExperimentEntity experiment, ExperimentSnapshot snapshot, ExperimentMutation.SetBatchCreator mutation) {
        UserInfo batchCreator = userService.getUserInfo(snapshot.getBatchCreator());
        experiment.setBatchCreator(userRepository.getReference(batchCreator.getId()));
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.EditExperimentAccess.class)
class EditExperimentAccessHandler extends ExperimentMutationHandlerBase<ExperimentMutation.EditExperimentAccess> {

    @Inject
    ACLService aclService;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    protected void doValidateAccess(ExperimentEntity experiment, ExperimentMutation.EditExperimentAccess mutation, ExperimentMutationContext context) {
        aclService.ensureAccess(experiment, ApplicationPermission.MANAGE_EXPERIMENT_ACCESS);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.EditExperimentAccess mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(experiment.getProject());
        aclService.updateExperimentACL(experiment.getNotebook().getProject(), experiment.getNotebook(), experiment, mutation.edits());
        // !!! create revisions for notebook/project, if they are affected
        return new MutationResult(summary);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.CreateExperimentAttachment.class)
class CreateExperimentAttachmentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.CreateExperimentAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;
    @Inject
    AttachmentService attachmentService;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.CreateExperimentAttachment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddExperimentAttachment(experiment, attachment);
        return new MutationResult("Created attachment: %s, %d bytes".formatted(attachment.getName(), attachment.getSize()));
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void doRestoreStateAfterUndo(ExperimentEntity experiment, ExperimentSnapshot snapshot, ExperimentMutation.CreateExperimentAttachment mutation) {
        updateCollection(experiment.getAttachments(), attachmentRepository.getReferences(checkNotNull(snapshot.getAttachments())));
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.DeleteExperimentAttachment.class)
class DeleteExperimentAttachmentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.DeleteExperimentAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.DeleteExperimentAttachment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().remove(attachment);
        attachment.getExperiments().remove(experiment);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName());
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void doRestoreStateAfterUndo(ExperimentEntity experiment, ExperimentSnapshot snapshot, ExperimentMutation.DeleteExperimentAttachment mutation) {
        updateCollection(experiment.getAttachments(), attachmentRepository.getReferences(checkNotNull(snapshot.getAttachments())));
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ExperimentAccessUpdated.class)
class ExperimentAccessUpdatedHandler extends AbstractExperimentMutationHandler<ExperimentMutation.ExperimentAccessUpdated> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.ExperimentAccessUpdated mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        aclService.recalculateACL(experiment);
        String reason = mutation.projectName() != null ? "project " + mutation.projectName() : "notebook " + mutation.notebookName();
        return new MutationResult("Access updated because of the changes in " + reason);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.Undo.class)
class ExperimentUndoHandler extends ExperimentMutationHandlerBase<ExperimentMutation.Undo> {

    @Inject
    ExperimentUndoHelper experimentUndoHelper;
    @Inject
    UserService userService;

    @Override
    public void doPrepare(ExperimentEntity entity, ExperimentMutation.Undo mutation, ExperimentMutationContext context) {
        experimentUndoHelper.doPrepare(entity, userService.getCurrentUserEntity(), false, context);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity entity, ExperimentMutation.Undo mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        return experimentUndoHelper.doHandle(entity, snapshotBefore, context, false);
    }

    @Override
    protected ExperimentRevisionEntity doCreateRevision(ExperimentEntity experiment, ExperimentMutation.Undo mutation, MutationResult result, Integer revisionNo, JsonNode patch, ExperimentMutationContext context, ExperimentSnapshot snapshotAfter) {
        ExperimentRevisionEntity revision = super.doCreateRevision(experiment, mutation, result, revisionNo, patch, context, snapshotAfter);
        revision.setUndoFor(checkNotNull(context.getUndoInfo()).getRevision().getRevisionNo());
        return revision;
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.Redo.class)
class ExperimentRedoHandler extends ExperimentMutationHandlerBase<ExperimentMutation.Redo> {

    @Inject
    ExperimentUndoHelper experimentUndoHelper;
    @Inject
    UserService userService;

    @Override
    public void doPrepare(ExperimentEntity entity, ExperimentMutation.Redo mutation, ExperimentMutationContext context) {
        experimentUndoHelper.doPrepare(entity, userService.getCurrentUserEntity(), true, context);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity entity, ExperimentMutation.Redo mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        return experimentUndoHelper.doHandle(entity, snapshotBefore, context, true);
    }

    @Override
    protected ExperimentRevisionEntity doCreateRevision(ExperimentEntity experiment, ExperimentMutation.Redo mutation, MutationResult result, Integer revisionNo, JsonNode patch, ExperimentMutationContext context, ExperimentSnapshot snapshotAfter) {
        ExperimentRevisionEntity revision = super.doCreateRevision(experiment, mutation, result, revisionNo, patch, context, snapshotAfter);
        revision.setRedoFor(checkNotNull(context.getUndoInfo()).getRevision().getRevisionNo());
        return revision;
    }
}
