package com.epam.indigoeln.reaction.service.mutation.project;

import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.service.mutation.EntityMutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

@Dependent
@MutationHandlerFor(ProjectMutation.CreateProject.class)
class CreateProjectHandler extends AbstractProjectMutationHandler<ProjectMutation.CreateProject> {

    @Override
    protected void doValidateAccess(ProjectEntity project) {
        aclService.ensureTopLevelAccess(ApplicationPermission.CREATE_PROJECTS);
    }

    @Override
    public String doHandle(ProjectEntity project, ProjectMutation.CreateProject mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        project.setName(mutation.name());
        project.setLiterature(mutation.literature());
        project.setDescription(mutation.description());
        if (mutation.keywords() != null) {
            project.getKeywords().addAll(mutation.keywords());
        }
        project.setRevision(0);
        project.setCreatedBy(userService.getCurrentUserEntity());
        aclService.initProjectACL(project);
        return "Create project";
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.EditProjectAttributes.class)
class EditProjectAttributesHandler extends AbstractProjectMutationHandler<ProjectMutation.EditProjectAttributes> {

    @Override
    public String doHandle(ProjectEntity project, ProjectMutation.EditProjectAttributes mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        List<String> summaryList = new ArrayList<>();
        boolean updated = editProperty(
                mutation.name(),
                project::setName,
                summaryList,
                "name"
        );
        updated |= editProperty(
                mutation.keywords(),
                v -> project.setKeywords(new ArrayList<>(v)),
                summaryList,
                "keywords"
        );
        updated |= editProperty(
                mutation.literature(),
                project::setLiterature,
                summaryList,
                x -> "literature"
        );
        updated |= editProperty(
                mutation.description(),
                project::setDescription,
                summaryList,
                x -> "description"
        );
        validate(updated, "Nothing to update");
        return entityMutationHelper.formatEditAttributesSummary(summaryList);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.EditProjectAccess.class)
class EditProjectAccessHandler extends AbstractProjectMutationHandler<ProjectMutation.EditProjectAccess> {

    @Inject
    ACLService aclService;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    protected void doValidateAccess(ProjectEntity project) {
        aclService.ensureAccess(project, ApplicationPermission.MANAGE_PROJECT_ACCESS);
    }

    @Override
    public String doHandle(ProjectEntity project, ProjectMutation.EditProjectAccess mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(project);
        aclService.updateProjectACL(project, mutation.edits());
        return summary;
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.CreateProjectAttachment.class)
class CreateProjectAttachmentHandler extends AbstractProjectMutationHandler<ProjectMutation.CreateProjectAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;
    @Inject
    AttachmentService attachmentService;

    @Override
    public String doHandle(ProjectEntity project, ProjectMutation.CreateProjectAttachment mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddProjectAttachment(project, attachment);
        return entityMutationHelper.formatCreateAttachmentSummary(attachment);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.DeleteProjectAttachment.class)
class DeleteProjectAttachmentHandler extends AbstractProjectMutationHandler<ProjectMutation.DeleteProjectAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public String doHandle(ProjectEntity project, ProjectMutation.DeleteProjectAttachment mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        project.getAttachments().remove(attachment);
        attachment.setProject(null);
        attachment.setDeleted(true);
        return "Deleted attachment: " + attachment.getName();
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectAccessUpdated.class)
class ProjectAccessUpdatedHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectAccessUpdated> {

    @Override
    public String doHandle(ProjectEntity project, ProjectMutation.ProjectAccessUpdated mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        aclService.recalculateACL(project);
        String reason = mutation.notebookName() != null ? "notebook " + mutation.notebookName() : "experiment " + mutation.experimentName();
        return "Access updated because of the changes in " + reason;
    }
}
