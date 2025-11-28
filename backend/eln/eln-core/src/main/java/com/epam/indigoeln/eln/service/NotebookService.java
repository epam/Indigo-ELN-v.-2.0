package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.QueryParam;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@DataAccess
@Transactional
@ApplicationScoped
public class NotebookService {

    @Inject
    NotebookRepository notebookRepository;
    @Inject
    NotebookMapper notebookMapper;
    @Inject
    UserService userService;
    @Inject
    ACLService aclService;
    @Inject
    ProjectRepository projectRepository;

    public NotebookDetailsDTO createNotebook(UUID projectId, NotebookRequest request) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.CREATE_NOTEBOOKS);
        NotebookEntity notebook = notebookMapper.requestToNotebook(request);
        project.getNotebooks().add(notebook);
        notebook.setProject(project);
        updateDates(notebook, userService.getCurrentUserEntity());
        aclService.initNotebookACL(notebook);
        try {
            notebookRepository.persist(notebook);
            notebookRepository.flushAndClear(notebook);
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            if ("notebook_name_uq".equals(e.getConstraintName())) {
                throw new InvalidRequestException("Notebook with name '" + notebook.getName() + "' already exists");
            }
            throw e;
        }
        return getNotebook(notebook.getId());
    }

    public Page<NotebookDTO> getNotebooks(UUID projectId, @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort,
                                          @QueryParam("createdByMe") @Nullable Boolean createdByMe, Paging paging) {
        UserEntity currentUser = Boolean.TRUE.equals(createdByMe) ? userService.getCurrentUserEntity() : null;
        return notebookRepository.findAll(projectId, search, sort, currentUser, paging);
    }

    public NotebookDetailsDTO getNotebook(UUID notebookId) {
        return notebookRepository.loadDetails(notebookId);
    }

    public NotebookDetailsDTO editNotebook(UUID notebookId, NotebookEditRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
        editProperty(request.getName(), notebook::setName);
        editProperty(request.getDescription(), notebook::setDescription);
        updateDates(notebook, userService.getCurrentUserEntity());
        notebookRepository.flushAndClear(notebook);
        return getNotebook(notebookId);
    }

    public List<ACLDetailsEntryDTO> updateNotebookAccess(UUID notebookId, List<AccessForm> form) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
//        notebookRepository.getEntityManager().lock(notebook.getProject(), LockModeType.PESSIMISTIC_WRITE);
        aclService.ensureAccess(notebook, ApplicationPermission.MANAGE_NOTEBOOK_ACCESS);
        for (AccessForm item : form) {
            UserEntity user = userService.getUserEntity(item.getUserID());
            aclService.updateNotebookACL(notebook.getProject(), notebook, user, item.getLevel());
        }
        return notebookMapper.convertACLMap(notebook.getAclEntities());
    }
}
