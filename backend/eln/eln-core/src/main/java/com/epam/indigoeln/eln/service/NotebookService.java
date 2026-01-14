package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutationContext;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.NotebookDiffHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.service.mutation.NotebookMutationHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.QueryParam;
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
    @Inject
    SnapshotMapper snapshotMapper;
    @Inject
    RevisionService revisionService;

    public NotebookDetailsDTO createNotebook(UUID projectId, NotebookRequest request) {
        NotebookEntity notebook = new NotebookEntity();
        return wrapConstraintViolation(() -> {
            ProjectEntity project = projectRepository.get(projectId);
            aclService.ensureAccess(project, ApplicationPermission.CREATE_NOTEBOOKS);
            project.getNotebooks().add(notebook);
            notebook.setProject(project);
            applyMutation(notebook, notebookMapper.requestToMutation(request));
            notebookRepository.flushAndRefresh(notebook);
            return getNotebook(notebook.getId());
        }, e -> mapConstraintToError(e, notebook));
    }

    public Page<NotebookDTO> getNotebooks(UUID projectId, @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort,
                                          @QueryParam("createdByMe") @Nullable Boolean createdByMe, Paging paging) {
        UserEntity currentUser = Boolean.TRUE.equals(createdByMe) ? userService.getCurrentUserEntity() : null;
        boolean showAll = userService.getCurrentUser().getPermissions().contains(ApplicationPermission.VIEW_NOTEBOOKS);
        return notebookRepository.findAll(projectId, search, sort, currentUser, paging, showAll);
    }

    public NotebookDetailsDTO getNotebook(UUID notebookId) {
        NotebookEntity notebook = notebookRepository.loadDetails(notebookId);
        Set<ApplicationPermission> currentPermissions = aclService.getCurrentPermissions(notebook.getCalculatedInfo() != null ? notebook.getCalculatedInfo().getCurrentAccess() : null);
        currentPermissions.retainAll(EnumSet.of(VIEW_NOTEBOOKS, EDIT_NOTEBOOKS, MANAGE_NOTEBOOK_ACCESS, DELETE_NOTEBOOKS));
        return notebookMapper.entityToDetailsDTO(notebook, currentPermissions);
    }

    public NotebookDetailsDTO editNotebook(UUID notebookId, NotebookEditRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
        return wrapConstraintViolation(() -> {
            applyMutation(notebook, notebookMapper.requestToMutation(request));
            return getNotebook(notebookId);
        }, e -> mapConstraintToError(e, notebook));
    }

    public List<ACLDetailsEntryDTO> updateNotebookAccess(UUID notebookId, List<AccessForm> form) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.MANAGE_NOTEBOOK_ACCESS);
        applyMutation(notebook, new NotebookMutation.EditNotebookAccess(form));
        return notebookMapper.convertDetailsACLList(notebook.getFullACL());
    }

    public List<NestedACLEntryDTO> getNestedNotebookAccess(UUID projectId) {
        return notebookRepository.findNestedAccess(projectId);
    }

    public void applyMutation(NotebookEntity notebook, NotebookMutation mutation) {
        log.debug("Mutating notebook {}: {}", notebook.getId(), mutation);

        NotebookMutationHandler<Mutation, MutationRedoInfo> handler = mutationHandlerRegistry.findHandler(mutation);
        NotebookMutationContext context = new NotebookMutationContext(false, false);
        handler.initContext(notebook, mutation, context);

        NotebookSnapshot initial = snapshotMapper.createSnapshot(notebook, context);

        MutationResult result = handler.handle(notebook, mutation, null, context);

        updateDates(notebook, userService.getCurrentUserEntity());
        if (notebook.getId() == null) {
            notebookRepository.persist(notebook);
        }

        NotebookSnapshot target = snapshotMapper.createSnapshot(notebook, context);
        NotebookPatch diff = createPatch(initial, target, context);

        revisionService.addRevision(notebook, notebook.getModifiedAt(), result.summary(), mutation, result.redoInfo(), result.reverseMutation(), diff);
    }

    NotebookPatch createPatch(NotebookSnapshot a, NotebookSnapshot b, NotebookMutationContext context) {
        NotebookDiffHandler valueHandler = new NotebookDiffHandler(context);
        //noinspection DataFlowIssue
        return valueHandler.compare(a, b).updatedValue();
    }

    public List<RevisionDetailsDTO<NotebookPatch>> getNotebookRevisions(UUID notebookId) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.VIEW_NOTEBOOKS);
        return notebookMapper.revisionToDTOList(notebook.getRevisions());
    }

    @Nullable
    private String mapConstraintToError(ConstraintViolationException e, NotebookEntity notebook) {
        if ("notebook_name_uq".equals(e.getConstraintName())) {
            return "Notebook with name '" + notebook.getName() + "' already exists";
        }
        return null;
    }
}
