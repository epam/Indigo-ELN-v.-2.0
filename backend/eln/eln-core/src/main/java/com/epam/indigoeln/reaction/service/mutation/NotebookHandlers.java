package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.MutationNotUndoableException;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.NotebookRevisionEntity;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

@Dependent
@MutationHandlerFor(NotebookMutation.CreateNotebook.class)
class CreateNotebookHandler extends AbstractNotebookMutationHandler<NotebookMutation.CreateNotebook> {

    @Inject
    UserService userService;

    @Override
    protected void doValidateAccess(NotebookEntity notebook, NotebookMutation.CreateNotebook mutation) {
        aclService.ensureAccess(notebook.getProject(), ApplicationPermission.CREATE_NOTEBOOKS);
    }

    @Override
    public MutationResult doHandle(NotebookEntity notebook, @Nullable Void model, NotebookMutation.CreateNotebook mutation) {
        notebook.setName(mutation.name());
        notebook.setDescription(mutation.description());
        notebook.setRevision(0);
        notebook.setCreatedBy(userService.getCurrentUserEntity());
        aclService.initNotebookACL(notebook);
        return new MutationResult("Create notebook", null);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.EditNotebookAttributes.class)
class EditNotebookAttributesHandler extends AbstractNotebookMutationHandler<NotebookMutation.EditNotebookAttributes> {

    @Override
    public MutationResult doHandle(NotebookEntity notebook, @Nullable Void model, NotebookMutation.EditNotebookAttributes mutation) {
        List<String> summaryList = new ArrayList<>();
        editProperty(mutation.name(), notebook::setName, summaryList, "name");
        editProperty(mutation.description(), notebook::setDescription, summaryList, "description");
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList), null);
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
    public boolean isAffectsACL() {
        return true;
    }
    
    @Override
    protected void doValidateAccess(NotebookEntity notebook, NotebookMutation.EditNotebookAccess mutation) {
        aclService.ensureAccess(notebook, ApplicationPermission.MANAGE_NOTEBOOK_ACCESS);
    }

    @Override
    public MutationResult doHandle(NotebookEntity notebook, @Nullable Void model, NotebookMutation.EditNotebookAccess mutation) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(notebook.getProject());
        aclService.updateNotebookACL(notebook.getProject(), notebook, mutation.edits());
        // !!! create revisions for project/experiment, if they are affected
        return new MutationResult(summary, null);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.CreateNotebookAttachment.class)
class CreateNotebookAttachmentHandler extends AbstractNotebookMutationHandler<NotebookMutation.CreateNotebookAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(NotebookEntity notebook, @Nullable Void model, NotebookMutation.CreateNotebookAttachment mutation) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        notebook.getAttachments().add(attachment);
        attachment.getNotebooks().add(notebook);
        return new MutationResult(entityMutationHelper.formatCreateAttachmentSummary(attachment), null);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.DeleteNotebookAttachment.class)
class DeleteNotebookAttachmentHandler extends AbstractNotebookMutationHandler<NotebookMutation.DeleteNotebookAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(NotebookEntity notebook, @Nullable Void model, NotebookMutation.DeleteNotebookAttachment mutation) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        notebook.getAttachments().remove(attachment);
        attachment.getNotebooks().remove(notebook);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName(), null);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.NotebookUndo.class)
class NotebookUndoHandler extends AbstractNotebookMutationHandler<NotebookMutation.NotebookUndo> {

    @Inject
    NotebookRepository notebookRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    NotebookRevisionEntity initialRevision;
    Mutation reverseMutation;
    AbstractNotebookMutationHandler<Mutation> reverseHandler;

    @Override
    public boolean isAffectsAttachments() {
        return reverseHandler.isAffectsAttachments();
    }

    @Override
    public boolean isAffectsACL() {
        return reverseHandler.isAffectsACL();
    }

    @Override
    protected void doValidateAccess(NotebookEntity notebook, NotebookMutation.NotebookUndo mutation) {
        reverseHandler.doValidateAccess(notebook, mutation);
    }

    @Override
    protected void doPrepare(NotebookEntity notebook, NotebookMutation.NotebookUndo mutation) {
        initialRevision = notebookRepository.getRevision(notebook, mutation.revision());
        if (initialRevision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(initialRevision.getSummary()));
        }
        reverseMutation = initialRevision.getReverseMutation();
        reverseHandler = mutationHandlerRegistry.findHandler(reverseMutation);
    }

    @Override
    public MutationResult doHandle(NotebookEntity notebook, @Nullable Void model, NotebookMutation.NotebookUndo mutation) {
        reverseHandler.doHandle(notebook, model, reverseMutation);
        return new MutationResult("Undo: " + initialRevision.getSummary(), null);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.NotebookRedo.class)
class NotebookRedoHandler extends AbstractNotebookMutationHandler<NotebookMutation.NotebookRedo> {

    @Inject
    NotebookRepository notebookRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    NotebookRevisionEntity initialRevision;
    Mutation initialMutation;
    AbstractNotebookMutationHandler<Mutation> initialHandler;

    @Override
    public boolean isAffectsAttachments() {
        return initialHandler.isAffectsAttachments();
    }

    @Override
    public boolean isAffectsACL() {
        return initialHandler.isAffectsACL();
    }

    @Override
    protected void doValidateAccess(NotebookEntity notebook, NotebookMutation.NotebookRedo mutation) {
        initialHandler.doValidateAccess(notebook, mutation);
    }

    @Override
    protected void doPrepare(NotebookEntity notebook, NotebookMutation.NotebookRedo mutation) {
        initialRevision = notebookRepository.getRevision(notebook, mutation.revision());
        initialMutation = initialRevision.getMutation();
        initialHandler = mutationHandlerRegistry.findHandler(initialMutation);
    }

    @Override
    public MutationResult doHandle(NotebookEntity notebook, @Nullable Void model, NotebookMutation.NotebookRedo mutation) {
        // !!! verify revision was undone
        initialHandler.doHandle(notebook, model, initialMutation);
        return new MutationResult("Redo: " + initialRevision.getSummary(), null);
    }
}
