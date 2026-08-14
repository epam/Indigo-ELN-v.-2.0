package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.util.SearchVectorField;
import com.epam.indigoeln.eln.util.SearchVectorUpdater;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.service.mutation.project.AbstractProjectMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.project.ProjectMutationContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.epam.indigoeln.eln.model.ApplicationPermission.*;

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
    SearchVectorUpdater searchVectorUpdater;

    public ProjectDetailsDTO createProject(ProjectRequest request) {
        ProjectEntity project = new ProjectEntity();
        applyMutation(project, projectMapper.requestToMutation(request));
        return getProject(project.getId());
    }

    public Page<ProjectDTO> getProjects(@Nullable String search, @Nullable SortOrder sort, @Nullable Boolean createdByMe, Paging paging) {
        UserEntity currentUser = Boolean.TRUE.equals(createdByMe) ? userService.getCurrentUserEntity() : null;
        boolean showAll = userService.getCurrentUser().getPermissions().contains(ApplicationPermission.VIEW_PROJECTS);
        return projectRepository.findAll(search, sort, currentUser, paging, showAll);
    }

    public ProjectDetailsDTO getProject(UUID projectId) {
        ProjectEntity project = projectRepository.load(projectId);
        return getProjectDetails(project);
    }

    private ProjectDetailsDTO getProjectDetails(ProjectEntity project) {
        Set<ApplicationPermission> currentPermissions = aclService.getCurrentPermissions(project.getCurrentAccess());
        currentPermissions.retainAll(EnumSet.of(VIEW_PROJECTS, EDIT_PROJECTS, MANAGE_PROJECT_ACCESS, DELETE_PROJECTS));
        return projectMapper.entityToDetailsDTO(project, currentPermissions);
    }

    public ProjectExistenceCheckDTO checkExistenceByName(String name) {
        boolean exists = projectRepository.existsByName(name);
        return new ProjectExistenceCheckDTO(exists);
    }

    public List<String> suggestKeywords(@Nullable String search) {
        return projectRepository.suggestKeywords(search);
    }

    public ProjectDetailsDTO editProject(UUID projectId, ProjectEditRequest request) {
        ProjectEntity project = projectRepository.loadAndLock(projectId);
        applyMutation(project, projectMapper.requestToMutation(request));
        return getProjectDetails(project);
    }

    public TotalCounts getTotalCounts() {
        return projectRepository.getTotalCounts();
    }

    public List<ACLEntryDTO> updateProjectAccess(UUID projectId, List<AccessForm> form) {
        ProjectEntity project = projectRepository.loadAndLock(projectId); // protect project and its tree from changes
        applyMutation(project, new ProjectMutation.EditProjectAccess(form));
        return projectMapper.convertACLList(project.getFullACL());
    }

    public MutationResult<ProjectSnapshot, ProjectMutationContext> applyMutation(ProjectEntity project, ProjectMutation mutation) {
        log.debug("Mutating project {}: {}", project.getId(), mutation);
        return mutationHandlerRegistry.withHandler(mutation, (AbstractProjectMutationHandler<ProjectMutation> handler) -> handler.applyMutation(project, mutation));
    }

    public List<RevisionSummaryDTO> getProjectRevisions(UUID projectId) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.VIEW_PROJECTS);
        return projectMapper.revisionToDTOList(projectRepository.getRevisions(project));
    }

    public List<@Nullable SearchVectorField> collectSearchFields(ProjectEntity entity) {
        List<@Nullable SearchVectorField> fields = new ArrayList<>();
        fields.add(SearchVectorField.a(entity.getName()));
        for (String keyword : entity.getKeywords()) {
            fields.add(SearchVectorField.b(keyword));
        }
        fields.add(SearchVectorField.d(entity.getDescription()));
        fields.add(SearchVectorField.d(entity.getLiterature()));
        //noinspection ConstantValue
        fields.add(SearchVectorField.c(entity.getCreatedBy() != null ? entity.getCreatedBy().getDisplayName() : null));
        return fields;
    }

    public void updateSearchVector(ProjectEntity project, List<@Nullable SearchVectorField> fields) {
        searchVectorUpdater.update("Project", project.getId(), fields);
    }
}
