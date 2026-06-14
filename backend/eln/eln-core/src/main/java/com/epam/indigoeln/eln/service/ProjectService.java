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
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.project.AbstractProjectMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.project.ProjectMutationContext;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        ProjectEntity project = projectRepository.loadDetails(projectId);
        Set<ApplicationPermission> currentPermissions = aclService.getCurrentPermissions(project.getCalculatedInfo() != null ? project.getCalculatedInfo().getCurrentAccess() : null);
        currentPermissions.retainAll(EnumSet.of(VIEW_PROJECTS, EDIT_PROJECTS, MANAGE_PROJECT_ACCESS, DELETE_PROJECTS));
        return projectMapper.entityToDetailsDTO(project, currentPermissions);
    }

    public ProjectExistenceCheckDTO checkExistenceByName(String name) {
        boolean exists = projectRepository.existsByName(name);
        return new ProjectExistenceCheckDTO(exists);
    }

    public ProjectDetailsDTO editProject(UUID projectId, ProjectEditRequest request) {
        ProjectEntity project = projectRepository.get(projectId);
        applyMutation(project, projectMapper.requestToMutation(request));
        return getProject(project.getId());
    }

    public TotalCounts getTotalCounts() {
        return projectRepository.getTotalCounts();
    }

    public List<ACLEntryDTO> updateProjectAccess(UUID projectId, List<AccessForm> form) {
        ProjectEntity project = projectRepository.get(projectId);
        applyMutation(project, new ProjectMutation.EditProjectAccess(form));
        return projectMapper.convertACLList(project.getFullACL());
    }

    public List<NestedACLEntryDTO> getNestedProjectAccess(UUID projectId) {
        return projectRepository.findNestedAccess(projectId);
    }

    public Triple<ProjectSnapshot, JsonNode, ProjectMutationContext> applyMutation(ProjectEntity project, ProjectMutation mutation) {
        log.debug("Mutating project {}: {}", project.getId(), mutation);
        AbstractProjectMutationHandler<Mutation> handler = mutationHandlerRegistry.findHandler(mutation);
        return handler.applyMutation(project, mutation);
    }

    public List<RevisionSummaryDTO> getProjectRevisions(UUID projectId) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.VIEW_PROJECTS);
        return projectMapper.revisionToDTOList(projectRepository.getRevisions(project));
    }
}
