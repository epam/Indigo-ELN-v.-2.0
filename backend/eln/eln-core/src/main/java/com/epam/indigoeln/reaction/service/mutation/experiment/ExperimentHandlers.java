package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.MutationNotUndoableException;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
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
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
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
class CreateExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.CreateExperiment> {

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

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.CreateExperiment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        experiment.setTherapeuticArea(dictionaryService.lookup(BuiltInDictionary.THERAPEUTIC_AREA.name(), mutation.therapeuticArea()));
        experiment.setProjectCode(dictionaryService.lookup(BuiltInDictionary.PROJECT_CODE.name(), mutation.projectCode()));
        experiment.setDescription(mutation.description());
        TemplateEntity template = templateRepository.get(mutation.templateID());
        template.getExperiments().add(experiment);
        experiment.setTemplate(template);

        experiment.setName(generateExperimentName(experiment.getNotebook()));
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
@MutationHandlerFor(ExperimentMutation.EditExperimentAttributes.class)
class EditExperimentAttributesHandler extends AbstractExperimentMutationHandler<ExperimentMutation.EditExperimentAttributes> {

    @Inject
    DictionaryService dictionaryService;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.EditExperimentAttributes mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
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
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList));
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.EditExperimentAccess.class)
class EditExperimentAccessHandler extends AbstractExperimentMutationHandler<ExperimentMutation.EditExperimentAccess> {

    @Inject
    ACLService aclService;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    UserService userService;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    protected boolean isAffectsACL() {
        return true;
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.EditExperimentAccess mutation, MutationRedoInfo redoInfo, MutationContext context) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(experiment.getProject());
        aclService.updateExperimentACL(experiment.getNotebook().getProject(), experiment.getNotebook(), experiment, mutation.edits());
        // !!! create revisions for notebook/project, if they are affected
        return new MutationResult(summary);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.CreateExperimentAttachment.class)
class CreateExperimentAttachmentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.CreateExperimentAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    protected boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.CreateExperimentAttachment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().add(attachment);
        attachment.getExperiments().add(experiment);
        return new MutationResult("Created attachment: %s, %d bytes".formatted(attachment.getName(), attachment.getSize()));
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.DeleteExperimentAttachment.class)
class DeleteExperimentAttachmentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.DeleteExperimentAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    protected boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.DeleteExperimentAttachment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().remove(attachment);
        attachment.getExperiments().remove(experiment);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName());
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.Undo.class)
class UndoHandler extends AbstractExperimentMutationHandler<ExperimentMutation.Undo> {

    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    @Override
    protected boolean isAffectsAttachments() {
        return true; // !!!
    }

    @Override
    protected boolean isAffectsACL() {
        return true; // !!!
    }

    @Override
    protected boolean isAffectsModel() {
        return true; // !!!
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.Undo mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        ExperimentRevisionEntity initialRevision = experimentRepository.getRevision(experiment, mutation.revision());
        if (initialRevision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(initialRevision.getSummary()));
        }
        Mutation reverseMutation = initialRevision.getReverseMutation();
        ExperimentMutationHandler<Mutation, ?> handler = mutationHandlerRegistry.findHandler(reverseMutation);
        handler.handle(experiment, model, reverseMutation, null, context);
        return new MutationResult("Undo: " + initialRevision.getSummary());
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.Redo.class)
class RedoHandler extends AbstractExperimentMutationHandler<ExperimentMutation.Redo> {

    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    @Override
    protected boolean isAffectsAttachments() {
        return true; // !!!
    }

    @Override
    protected boolean isAffectsACL() {
        return true; // !!!
    }

    @Override
    protected boolean isAffectsModel() {
        return true; // !!!
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, @Nullable ExperimentModel model, ExperimentMutation.Redo mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        ExperimentRevisionEntity initialRevision = experimentRepository.getRevision(experiment, mutation.revision());
        // !!! verify revision was undone
        Mutation initialMutation = initialRevision.getMutation();
        ExperimentMutationHandler<Mutation, MutationRedoInfo> handler = mutationHandlerRegistry.findHandler(initialMutation);
        handler.handle(experiment, model, initialMutation, initialRevision.getRedoInfo(), context);
        return new MutationResult("Redo: " + initialRevision.getSummary());
    }
}
