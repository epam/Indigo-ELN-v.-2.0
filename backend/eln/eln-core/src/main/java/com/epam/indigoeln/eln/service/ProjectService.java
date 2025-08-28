package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

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
    DictionaryService dictionaryService;

    public ProjectDetailsDTO createProject(ProjectRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.CREATE_PROJECTS);
        ProjectEntity project = projectMapper.requestToProject(request);
        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
            project.setKeywords(dictionaryService.findOrCreateByNames(Dictionary.PROJECT_KEYWORD, request.getKeywords()));
        }
        updateDates(project, userService.getCurrentUser());
        aclService.initProjectACL(project);
        try {
            projectRepository.persist(project);
            projectRepository.flushAndClear();
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            if ("project_name_uq".equals(e.getConstraintName())) {
                throw new InvalidRequestException("Project with name '" + project.getName() + "' already exists");
            }
            throw e;
        }
        return getProject(project.getId());
    }

    public Page<ProjectDTO> getProjects(@Nullable String search, @Nullable SortOrder sort, @Nullable Boolean createdByMe, Paging paging) {
        UserEntity currentUser = null;

        if (Boolean.TRUE.equals(createdByMe)) {
            currentUser = userService.getCurrentUser();
        }

        SortOrder sortOrder = (sort != null) ? sort : SortOrder.LATEST;

        return projectRepository.findAll(search, sortOrder, currentUser, paging);
    }

    public ProjectDetailsDTO getProject(UUID projectId) {
        return projectRepository.loadDetails(projectId);
    }

    public ProjectDetailsDTO editProject(UUID projectId, ProjectEditRequest request) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
        editProperty(request.getName(), project::setName);
        editProperty(request.getKeywords(), v -> {
            project.setKeywords(dictionaryService.findOrCreateByNames(Dictionary.PROJECT_KEYWORD, v));
        });
        editProperty(request.getLiterature(), project::setLiterature);
        editProperty(request.getDescription(), project::setDescription);
        updateDates(project, userService.getCurrentUser());
        projectRepository.flushAndClear();
        return getProject(projectId);
    }

    public TotalCounts getTotalCounts() {
        return projectRepository.getTotalCounts();
    }

    public List<ACLDetailsEntryDTO> updateProjectAccess(UUID projectId, List<AccessForm> form) {
        ProjectEntity project = projectRepository.get(projectId);
        // TODO issue separate select for update
//        projectRepository.getEntityManager().lock(project, LockModeType.PESSIMISTIC_WRITE);
        aclService.ensureAccess(project, ApplicationPermission.MANAGE_PROJECT_ACCESS);
        for (AccessForm item : form) {
            UserEntity user = userService.getUserEntity(item.getUserID());
            aclService.updateProjectACL(project, user, item.getLevel());
        }
        return projectMapper.convertACLMap(project.getAclEntities());
    }
}
