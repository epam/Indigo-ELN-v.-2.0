package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.exception.MutationNotUndoableException;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.*;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.common.exception.InvalidRequestException.fail;
import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ExperimentMutation.CreateExperiment.class)
class CreateExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.CreateExperiment> {

    @Inject
    DictionaryService dictionaryService;
    @Inject
    TemplateRepository templateRepository;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    ACLService aclService;
    @Inject
    UserService userService;

    @Override
    protected void doValidateAccess(ExperimentEntity experiment, ExperimentMutation.CreateExperiment mutation) {
        aclService.ensureAccess(experiment.getNotebook(), ApplicationPermission.CREATE_EXPERIMENTS);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, ExperimentMutation.CreateExperiment mutation) {
        experimentModelService.setModel(experiment, experimentModelService.createNewModel());
        experiment.setStatus(ExperimentStatus.OPEN);
        experiment.setDeleted(false);
        experiment.setTherapeuticArea(dictionaryService.lookup(BuiltInDictionary.THERAPEUTIC_AREA.name(), mutation.therapeuticArea()));
        experiment.setProjectCode(dictionaryService.lookup(BuiltInDictionary.PROJECT_CODE.name(), mutation.projectCode()));
        experiment.setDescription(mutation.description());
        experiment.setLinkedExperiments(Set.of());
        experiment.setContinuedFrom(Set.of());
        experiment.setContinuedTo(Set.of());

        experiment.setName(generateExperimentName(experiment.getNotebook()));
        experiment.setCreatedBy(userService.getCurrentUserEntity());
        experiment.setBatchCreator(experiment.getCreatedBy());
        aclService.initExperimentACL(experiment);
        return new MutationResult("Experiment created", null);
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
    public MutationResult doHandle(ExperimentEntity entity, @Nullable ExperimentModel model, ExperimentMutation.SetExperimentSignificantFigures mutation) {
        Preconditions.checkArgument(model != null);
        Integer oldValue = model.getSignificantFigures();
        model.setSignificantFigures(mutation.significantFigures());
        return new MutationResult(formatSetterSummary("significant figures", mutation.significantFigures()), new ExperimentMutation.SetExperimentSignificantFigures(oldValue));
    }

    @Override
    public boolean isAffectsModel() {
        return true;
    }

    @Override
    protected boolean isRequiresEditSession() {
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
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, ExperimentMutation.EditExperimentAttributes mutation) {
        List<String> summaryList = new ArrayList<>();
        boolean updated = false;
        updated |= editProperty(mutation.title()
                , experiment::setTitle
                , summaryList, "title"
        );
        updated |= editProperty(mutation.therapeuticArea()
                , v -> {
                    DictionaryItemEntity value = dictionaryService.lookup(BuiltInDictionary.THERAPEUTIC_AREA.name(), v);
                    experiment.setTherapeuticArea(value);
                }
                , summaryList
                , "therapeutic area"
        );
        updated |= editProperty(mutation.projectCode()
                , v -> {
                    DictionaryItemEntity value = dictionaryService.lookup(BuiltInDictionary.PROJECT_CODE.name(), v);
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
                , v -> experiment.setLinkedExperiments(experimentsFromRef(v))
                , summaryList
                , "linked experiments"
        );
        updated |= editProperty(mutation.continuedFrom()
                , v -> experiment.setContinuedFrom(experimentsFromRef(v))
                , summaryList
                , "continued from"
        );
        updated |= editProperty(mutation.continuedTo()
                , v -> experiment.setContinuedTo(experimentsFromRef(v))
                , summaryList
                , "continued to"
        );
        InvalidRequestException.validate(updated, "Nothing to update");
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList), null);
    }

    private Set<ExperimentEntity> experimentsFromRef(Collection<ExperimentRef> refs) {
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
    public boolean isAffectsModel() {
        return true;
    }

    @Override
    public MutationResult doHandle(ExperimentEntity entity, @Nullable ExperimentModel model, ExperimentMutation.SetBatchCreator mutation) {
        for (Reaction reaction : checkNotNull(model).getReactions()) {
            for (ReactionOutput row : reaction.getOutputs()) {
                if (row.hasSamplesWithRegistrationStarted()) {
                    fail("Cannot modify batch creator after at least one batch is submitted for registration");
                }
            }
        }
        UserRef oldValue = entity.getBatchCreator().toRef();
        UserEntity batchCreator = userRepository.get(mutation.batchCreator().getId());
        entity.setBatchCreator(batchCreator);
        return new MutationResult(
                formatSetterSummary("batch creator", batchCreator.getDisplayName()),
                new ExperimentMutation.SetBatchCreator(oldValue)
        );
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
    public boolean isAffectsACL() {
        return true;
    }

    @Override
    protected void doValidateAccess(ExperimentEntity experiment, ExperimentMutation.EditExperimentAccess mutation) {
        aclService.ensureAccess(experiment, ApplicationPermission.MANAGE_EXPERIMENT_ACCESS);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, ExperimentMutation.EditExperimentAccess mutation) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(experiment.getProject());
        aclService.updateExperimentACL(experiment.getNotebook().getProject(), experiment.getNotebook(), experiment, mutation.edits());
        // !!! create revisions for notebook/project, if they are affected
        return new MutationResult(summary, null);
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
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, ExperimentMutation.CreateExperimentAttachment mutation) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddExperimentAttachment(experiment, attachment);
        return new MutationResult("Created attachment: %s, %d bytes".formatted(attachment.getName(), attachment.getSize()), null);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.DeleteExperimentAttachment.class)
class DeleteExperimentAttachmentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.DeleteExperimentAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, ExperimentMutation.DeleteExperimentAttachment mutation) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().remove(attachment);
        attachment.getExperiments().remove(experiment);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName(), null);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ExperimentAccessUpdated.class)
class ExperimentAccessUpdatedHandler extends AbstractExperimentMutationHandler<ExperimentMutation.ExperimentAccessUpdated> {

    @Override
    public boolean isAffectsACL() {
        return true;
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, ExperimentMutation.ExperimentAccessUpdated mutation) {
        aclService.recalculateACL(experiment);
        String reason = mutation.projectName() != null ? "project " + mutation.projectName() : "notebook " + mutation.notebookName();
        return new MutationResult("Access updated because of the changes in " + reason, null);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.Undo.class)
class UndoHandler extends ExperimentMutationHandlerBase<ExperimentMutation.Undo> {

    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    ExperimentRevisionEntity initialRevision;
    Mutation reverseMutation;
    AbstractExperimentMutationHandler<Mutation> reverseHandler;

    @Override
    public boolean isAffectsAttachments() {
        return reverseHandler.isAffectsAttachments();
    }

    @Override
    public boolean isAffectsACL() {
        return reverseHandler.isAffectsACL();
    }

    @Override
    public boolean isAffectsModel() {
        return reverseHandler.isAffectsModel();
    }

    @Override
    protected boolean isRequiresEditSession() {
        return reverseHandler.isRequiresEditSession();
    }

    @Override
    protected void doValidateAccess(ExperimentEntity experiment, ExperimentMutation.Undo mutation) {
        reverseHandler.doValidateAccess(experiment, mutation);
    }

    @Override
    protected void doPrepare(ExperimentEntity experiment, ExperimentMutation.Undo mutation) {
        initialRevision = experimentRepository.getRevision(experiment, mutation.revision());
        if (initialRevision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(initialRevision.getSummary()));
        }
        reverseMutation = initialRevision.getReverseMutation();
        reverseHandler = mutationHandlerRegistry.findHandler(reverseMutation);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity entity, @Nullable ExperimentModel model, ExperimentMutation.Undo mutation) {
        reverseHandler.doHandle(entity, model, reverseMutation);
        affectedRoles.addAll(reverseHandler.affectedRoles);
        return new MutationResult("Undo: " + initialRevision.getSummary(), null);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.Redo.class)
class RedoHandler extends ExperimentMutationHandlerBase<ExperimentMutation.Redo> {

    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;
    
    ExperimentRevisionEntity initialRevision;
    Mutation initialMutation;
    AbstractExperimentMutationHandler<Mutation> initialHandler;

    @Override
    public boolean isAffectsAttachments() {
        return initialHandler.isAffectsAttachments();
    }

    @Override
    public boolean isAffectsACL() {
        return initialHandler.isAffectsACL();
    }

    @Override
    public boolean isAffectsModel() {
        return initialHandler.isAffectsModel();
    }

    @Override
    protected boolean isRequiresEditSession() {
        return initialHandler.isRequiresEditSession();
    }

    @Override
    protected void doValidateAccess(ExperimentEntity experiment, ExperimentMutation.Redo mutation) {
        initialHandler.doValidateAccess(experiment, mutation);
    }

    @Override
    protected void doPrepare(ExperimentEntity experiment, ExperimentMutation.Redo mutation) {
        initialRevision = experimentRepository.getRevision(experiment, mutation.revision());
        initialMutation = initialRevision.getMutation();
        initialHandler = mutationHandlerRegistry.findHandler(initialMutation);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, ExperimentMutation.Redo mutation) {
        // !!! verify revision was previously undone
        initialHandler.doHandle(experiment, model, initialMutation);
        affectedRoles.addAll(initialHandler.affectedRoles);
        return new MutationResult("Redo: " + initialRevision.getSummary(), null);
    }
}
