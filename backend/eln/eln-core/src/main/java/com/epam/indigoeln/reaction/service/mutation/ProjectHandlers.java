package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.MutationNotUndoableException;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.ProjectRevisionEntity;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

@Dependent
@MutationHandlerFor(ProjectMutation.CreateProject.class)
class CreateProjectHandler extends AbstractProjectMutationHandler<ProjectMutation.CreateProject> {

    @Inject
    DictionaryService dictionaryService;

    @Override
    protected void doValidateAccess(ProjectEntity project, ProjectMutation.CreateProject mutation) {
        aclService.ensureTopLevelAccess(ApplicationPermission.CREATE_PROJECTS);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.CreateProject mutation) {
        project.setName(mutation.name());
        project.setLiterature(mutation.literature());
        project.setDescription(mutation.description());
        if (mutation.keywords() != null && !mutation.keywords().isEmpty()) {
            project.setKeywords(dictionaryService.findOrCreateByNames(BuiltInDictionary.PROJECT_KEYWORD.name(), mutation.keywords()));
        }
        project.setRevision(0);
        project.setCreatedBy(userService.getCurrentUserEntity());
        aclService.initProjectACL(project);
        return new MutationResult("Create project", null);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.EditProjectAttributes.class)
class EditProjectAttributesHandler extends AbstractProjectMutationHandler<ProjectMutation.EditProjectAttributes> {

    @Inject
    DictionaryService dictionaryService;

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.EditProjectAttributes mutation) {
        List<String> summaryList = new ArrayList<>();
        editProperty(mutation.name(), project::setName, summaryList, "name");
        editProperty(mutation.keywords(), v -> {
            project.setKeywords(dictionaryService.findOrCreateByNames(BuiltInDictionary.PROJECT_KEYWORD.name(), v));
        }, summaryList, "keywords");
        editProperty(mutation.literature(), project::setLiterature, summaryList, "literature");
        editProperty(mutation.description(), project::setDescription, summaryList, "description");
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList), null);
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
    UserService userService;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    public boolean isAffectsACL() {
        return true;
    }

    @Override
    protected void doValidateAccess(ProjectEntity project, ProjectMutation.EditProjectAccess mutation) {
        aclService.ensureAccess(project, ApplicationPermission.MANAGE_PROJECT_ACCESS);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.EditProjectAccess mutation) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(project);
        aclService.updateProjectACL(project, mutation.edits());
        // !!! create revisions for notebook/experiment, if they are affected
        return new MutationResult(summary, null);
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
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.CreateProjectAttachment mutation) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddProjectAttachment(project, attachment);
        return new MutationResult(entityMutationHelper.formatCreateAttachmentSummary(attachment), null);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.DeleteProjectAttachment.class)
class DeleteProjectAttachmentHandler extends AbstractProjectMutationHandler<ProjectMutation.DeleteProjectAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.DeleteProjectAttachment mutation) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        project.getAttachments().remove(attachment);
        attachment.getProjects().remove(project);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName(), null);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectAccessUpdated.class)
class ProjectAccessUpdatedHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectAccessUpdated> {

    @Override
    public boolean isAffectsACL() {
        return true;
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.ProjectAccessUpdated mutation) {
        aclService.recalculateACL(project);
        String reason = mutation.notebookName() != null ? "notebook " + mutation.notebookName() : "experiment " + mutation.experimentName();
        return new MutationResult("Access updated because of the changes in " + reason, null);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectUndo.class)
class ProjectUndoHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectUndo> {

    @Inject
    ProjectRepository projectRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    ProjectRevisionEntity initialRevision;
    Mutation reverseMutation;
    AbstractProjectMutationHandler<Mutation> reverseHandler;

    @Override
    public boolean isAffectsAttachments() {
        return reverseHandler.isAffectsAttachments();
    }

    @Override
    public boolean isAffectsACL() {
        return reverseHandler.isAffectsACL();
    }

    @Override
    protected void doValidateAccess(ProjectEntity project, ProjectMutation.ProjectUndo mutation) {
        reverseHandler.doValidateAccess(project, mutation);
    }

    @Override
    protected void doPrepare(ProjectEntity project, ProjectMutation.ProjectUndo mutation) {
        initialRevision = projectRepository.getRevision(project, mutation.revision());
        if (initialRevision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(initialRevision.getSummary()));
        }
        reverseMutation = initialRevision.getReverseMutation();
        reverseHandler = mutationHandlerRegistry.findHandler(reverseMutation);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.ProjectUndo mutation) {
        reverseHandler.doHandle(project, model, reverseMutation);
        return new MutationResult("Undo: " + initialRevision.getSummary(), null);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectRedo.class)
class ProjectRedoHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectRedo> {

    @Inject
    ProjectRepository projectRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    ProjectRevisionEntity initialRevision;
    Mutation initialMutation;
    AbstractProjectMutationHandler<Mutation> initialHandler;

    @Override
    public boolean isAffectsAttachments() {
        return initialHandler.isAffectsAttachments();
    }

    @Override
    public boolean isAffectsACL() {
        return initialHandler.isAffectsACL();
    }

    @Override
    protected void doValidateAccess(ProjectEntity project, ProjectMutation.ProjectRedo mutation) {
        initialHandler.doValidateAccess(project, mutation);
    }

    @Override
    protected void doPrepare(ProjectEntity project, ProjectMutation.ProjectRedo mutation) {
        initialRevision = projectRepository.getRevision(project, mutation.revision());
        initialMutation = initialRevision.getMutation();
        initialHandler = mutationHandlerRegistry.findHandler(initialMutation);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.ProjectRedo mutation) {
        // !!! verify revision was undone
        initialHandler.doHandle(project, model, initialMutation);
        return new MutationResult("Redo: " + initialRevision.getSummary(), null);
    }
}
