package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutationContext;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ProjectDiffHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.service.mutation.ProjectMutationHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.eln.model.ApplicationPermission.*;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.eln.util.ModelUtil.wrapConstraintViolation;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class ProjectService {

    @Inject
    ProjectMapper projectMapper;
    @Inject
    UserService userService;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    ACLService aclService;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;
    @Inject
    SnapshotMapper snapshotMapper;
    @Inject
    RevisionService revisionService;
    
    public ProjectDetailsDTO createProject(ProjectRequest request) {
        ProjectEntity project = new ProjectEntity();
        return wrapConstraintViolation(() -> {
            aclService.ensureTopLevelAccess(ApplicationPermission.CREATE_PROJECTS);
            applyMutation(project, projectMapper.requestToMutation(request));
            projectRepository.flushAndRefresh(project);
            return getProject(project.getId());
        }, e -> mapConstraintToError(e, project));
    }

    public Page<ProjectDTO> getProjects(@Nullable String search, @Nullable SortOrder sort, @Nullable Boolean createdByMe, Paging paging) {
        UserEntity currentUser = Boolean.TRUE.equals(createdByMe) ? userService.getCurrentUserEntity() : null;
        boolean showAll = userService.getCurrentUser().getPermissions().contains(ApplicationPermission.VIEW_PROJECTS);
        return projectRepository.findAll(search, sort, currentUser, paging, showAll);
    }

    public ProjectDetailsDTO getProject(UUID projectId) {
        ProjectEntity project = projectRepository.loadDetails(projectId);
        Set<ApplicationPermission> currentPermissions = aclService.getCurrentPermissions(project.getCalculatedInfo() != null ? project.getCalculatedInfo().getCurrentAccess() : null);
        currentPermissions.retainAll(EnumSet.of(VIEW_PROJECTS, EDIT_PROJECTS, MANAGE_PROJECT_ACCESS, DELETE_PROJECTS));
        return projectMapper.entityToDetailsDTO(project, currentPermissions);
    }

    public ProjectDetailsDTO editProject(UUID projectId, ProjectEditRequest request) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
        return wrapConstraintViolation(() -> {
            applyMutation(project, projectMapper.requestToMutation(request));
            return getProject(projectId);
        }, e -> mapConstraintToError(e, project));
    }

    public TotalCounts getTotalCounts() {
        return projectRepository.getTotalCounts();
    }

    public List<ACLDetailsEntryDTO> updateProjectAccess(UUID projectId, List<AccessForm> form) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, MANAGE_PROJECT_ACCESS);
        applyMutation(project, new ProjectMutation.EditProjectAccess(form));
        return projectMapper.convertDetailsACLList(project.getFullACL());
    }

    public List<NestedACLEntryDTO> getNestedProjectAccess(UUID projectId) {
        return projectRepository.findNestedAccess(projectId);
    }

    public void applyMutation(ProjectEntity project, ProjectMutation mutation) {
        log.debug("Mutating project {}: {}", project.getId(), mutation);

        ProjectMutationHandler<Mutation, MutationRedoInfo> handler = mutationHandlerRegistry.findHandler(mutation);
        ProjectMutationContext context = new ProjectMutationContext(false, false);
        handler.initContext(project, mutation, context);

        ProjectSnapshot initial = snapshotMapper.createSnapshot(project, context);

        MutationResult result = handler.handle(project, mutation, null, context);

        updateDates(project, userService.getCurrentUserEntity());
        if (project.getId() == null) {
            projectRepository.persist(project);
        }

        ProjectSnapshot target = snapshotMapper.createSnapshot(project, context);
        ProjectPatch diff = createPatch(initial, target, context);

        revisionService.addRevision(project, project.getModifiedAt(), result.summary(), mutation, result.redoInfo(), result.reverseMutation(), diff);
    }

    ProjectPatch createPatch(ProjectSnapshot a, ProjectSnapshot b, ProjectMutationContext context) {
        ProjectDiffHandler valueHandler = new ProjectDiffHandler(context);
        //noinspection DataFlowIssue
        return valueHandler.compare(a, b).updatedValue();
    }

    public List<RevisionDetailsDTO<ProjectPatch>> getProjectRevisions(UUID projectId) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.VIEW_PROJECTS);
        return projectMapper.revisionToDTOList(project.getRevisions());
    }

    @Nullable
    private String mapConstraintToError(ConstraintViolationException e, ProjectEntity project) {
        if ("project_name_uq".equals(e.getConstraintName())) {
            return "Project with name '" + project.getName() + "' already exists";
        }
        return null;
    }
}
