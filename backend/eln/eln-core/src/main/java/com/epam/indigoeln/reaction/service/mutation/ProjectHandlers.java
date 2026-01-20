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
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

@Dependent
@MutationHandlerFor(ProjectMutation.CreateProject.class)
class CreateProjectHandler extends AbstractProjectMutationHandler<ProjectMutation.CreateProject, MutationRedoInfo> {

    @Inject
    DictionaryService dictionaryService;

    @Override
    protected void doValidateAccess(ProjectEntity project, ProjectMutation.CreateProject mutation) {
        aclService.ensureTopLevelAccess(ApplicationPermission.CREATE_PROJECTS);
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.CreateProject mutation, @Nullable MutationRedoInfo redoInfo) {
        project.setName(mutation.name());
        project.setLiterature(mutation.literature());
        project.setDescription(mutation.description());
        if (mutation.keywords() != null && !mutation.keywords().isEmpty()) {
            project.setKeywords(dictionaryService.findOrCreateByNames(BuiltInDictionary.PROJECT_KEYWORD.name(), mutation.keywords()));
        }
        project.setRevision(0);
        project.setCreatedBy(userService.getCurrentUserEntity());
        aclService.initProjectACL(project);
        return new MutationResult("Create project");
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.EditProjectAttributes.class)
class EditProjectAttributesHandler extends AbstractProjectMutationHandler<ProjectMutation.EditProjectAttributes, MutationRedoInfo> {

    @Inject
    DictionaryService dictionaryService;

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.EditProjectAttributes mutation, @Nullable MutationRedoInfo redoInfo) {
        List<String> summaryList = new ArrayList<>();
        editProperty(mutation.name(), project::setName, summaryList, "name");
        editProperty(mutation.keywords(), v -> {
            project.setKeywords(dictionaryService.findOrCreateByNames(BuiltInDictionary.PROJECT_KEYWORD.name(), v));
        }, summaryList, "keywords");
        editProperty(mutation.literature(), project::setLiterature, summaryList, "literature");
        editProperty(mutation.description(), project::setDescription, summaryList, "description");
        return new MutationResult(entityMutationHelper.formatEditAttributesSummary(summaryList));
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.EditProjectAccess.class)
class EditProjectAccessHandler extends AbstractProjectMutationHandler<ProjectMutation.EditProjectAccess, MutationRedoInfo> {

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
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.EditProjectAccess mutation, @Nullable MutationRedoInfo redoInfo) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(project);
        aclService.updateProjectACL(project, mutation.edits());
        // !!! create revisions for notebook/experiment, if they are affected
        return new MutationResult(summary);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.CreateProjectAttachment.class)
class CreateProjectAttachmentHandler extends AbstractProjectMutationHandler<ProjectMutation.CreateProjectAttachment, MutationRedoInfo> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.CreateProjectAttachment mutation, @Nullable MutationRedoInfo redoInfo) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        project.getAttachments().add(attachment);
        attachment.getProjects().add(project);
        return new MutationResult(entityMutationHelper.formatCreateAttachmentSummary(attachment));
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.DeleteProjectAttachment.class)
class DeleteProjectAttachmentHandler extends AbstractProjectMutationHandler<ProjectMutation.DeleteProjectAttachment, MutationRedoInfo> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public boolean isAffectsAttachments() {
        return true;
    }

    @Override
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.DeleteProjectAttachment mutation, @Nullable MutationRedoInfo redoInfo) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        project.getAttachments().remove(attachment);
        attachment.getProjects().remove(project);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName());
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectUndo.class)
class ProjectUndoHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectUndo, MutationRedoInfo> {

    @Inject
    ProjectRepository projectRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    ProjectRevisionEntity initialRevision;
    Mutation reverseMutation;
    AbstractProjectMutationHandler<Mutation, ?> reverseHandler;

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
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.ProjectUndo mutation, @Nullable MutationRedoInfo redoInfo) {
        reverseHandler.doHandle(project, model, reverseMutation, null);
        return new MutationResult("Undo: " + initialRevision.getSummary());
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectRedo.class)
class ProjectRedoHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectRedo, MutationRedoInfo> {

    @Inject
    ProjectRepository projectRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    ProjectRevisionEntity initialRevision;
    Mutation initialMutation;
    AbstractProjectMutationHandler<Mutation, MutationRedoInfo> initialHandler;

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
    public MutationResult doHandle(ProjectEntity project, @Nullable Void model, ProjectMutation.ProjectRedo mutation, @Nullable MutationRedoInfo redoInfo) {
        // !!! verify revision was undone
        initialHandler.doHandle(project, model, initialMutation, initialRevision.getRedoInfo());
        return new MutationResult("Redo: " + initialRevision.getSummary());
    }
}
