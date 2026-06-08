package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.notebook.AbstractNotebookMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.notebook.NotebookMutationContext;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.QueryParam;
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
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;

    public NotebookDetailsDTO createNotebook(UUID projectId, NotebookRequest request) {
        NotebookEntity notebook = new NotebookEntity();
        ProjectEntity project = projectRepository.get(projectId);
        project.getNotebooks().add(notebook);
        notebook.setProject(project);
        applyMutation(notebook, notebookMapper.requestToMutation(request));
        return getNotebook(notebook.getId());
    }

    public Page<NotebookDTO> getNotebooks(UUID projectId, @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort,
                                          @QueryParam("createdByMe") @Nullable Boolean createdByMe, Paging paging) {
        UserEntity currentUser = Boolean.TRUE.equals(createdByMe) ? userService.getCurrentUserEntity() : null;
        boolean showAll = userService.getCurrentUser().getPermissions().contains(ApplicationPermission.VIEW_NOTEBOOKS);
        return notebookRepository.findAll(projectId, search, sort, currentUser, paging, showAll);
    }

    public NotebookExistenceCheckDTO checkExistenceByName(String name) {
        boolean exists = notebookRepository.existsByName(name);
        return new NotebookExistenceCheckDTO(exists);
    }

    public NotebookDetailsDTO getNotebook(UUID notebookId) {
        NotebookEntity notebook = notebookRepository.loadDetails(notebookId);
        Set<ApplicationPermission> currentPermissions = aclService.getCurrentPermissions(notebook.getCalculatedInfo() != null ? notebook.getCalculatedInfo().getCurrentAccess() : null);
        currentPermissions.retainAll(EnumSet.of(VIEW_NOTEBOOKS, EDIT_NOTEBOOKS, MANAGE_NOTEBOOK_ACCESS, DELETE_NOTEBOOKS));
        return notebookMapper.entityToDetailsDTO(notebook, currentPermissions);
    }

    public NotebookDetailsDTO editNotebook(UUID notebookId, NotebookEditRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        applyMutation(notebook, notebookMapper.requestToMutation(request));
        return getNotebook(notebookId);
    }

    public List<ACLEntryDTO> updateNotebookAccess(UUID notebookId, List<AccessForm> form) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.MANAGE_NOTEBOOK_ACCESS);
        applyMutation(notebook, new NotebookMutation.EditNotebookAccess(form));
        return notebookMapper.convertACLList(notebook.getFullACL());
    }

    public List<NestedACLEntryDTO> getNestedNotebookAccess(UUID projectId) {
        return notebookRepository.findNestedAccess(projectId);
    }

    public Triple<NotebookSnapshot, JsonNode, NotebookMutationContext> applyMutation(NotebookEntity notebook, NotebookMutation mutation) {
        log.debug("Mutating notebook {}: {}", notebook.getId(), mutation);
        AbstractNotebookMutationHandler<Mutation> handler = mutationHandlerRegistry.findHandler(mutation);
        return handler.applyMutation(notebook, mutation);
    }

    public List<RevisionSummaryDTO> getNotebookRevisions(UUID notebookId) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.VIEW_NOTEBOOKS);
        return notebookMapper.revisionToDTOList(notebookRepository.getRevisions(notebook));
    }
}
