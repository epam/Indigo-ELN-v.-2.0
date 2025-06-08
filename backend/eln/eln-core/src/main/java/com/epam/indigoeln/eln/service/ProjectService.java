package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.ProjectKeywordEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ProjectKeywordsRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.util.ListWithTotal;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Set;
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
    ProjectKeywordsRepository projectKeywordsRepository;
    @Inject
    ACLService aclService;

    public ProjectDetailsDTO createProject(ProjectRequest request) {
        aclService.ensureTopLevelAccess(AccessOperation.CREATE_PROJECT);
        ProjectEntity project = projectMapper.requestToProject(request);
        if (request.getKeywords() != null) {
            updateKeywords(project, request.getKeywords());
        }
        updateDates(project, userService.getCurrentUser());
        aclService.initProjectACL(project);
        projectRepository.persist(project);
        projectRepository.flushAndClear();
        return getProject(project.getId());
    }

    public Page<ProjectDTO> getProjects(@Nullable String search, Paging paging) {
        ListWithTotal<ProjectDTO> list = projectRepository.findAll(search, paging);
        return Page.of(paging, list.total(), list.list());
    }

    public ProjectDetailsDTO getProject(UUID projectId) {
        return projectRepository.loadDetails(projectId);
    }

    public ProjectDetailsDTO editProject(UUID projectId, ProjectEditRequest request) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, AccessOperation.EDIT);
        editProperty(request.getName(), project::setName);
        editProperty(request.getKeywords(), v -> updateKeywords(project, v));
        editProperty(request.getLiterature(), project::setLiterature);
        editProperty(request.getDescription(), project::setDescription);
        updateDates(project, userService.getCurrentUser());
        projectRepository.flushAndClear();
        return getProject(projectId);
    }

    public TotalCounts getTotalCounts() {
        return projectRepository.getTotalCounts();
    }

    public List<ACLEntryDTO> updateProjectAccess(UUID projectId, List<AccessForm> form) {
        ProjectEntity project = projectRepository.get(projectId);
        // TODO issue separate select for update
//        projectRepository.getEntityManager().lock(project, LockModeType.PESSIMISTIC_WRITE);
        aclService.ensureAccess(project, AccessOperation.MANAGE_PERMISSIONS);
        for (AccessForm item : form) {
            UserEntity user = userService.getUser(item.getUserID());
            aclService.updateProjectACL(project, user, item.getLevel());
        }
        return projectMapper.convertACLMap(project.getAclEntities());
    }

    public List<String> suggestProjectKeywords(@Nullable String search, Paging paging) {
        return projectKeywordsRepository.suggest(search, paging);
    }

    private void updateKeywords(ProjectEntity project, List<String> keywords) {
        Set<String> existing = StreamEx.of(project.getKeywords()).map(ProjectKeywordEntity::getName).toSet();
        Set<String> updated = StreamEx.of(keywords).map(String::toLowerCase).toMutableSet();
        // deleted
        project.getKeywords().removeIf(keyword -> !updated.contains(keyword.getName()));
        // new
        Set<String> added = StreamEx.of(updated)
                .remove(existing::contains)
                .toMutableSet();
        if (!added.isEmpty()) {
            List<ProjectKeywordEntity> found = projectKeywordsRepository.find(added);
            project.getKeywords().addAll(found);
            for (ProjectKeywordEntity entity : found) {
                added.remove(entity.getName());
            }
            for (String notFound : added) {
                ProjectKeywordEntity entity = new ProjectKeywordEntity(notFound);
                projectKeywordsRepository.persist(entity);
                project.getKeywords().add(entity);
            }
        }
    }
}
