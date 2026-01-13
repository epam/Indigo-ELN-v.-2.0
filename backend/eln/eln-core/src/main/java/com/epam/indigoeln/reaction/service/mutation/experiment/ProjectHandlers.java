package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.MutationNotUndoableException;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.ProjectRevisionEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutationContext;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

abstract class AbstractProjectMutationHandler<T extends ProjectMutation> implements ProjectMutationHandler<T, MutationRedoInfo> {

    @Inject
    EntityMutationHelper entityMutationHelper;
    @Inject
    UserService userService;

    @Override
    public void initContext(ProjectEntity project, T mutation, ProjectMutationContext context) {
        // nothing
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.CreateProject.class)
class CreateProjectHandler extends AbstractProjectMutationHandler<ProjectMutation.CreateProject> {

    @Inject
    ProjectMapper projectMapper;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    ACLService aclService;
    @Inject
    DictionaryService dictionaryService;

    @Override
    public MutationResult handle(ProjectEntity project, ProjectMutation.CreateProject mutation, @Nullable MutationRedoInfo redoInfo, ProjectMutationContext context) {
        project.setName(mutation.name());
        project.setLiterature(mutation.literature());
        project.setDescription(mutation.description());
        if (mutation.keywords() != null && !mutation.keywords().isEmpty()) {
            project.setKeywords(dictionaryService.findOrCreateByNames(BuiltInDictionary.PROJECT_KEYWORD.name(), mutation.keywords()));
        }
        project.setRevision(0);
        project.setCreatedBy(userService.getCurrentUserEntity());
        aclService.initProjectACL(project);
        return new MutationResult("Project created");
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.EditProjectAttributes.class)
class EditProjectAttributesHandler extends AbstractProjectMutationHandler<ProjectMutation.EditProjectAttributes> {

    @Inject
    DictionaryService dictionaryService;

    @Override
    public MutationResult handle(ProjectEntity project, ProjectMutation.EditProjectAttributes mutation, @Nullable MutationRedoInfo redoInfo, ProjectMutationContext context) {
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
    public MutationResult handle(ProjectEntity project, ProjectMutation.EditProjectAccess mutation, @Nullable MutationRedoInfo redoInfo, ProjectMutationContext context) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(project);
        aclService.updateProjectACL(project, mutation.edits());
        // !!! create revisions for notebook/experiment, if they are affected
        return new MutationResult(summary);
    }

    @Override
    public void initContext(ProjectEntity project, ProjectMutation.EditProjectAccess mutation, ProjectMutationContext context) {
        context.setAffectsACL(true);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.CreateProjectAttachment.class)
class CreateProjectAttachmentHandler extends AbstractProjectMutationHandler<ProjectMutation.CreateProjectAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public MutationResult handle(ProjectEntity project, ProjectMutation.CreateProjectAttachment mutation, @Nullable MutationRedoInfo redoInfo, ProjectMutationContext context) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        project.getAttachments().add(attachment);
        attachment.getProjects().add(project);
        return new MutationResult(entityMutationHelper.formatCreateAttachmentSummary(attachment));
    }

    @Override
    public void initContext(ProjectEntity project, ProjectMutation.CreateProjectAttachment mutation, ProjectMutationContext context) {
        context.setAffectsAttachments(true);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.DeleteProjectAttachment.class)
class DeleteProjectAttachmentHandler extends AbstractProjectMutationHandler<ProjectMutation.DeleteProjectAttachment> {

    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public MutationResult handle(ProjectEntity project, ProjectMutation.DeleteProjectAttachment mutation, @Nullable MutationRedoInfo redoInfo, ProjectMutationContext context) {
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        project.getAttachments().remove(attachment);
        attachment.getProjects().remove(project);
        attachment.setDeleted(true);
        return new MutationResult("Deleted attachment: " + attachment.getName());
    }

    @Override
    public void initContext(ProjectEntity project, ProjectMutation.DeleteProjectAttachment mutation, ProjectMutationContext context) {
        context.setAffectsAttachments(true);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectUndo.class)
class ProjectUndoHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectUndo> {

    @Inject
    ProjectRepository projectRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    @Override
    public MutationResult handle(ProjectEntity project, ProjectMutation.ProjectUndo mutation, @Nullable MutationRedoInfo redoInfo, ProjectMutationContext context) {
        ProjectRevisionEntity initialRevision = projectRepository.getRevision(project, mutation.revision());
        if (initialRevision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(initialRevision.getSummary()));
        }
        Mutation reverseMutation = initialRevision.getReverseMutation();
        mutationHandlerRegistry.<ProjectMutationHandler<Mutation, ?>>findHandler(reverseMutation).handle(project, reverseMutation, null, context);
        return new MutationResult("Undo: " + initialRevision.getSummary());
    }

    @Override
    public void initContext(ProjectEntity project, ProjectMutation.ProjectUndo mutation, ProjectMutationContext context) {
        ProjectRevisionEntity initialRevision = projectRepository.getRevision(project, mutation.revision());
        if (initialRevision.getReverseMutation() == null) {
            throw new MutationNotUndoableException("Cannot undo '%s'".formatted(initialRevision.getSummary()));
        }
        Mutation reverseMutation = initialRevision.getReverseMutation();
        mutationHandlerRegistry.<ProjectMutationHandler<Mutation, ?>>findHandler(reverseMutation).initContext(project, reverseMutation, context);
    }
}

@Dependent
@MutationHandlerFor(ProjectMutation.ProjectRedo.class)
class ProjectRedoHandler extends AbstractProjectMutationHandler<ProjectMutation.ProjectRedo> {

    @Inject
    ProjectRepository projectRepository;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    @Override
    public MutationResult handle(ProjectEntity project, ProjectMutation.ProjectRedo mutation, @Nullable MutationRedoInfo redoInfo, ProjectMutationContext context) {
        ProjectRevisionEntity initialRevision = projectRepository.getRevision(project, mutation.revision());
        // !!! verify revision was undone
        Mutation initialMutation = initialRevision.getMutation();
        mutationHandlerRegistry.<ProjectMutationHandler<Mutation, MutationRedoInfo>>findHandler(initialMutation).handle(project, initialMutation, initialRevision.getRedoInfo(), context);
        return new MutationResult("Redo: " + initialRevision.getSummary());
    }

    @Override
    public void initContext(ProjectEntity project, ProjectMutation.ProjectRedo mutation, ProjectMutationContext context) {
        ProjectRevisionEntity initialRevision = projectRepository.getRevision(project, mutation.revision());
        // !!! verify revision was undone
        Mutation initialMutation = initialRevision.getMutation();
        mutationHandlerRegistry.<ProjectMutationHandler<Mutation, ?>>findHandler(initialMutation).initContext(project, initialMutation, context);
    }
}
