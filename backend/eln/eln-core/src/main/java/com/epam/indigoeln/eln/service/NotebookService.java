package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.util.ListWithTotal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
        aclService.ensureAccess(project, AccessOperation.CREATE_NOTEBOOK);
        NotebookEntity notebook = notebookMapper.requestToNotebook(request);
        project.getNotebooks().add(notebook);
        notebook.setProject(project);
        updateDates(notebook, userService.getCurrentUser());
        aclService.initNotebookACL(notebook);
        notebookRepository.persist(notebook);
        notebookRepository.flushAndClear();
        return getNotebook(notebook.getId());
    }

    public Page<NotebookDTO> getNotebooks(UUID projectId, @Nullable String search, Paging paging) {
        ListWithTotal<NotebookDTO> list = notebookRepository.findAll(projectId, search, paging);
        return Page.of(paging, list.total(), list.list());
    }

    public NotebookDetailsDTO getNotebook(UUID notebookId) {
        return notebookRepository.loadDetails(notebookId);
    }

    @SuppressWarnings("OptionalAssignedToNull")
    public NotebookDetailsDTO editNotebook(UUID notebookId, NotebookEditRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, AccessOperation.EDIT);
        editProperty(request.getName(), notebook::setName);
        editProperty(request.getDescription(), notebook::setDescription);
        updateDates(notebook, userService.getCurrentUser());
        notebookRepository.flushAndClear();
        return getNotebook(notebookId);
    }

    public List<ACLEntryDTO> updateNotebookAccess(UUID notebookId, List<AccessForm> form) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
//        notebookRepository.getEntityManager().lock(notebook.getProject(), LockModeType.PESSIMISTIC_WRITE);
        aclService.ensureAccess(notebook, AccessOperation.MANAGE_PERMISSIONS);
        for (AccessForm item : form) {
            UserEntity user = userService.getUserEntity(item.getUserID());
            aclService.updateNotebookACL(notebook.getProject(), notebook, user, item.getLevel());
        }
        return notebookMapper.convertACLMap(notebook.getAclEntities());
    }
}
