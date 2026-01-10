package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.exception.MutationNotUndoableException;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.AccessLevel;
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

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.CreateExperiment mutation, MutationContext context) {
        // nothing
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.EditExperimentAttributes.class)
class EditExperimentAttributesHandler extends AbstractExperimentMutationHandler<ExperimentMutation.EditExperimentAttributes> {

    @Inject
    DictionaryService dictionaryService;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.EditExperimentAttributes mutation, MutationRedoInfo redoInfo, MutationContext context) {
        List<String> attributeSummaries = new ArrayList<>();
        editProperty(mutation.therapeuticArea(), v -> {
            DictionaryItemEntity value = dictionaryService.lookup(BuiltInDictionary.THERAPEUTIC_AREA.name(), v);
            experiment.setTherapeuticArea(value);
            attributeSummaries.add(value != null ? "set therapeutic area = " + value.getName() : "reset therapeutic area");
        });
        editProperty(mutation.projectCode(), v -> {
            DictionaryItemEntity value = dictionaryService.lookup(BuiltInDictionary.PROJECT_CODE.name(), v);
            experiment.setProjectCode(value);
            attributeSummaries.add(value != null ? "set project code = " + value.getName() : "reset project code");
        });
        String summary = switch (attributeSummaries.size()) {
            case 0 -> throw new InvalidRequestException("No attributes to update");
            case 1 -> "Edited attribute: " + attributeSummaries.getFirst();
            case 2 -> "Edited attributes: " + attributeSummaries.get(0) + ", " + attributeSummaries.get(1);
            default -> "Edited multiple attributes";
        };
        return new MutationResult(summary);
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.EditExperimentAttributes mutation, MutationContext context) {
        // nothing
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

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.EditExperimentAccess mutation, MutationRedoInfo redoInfo, MutationContext context) {
        String summary = switch (mutation.edits().size()) {
            case 0 -> throw new InvalidRequestException("No access edits to apply");
            case 1 -> {
                AccessForm update = mutation.edits().getFirst();
                UserEntity user = userService.getUserEntity(update.getUserID());
                if (update.getLevel() == AccessLevel.NONE) {
                    yield "Edited Team: removed " + user.getUsername();
                } else {
                    yield "Edited Team: granted " + user.getUsername() + " " + update.getLevel() + " access";
                }
            }
            default -> "Edited Team: updated multiple users";
        };
        projectRepository.lockProject(experiment.getProject());
        aclService.updateExperimentACL(experiment.getNotebook().getProject(), experiment.getNotebook(), experiment, mutation.edits());
        // !!! create revisions for notebook/project, if they are affected
        return new MutationResult(summary);
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.EditExperimentAccess mutation, MutationContext context) {
        context.setAffectsACL(true);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.CreateExperimentAttachment.class)
class CreateExperimentAttachmentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.CreateExperimentAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.CreateExperimentAttachment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().add(attachment);
        attachment.getExperiments().add(experiment);
        return new MutationResult("Created attachment: %s, %d bytes".formatted(attachment.getName(), attachment.getSize()));
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.CreateExperimentAttachment mutation, MutationContext context) {
        context.setAffectsAttachments(true);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.DeleteExperimentAttachment.class)
class DeleteExperimentAttachmentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.DeleteExperimentAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.DeleteExperimentAttachment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().remove(attachment);
        attachment.getExperiments().remove(experiment);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName());
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.DeleteExperimentAttachment mutation, MutationContext context) {
        context.setAffectsAttachments(true);
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
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.Undo mutation, MutationRedoInfo redoInfo, MutationContext context) {
        ExperimentRevisionEntity initialRevision = getRevision(experiment, mutation);
        Mutation reverseMutation = initialRevision.getReverseMutation();
        //noinspection unchecked
        mutationHandlerRegistry.findHandler(reverseMutation).handle(experiment, model, reverseMutation, null, context);
        return new MutationResult("Undo: " + initialRevision.getSummary());
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.Undo mutation, MutationContext context) {
        ExperimentRevisionEntity initialRevision = getRevision(experiment, mutation);
        Mutation reverseMutation = initialRevision.getReverseMutation();
        mutationHandlerRegistry.findHandler(reverseMutation).initContext(experiment, reverseMutation, context);
    }

    private ExperimentRevisionEntity getRevision(ExperimentEntity experiment, ExperimentMutation.Undo undoMutation) {
        ExperimentRevisionEntity revision = experimentRepository.getRevision(experiment, undoMutation.revision());
        if (revision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(revision.getSummary()));
        }
        return revision;
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
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.Redo mutation, MutationRedoInfo redoInfo, MutationContext context) {
        ExperimentRevisionEntity initialRevision = getRevision(experiment, mutation);
        Mutation initialMutation = initialRevision.getMutation();
        mutationHandlerRegistry.findHandler(initialMutation).handle(experiment, model, initialMutation, initialRevision.getRedoInfo(), context);
        return new MutationResult("Redo: " + initialRevision.getSummary());
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.Redo mutation, MutationContext context) {
        ExperimentRevisionEntity initialRevision = getRevision(experiment, mutation);
        Mutation initialMutation = initialRevision.getMutation();
        mutationHandlerRegistry.findHandler(initialMutation).initContext(experiment, initialMutation, context);
    }

    private ExperimentRevisionEntity getRevision(ExperimentEntity experiment, ExperimentMutation.Redo redoMutation) {
        ExperimentRevisionEntity revision = experimentRepository.getRevision(experiment, redoMutation.revision());
        // !!! verify revision was undone
        return revision;
    }
}
