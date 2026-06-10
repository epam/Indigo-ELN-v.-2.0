package com.epam.indigoeln.reaction.service.mutation.project;

import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.DictionaryUpdateService;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.service.mutation.EntityMutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.common.util.ModelUtil.updateCollection;

@Dependent
@MutationHandlerFor(ProjectMutation.CreateProject.class)
class CreateProjectHandler extends AbstractProjectMutationHandler<ProjectMutation.CreateProject> {

    @Inject
    DictionaryUpdateService dictionaryUpdateService;

    @Override
    protected void doValidateAccess(ProjectEntity project, ProjectMutation.CreateProject mutation, ProjectMutationContext context) {
        aclService.ensureTopLevelAccess(ApplicationPermission.CREATE_PROJECTS);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, ProjectMutation.CreateProject mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        project.setName(mutation.name());
        project.setLiterature(mutation.literature());
        project.setDescription(mutation.description());
        if (mutation.keywords() != null && !mutation.keywords().isEmpty()) {
            updateCollection(project.getKeywords(), dictionaryUpdateService.findOrCreateByNames(BuiltInDictionary.PROJECT_KEYWORD.name(), mutation.keywords()));
        }
        project.setRevision(0);
        project.setCreatedBy(userService.getCurrentUserEntity());
        aclService.initProjectACL(project);
        return new MutationResult("Create project");
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.EditProjectAttributes.class)
class EditProjectAttributesHandler extends AbstractProjectMutationHandler<ProjectMutation.EditProjectAttributes> {

    @Inject
    DictionaryUpdateService dictionaryUpdateService;

    @Override
    public MutationResult doHandle(ProjectEntity project, ProjectMutation.EditProjectAttributes mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        List<String> summaryList = new ArrayList<>();
        boolean updated = editProperty(mutation.name(), project::setName, summaryList, "name");
        updated |= editProperty(mutation.keywords(), v -> {
            updateCollection(project.getKeywords(), dictionaryUpdateService.findOrCreateByNames(BuiltInDictionary.PROJECT_KEYWORD.name(), v));
        }, summaryList, "keywords");
        updated |= editProperty(mutation.literature(), project::setLiterature, summaryList, "literature");
        updated |= editProperty(mutation.description(), project::setDescription, summaryList, "description");
        validate(updated, "Nothing to update");
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList));
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
    public void doPrepare(ProjectEntity entity, ProjectMutation.EditProjectAccess mutation, ProjectMutationContext context) {
        context.setAffectsACL(true);
    }

    @Override
    protected void doValidateAccess(ProjectEntity project, ProjectMutation.EditProjectAccess mutation, ProjectMutationContext context) {
        aclService.ensureAccess(project, ApplicationPermission.MANAGE_PROJECT_ACCESS);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, ProjectMutation.EditProjectAccess mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(project);
        aclService.updateProjectACL(project, mutation.edits());
        // !!! create revisions for notebook/experiment, if they are affected
        return new MutationResult(summary);
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
    public void doPrepare(ProjectEntity entity, ProjectMutation.CreateProjectAttachment mutation, ProjectMutationContext context) {
        context.setAffectsAttachments(true);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, ProjectMutation.CreateProjectAttachment mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddProjectAttachment(project, attachment);
        return new MutationResult(entityMutationHelper.formatCreateAttachmentSummary(attachment));
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.DeleteProjectAttachment.class)
class DeleteProjectAttachmentHandler extends AbstractProjectMutationHandler<ProjectMutation.DeleteProjectAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public void doPrepare(ProjectEntity entity, ProjectMutation.DeleteProjectAttachment mutation, ProjectMutationContext context) {
        context.setAffectsAttachments(true);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, ProjectMutation.DeleteProjectAttachment mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        project.getAttachments().remove(attachment);
        attachment.getProjects().remove(project);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName());
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectAccessUpdated.class)
class ProjectAccessUpdatedHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectAccessUpdated> {

    @Override
    public void doPrepare(ProjectEntity entity, ProjectMutation.ProjectAccessUpdated mutation, ProjectMutationContext context) {
        context.setAffectsACL(true);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, ProjectMutation.ProjectAccessUpdated mutation, ProjectMutationContext context, ProjectSnapshot snapshotBefore) {
        aclService.recalculateACL(project);
        String reason = mutation.notebookName() != null ? "notebook " + mutation.notebookName() : "experiment " + mutation.experimentName();
        return new MutationResult("Access updated because of the changes in " + reason);
    }
}
