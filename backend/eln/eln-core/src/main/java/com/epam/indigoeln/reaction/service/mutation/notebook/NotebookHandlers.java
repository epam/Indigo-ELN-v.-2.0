package com.epam.indigoeln.reaction.service.mutation.notebook;

import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import com.epam.indigoeln.reaction.service.mutation.EntityMutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

@Dependent
@MutationHandlerFor(NotebookMutation.CreateNotebook.class)
class CreateNotebookHandler extends AbstractNotebookMutationHandler<NotebookMutation.CreateNotebook> {

    @Inject
    UserService userService;

    @Override
    protected void doValidateAccess(NotebookEntity notebook, NotebookMutation.CreateNotebook mutation, NotebookMutationContext context) {
        aclService.ensureAccess(notebook.getProject(), ApplicationPermission.CREATE_NOTEBOOKS);
    }

    @Override
    public MutationResult doHandle(NotebookEntity notebook, NotebookMutation.CreateNotebook mutation, NotebookMutationContext context, NotebookSnapshot snapshotBefore) {
        notebook.setName(mutation.name());
        notebook.setDescription(mutation.description());
        notebook.setRevision(0);
        notebook.setCreatedBy(userService.getCurrentUserEntity());
        aclService.initNotebookACL(notebook);
        return new MutationResult("Create notebook");
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.EditNotebookAttributes.class)
class EditNotebookAttributesHandler extends AbstractNotebookMutationHandler<NotebookMutation.EditNotebookAttributes> {

    @Override
    public MutationResult doHandle(NotebookEntity notebook, NotebookMutation.EditNotebookAttributes mutation, NotebookMutationContext context, NotebookSnapshot snapshotBefore) {
        List<String> summaryList = new ArrayList<>();
        boolean updated = editProperty(mutation.name(), notebook::setName, summaryList, "name");
        updated |= editProperty(mutation.description(), notebook::setDescription, summaryList, "description");
        validate(updated, "Nothing to update");
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
    EntityMutationHelper entityMutationHelper;

    @Override
    protected void doValidateAccess(NotebookEntity notebook, NotebookMutation.EditNotebookAccess mutation, NotebookMutationContext context) {
        aclService.ensureAccess(notebook, ApplicationPermission.MANAGE_NOTEBOOK_ACCESS);
    }

    @Override
    public MutationResult doHandle(NotebookEntity notebook, NotebookMutation.EditNotebookAccess mutation, NotebookMutationContext context, NotebookSnapshot snapshotBefore) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(notebook.getProject());
        aclService.updateNotebookACL(notebook.getProject(), notebook, mutation.edits());
        // !!! create revisions for project/experiment, if they are affected
        return new MutationResult(summary);
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.CreateNotebookAttachment.class)
class CreateNotebookAttachmentHandler extends AbstractNotebookMutationHandler<NotebookMutation.CreateNotebookAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;
    @Inject
    AttachmentService attachmentService;

    @Override
    public MutationResult doHandle(NotebookEntity notebook, NotebookMutation.CreateNotebookAttachment mutation, NotebookMutationContext context, NotebookSnapshot snapshotBefore) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddNotebookAttachment(notebook, attachment);
        return new MutationResult(entityMutationHelper.formatCreateAttachmentSummary(attachment));
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.DeleteNotebookAttachment.class)
class DeleteNotebookAttachmentHandler extends AbstractNotebookMutationHandler<NotebookMutation.DeleteNotebookAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public MutationResult doHandle(NotebookEntity notebook, NotebookMutation.DeleteNotebookAttachment mutation, NotebookMutationContext context, NotebookSnapshot snapshotBefore) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        notebook.getAttachments().remove(attachment);
        attachment.getNotebooks().remove(notebook);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName());
    }
}

@Dependent
@MutationHandlerFor(NotebookMutation.NotebookAccessUpdated.class)
class NotebookAccessUpdatedHandler extends AbstractNotebookMutationHandler<NotebookMutation.NotebookAccessUpdated> {

    @Override
    public MutationResult doHandle(NotebookEntity notebook, NotebookMutation.NotebookAccessUpdated mutation, NotebookMutationContext context, NotebookSnapshot snapshotBefore) {
        aclService.recalculateACL(notebook);
        String reason = mutation.projectName() != null ? "project " + mutation.projectName() : "experiment " + mutation.experimentName();
        return new MutationResult("Access updated because of the changes in " + reason);
    }
}
