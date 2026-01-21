package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.MutationNotUndoableException;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.repository.TemplateRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

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
        TemplateEntity template = templateRepository.get(mutation.templateID());
        template.getExperiments().add(experiment);
        experiment.setTemplate(template);

        experiment.setName(generateExperimentName(experiment.getNotebook()));
        experiment.setCreatedBy(userService.getCurrentUserEntity());
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
@MutationHandlerFor(ExperimentMutation.EditExperimentAttributes.class)
class EditExperimentAttributesHandler extends ExperimentMutationHandlerBase<ExperimentMutation.EditExperimentAttributes> {

    @Inject
    DictionaryService dictionaryService;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.EditExperimentAttributes mutation) {
        List<String> summaryList = new ArrayList<>();
        editProperty(mutation.therapeuticArea()
                , v -> {
                    DictionaryItemEntity value = dictionaryService.lookup(BuiltInDictionary.THERAPEUTIC_AREA.name(), v);
                    experiment.setTherapeuticArea(value);
                }
                , summaryList
                , "therapeutic area"
        );
        editProperty(mutation.projectCode()
                , v -> {
                    DictionaryItemEntity value = dictionaryService.lookup(BuiltInDictionary.PROJECT_CODE.name(), v);
                    experiment.setProjectCode(value);
                }
                , summaryList
                , "project code"
        );
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList), null);
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
    UserService userService;
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
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.EditExperimentAccess mutation) {
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

    @Override
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.CreateExperimentAttachment mutation) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().add(attachment);
        attachment.getExperiments().add(experiment);
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
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.DeleteExperimentAttachment mutation) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().remove(attachment);
        attachment.getExperiments().remove(experiment);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName(), null);
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
        // !!! verify revision was undone
        initialHandler.doHandle(experiment, model, initialMutation);
        affectedRoles.addAll(initialHandler.affectedRoles);
        return new MutationResult("Redo: " + initialRevision.getSummary(), null);
    }
}
