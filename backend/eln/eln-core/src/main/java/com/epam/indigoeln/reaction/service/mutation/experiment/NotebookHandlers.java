package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.MutationNotUndoableException;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.NotebookRevisionEntity;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutationContext;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

abstract class AbstractNotebookMutationHandler<T extends NotebookMutation> implements NotebookMutationHandler<T, MutationRedoInfo> {

    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    public void initContext(NotebookEntity notebook, T mutation, NotebookMutationContext context) {
        // nothing
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.CreateNotebook.class)
class CreateNotebookHandler extends AbstractNotebookMutationHandler<NotebookMutation.CreateNotebook> {

    @Inject
    NotebookMapper notebookMapper;
    @Inject
    NotebookRepository notebookRepository;
    @Inject
    ACLService aclService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    UserService userService;

    @Override
    public MutationResult handle(NotebookEntity notebook, NotebookMutation.CreateNotebook mutation, @Nullable MutationRedoInfo redoInfo, NotebookMutationContext context) {
        notebook.setName(mutation.name());
        notebook.setDescription(mutation.description());
        notebook.setRevision(0);
        notebook.setCreatedBy(userService.getCurrentUserEntity());
        aclService.initNotebookACL(notebook);
        return new MutationResult("Notebook created");
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.EditNotebookAttributes.class)
class EditNotebookAttributesHandler extends AbstractNotebookMutationHandler<NotebookMutation.EditNotebookAttributes> {

    @Override
    public MutationResult handle(NotebookEntity notebook, NotebookMutation.EditNotebookAttributes mutation, @Nullable MutationRedoInfo redoInfo, NotebookMutationContext context) {
        List<String> summaryList = new ArrayList<>();
        editProperty(mutation.name(), notebook::setName, summaryList, "name");
        editProperty(mutation.description(), notebook::setDescription, summaryList, "description");
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList));
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.EditNotebookAccess.class)
class EditNotebookAccessHandler extends AbstractNotebookMutationHandler<NotebookMutation.EditNotebookAccess> {

    @Inject
    ACLService aclService;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    UserService userService;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    public MutationResult handle(NotebookEntity notebook, NotebookMutation.EditNotebookAccess mutation, @Nullable MutationRedoInfo redoInfo, NotebookMutationContext context) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(notebook.getProject());
        aclService.updateNotebookACL(notebook.getProject(), notebook, mutation.edits());
        // !!! create revisions for project/experiment, if they are affected
        return new MutationResult(summary);
    }

    @Override
    public void initContext(NotebookEntity notebook, NotebookMutation.EditNotebookAccess mutation, NotebookMutationContext context) {
        context.setAffectsACL(true);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.CreateNotebookAttachment.class)
class CreateNotebookAttachmentHandler extends AbstractNotebookMutationHandler<NotebookMutation.CreateNotebookAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public MutationResult handle(NotebookEntity notebook, NotebookMutation.CreateNotebookAttachment mutation, @Nullable MutationRedoInfo redoInfo, NotebookMutationContext context) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        notebook.getAttachments().add(attachment);
        attachment.getNotebooks().add(notebook);
        return new MutationResult(entityMutationHelper.formatCreateAttachmentSummary(attachment));
    }

    @Override
    public void initContext(NotebookEntity notebook, NotebookMutation.CreateNotebookAttachment mutation, NotebookMutationContext context) {
        context.setAffectsAttachments(true);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.DeleteNotebookAttachment.class)
class DeleteNotebookAttachmentHandler extends AbstractNotebookMutationHandler<NotebookMutation.DeleteNotebookAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public MutationResult handle(NotebookEntity notebook, NotebookMutation.DeleteNotebookAttachment mutation, @Nullable MutationRedoInfo redoInfo, NotebookMutationContext context) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        notebook.getAttachments().remove(attachment);
        attachment.getNotebooks().remove(notebook);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName());
    }

    @Override
    public void initContext(NotebookEntity notebook, NotebookMutation.DeleteNotebookAttachment mutation, NotebookMutationContext context) {
        context.setAffectsAttachments(true);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.NotebookUndo.class)
class NotebookUndoHandler extends AbstractNotebookMutationHandler<NotebookMutation.NotebookUndo> {

    @Inject
    NotebookRepository notebookRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    @Override
    public MutationResult handle(NotebookEntity notebook, NotebookMutation.NotebookUndo mutation, @Nullable MutationRedoInfo redoInfo, NotebookMutationContext context) {
        NotebookRevisionEntity initialRevision = notebookRepository.getRevision(notebook, mutation.revision());
        if (initialRevision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(initialRevision.getSummary()));
        }
        Mutation reverseMutation = initialRevision.getReverseMutation();
        mutationHandlerRegistry.<NotebookMutationHandler<Mutation, ?>>findHandler(reverseMutation).handle(notebook, reverseMutation, null, context);
        return new MutationResult("Undo: " + initialRevision.getSummary());
    }

    @Override
    public void initContext(NotebookEntity notebook, NotebookMutation.NotebookUndo mutation, NotebookMutationContext context) {
        NotebookRevisionEntity initialRevision = notebookRepository.getRevision(notebook, mutation.revision());
        if (initialRevision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(initialRevision.getSummary()));
        }
        Mutation reverseMutation = initialRevision.getReverseMutation();
        mutationHandlerRegistry.<NotebookMutationHandler<Mutation, ?>>findHandler(reverseMutation).initContext(notebook, reverseMutation, context);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.NotebookRedo.class)
class NotebookRedoHandler extends AbstractNotebookMutationHandler<NotebookMutation.NotebookRedo> {

    @Inject
    NotebookRepository notebookRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    @Override
    public MutationResult handle(NotebookEntity notebook, NotebookMutation.NotebookRedo mutation, @Nullable MutationRedoInfo redoInfo, NotebookMutationContext context) {
        NotebookRevisionEntity initialRevision = notebookRepository.getRevision(notebook, mutation.revision());
        // !!! verify revision was undone
        Mutation initialMutation = initialRevision.getMutation();
        mutationHandlerRegistry.<NotebookMutationHandler<Mutation, MutationRedoInfo>>findHandler(initialMutation).handle(notebook, initialMutation, initialRevision.getRedoInfo(), context);
        return new MutationResult("Redo: " + initialRevision.getSummary());
    }

    @Override
    public void initContext(NotebookEntity notebook, NotebookMutation.NotebookRedo mutation, NotebookMutationContext context) {
        NotebookRevisionEntity initialRevision = notebookRepository.getRevision(notebook, mutation.revision());
        // !!! verify revision was undone
        Mutation initialMutation = initialRevision.getMutation();
        mutationHandlerRegistry.<NotebookMutationHandler<Mutation, ?>>findHandler(initialMutation).initContext(notebook, initialMutation, context);
    }
}
