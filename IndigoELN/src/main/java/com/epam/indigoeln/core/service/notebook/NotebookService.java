package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.*;

import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.exception.ChildReferenceException;
import com.epam.indigoeln.core.service.exception.DuplicateFieldException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotebookService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private CustomDtoMapper dtoMapper;

    private static List<Notebook> getNotebooksWithAccess(List<Notebook> notebooks, String userId) {
        return notebooks.stream().filter(notebook -> PermissionUtil.findPermissionsByUserId(
                notebook.getAccessList(), userId) != null).collect(Collectors.toList());
    }

    public List<TreeNodeDTO> getAllNotebookTreeNodes(String projectId) {
        return getAllNotebookTreeNodes(projectId, null);
    }

    public List<TreeNodeDTO> getAllNotebookTreeNodes(String projectId, User user) {
        Collection<Notebook> notebooks = getAllNotebooks(projectId, user);
        return notebooks.stream().map(TreeNodeDTO::new).sorted(TreeNodeDTO.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    /**
     * If user is null, then retrieve notebooks without checking for UserPermissions
     * Otherwise, use checking for UserPermissions
     */
    public Collection<Notebook> getAllNotebooks(String  projectId, User user) {
        Project project = Optional.ofNullable(projectRepository.findOne(projectId)).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));

        if (user == null) {
            return project.getNotebooks();
        }

        // Check of EntityAccess (User must have "Read Entity" permission in project access list)
        if (!PermissionUtil.hasPermissions(user.getId(), project.getAccessList(),
                UserPermission.READ_ENTITY)) {
            throw OperationDeniedException.createProjectSubEntitiesReadOperation(project.getId());
        }

        return getNotebooksWithAccess(project.getNotebooks(), user.getId());

    }

    /**
     * Fetch all notebooks for current user available when create sub entity
     * return notebooks where users is in accessList and has permission CREATE_SUB_ENTITY,
     * or has authority CONTENT_EDITOR (but not in accessLIst)
     * @param user user
     * @return list of notebooks available for experiment creation
     */
    public List<ShortEntityDTO> getNotebooksForExperimentCreation(User user) {
        List<Notebook> notebooks = PermissionUtil.isContentEditor(user) ?
            notebookRepository.findAllIgnoreChildren() :
                notebookRepository.findByUserIdAndPermissionsShort(user.getId(), Collections.singletonList(UserPermission.CREATE_SUB_ENTITY));
        return notebooks.stream().map(ShortEntityDTO::new).sorted(Comparator.comparing(ShortEntityDTO::getName)).collect(Collectors.toList());
    }

    public NotebookDTO getNotebookById(String projectId, String id, User user) {
        String fullNotebookId = SequenceIdUtil.buildFullId(projectId, id);
        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(fullNotebookId)).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(id));

        // Check of EntityAccess (User must have "Read Entity" permission in project access list and
        // "Read Entity" permission in notebook access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Project project = projectRepository.findByNotebookId(fullNotebookId);
            if (project == null) {
                throw EntityNotFoundException.createWithProjectChildId(notebook.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    project.getAccessList(), UserPermission.READ_ENTITY,
                    notebook.getAccessList(), UserPermission.READ_ENTITY)) {
                throw OperationDeniedException.createNotebookReadOperation(notebook.getId());
            }
        }

        return new NotebookDTO(notebook);
    }

    public NotebookDTO createNotebook(NotebookDTO notebookDTO, String projectId, User user) {
        Project project = Optional.ofNullable(projectRepository.findOne(projectId)).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in project access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw OperationDeniedException.createProjectSubEntityCreateOperation(project.getId());
        }

        Notebook notebook = dtoMapper.convertFromDTO(notebookDTO);

        // reset notebook's id
        notebook.setId(sequenceIdService.getNextNotebookId(projectId));

        // check of user permissions correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, notebook.getAccessList());
        // add OWNER's permissions for specified User to notebook
        PermissionUtil.addOwnerToAccessList(notebook.getAccessList(), user);

        saveNotebookAndHandleError(notebook);

        // add all users as VIEWER to project
        notebook.getAccessList().forEach(up ->
                PermissionUtil.addUserPermissions(project.getAccessList(), up.getUser(), UserPermission.VIEWER_PERMISSIONS));

        project.getNotebooks().add(notebook);
        projectRepository.save(project);

        return new NotebookDTO(notebook);
    }

    public NotebookDTO updateNotebook(NotebookDTO notebookDTO, String projectId, User user) {
        String fullNotebookId = SequenceIdUtil.buildFullId(projectId, notebookDTO.getId());
        Notebook notebookFromDB = Optional.ofNullable(notebookRepository.findOne(fullNotebookId)).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookDTO.getId()));

        // Check of EntityAccess (User must have "Read Entity" permission in project access list and
        // "Update Entity" permission in notebook access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Project project = projectRepository.findByNotebookId(fullNotebookId);
            if (project == null) {
                throw EntityNotFoundException.createWithNotebookChildId(notebookFromDB.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    project.getAccessList(), UserPermission.READ_ENTITY,
                    notebookFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                throw OperationDeniedException.createNotebookUpdateOperation(notebookFromDB.getId());
            }
        }

        Notebook notebook = dtoMapper.convertFromDTO(notebookDTO);
        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, notebook.getAccessList());

        notebookFromDB.setName(notebookDTO.getName());
        notebookFromDB.setAccessList(notebook.getAccessList());// Stay old notebook's experiments for updated notebook
        NotebookDTO result = new NotebookDTO(saveNotebookAndHandleError(notebookFromDB));

        Project project = Optional.ofNullable(projectRepository.findOne(projectId)).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));
        // add all users as VIEWER to project
        notebook.getAccessList().forEach(up ->
                PermissionUtil.addUserPermissions(project.getAccessList(), up.getUser(), UserPermission.VIEWER_PERMISSIONS));
        projectRepository.save(project);

        return result;
    }

    public void deleteNotebook(String projectId, String id) {
        String fullNotebookId = SequenceIdUtil.buildFullId(projectId, id);
        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(fullNotebookId)).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(id));

        if(notebook.getExperiments() != null && !notebook.getExperiments().isEmpty()) {
            throw new ChildReferenceException(notebook.getId());
        }

        Project project = projectRepository.findByNotebookId(fullNotebookId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectChildId(notebook.getId());
        }

        project.getNotebooks().remove(notebook);
        projectRepository.save(project);

        notebookRepository.delete(notebook);
    }

    /**
     * Checks if user can be deleted from notebook's access list with all the permissions without any problems.
     * It checks all the experiments and if any has this user added, then it will return false.
     * @param projectId project id
     * @param notebookId notebook id
     * @param userId user id
     * @return true if none of experiments has user added to it, true otherwise
     */
    public boolean isUserRemovable(String projectId, String notebookId, String userId) {
        String fullNotebookId = SequenceIdUtil.buildFullId(projectId, notebookId);
        Optional<Notebook> notebookOpt =  Optional.ofNullable(notebookRepository.findOne(fullNotebookId));
        Notebook notebook = notebookOpt.orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

        return notebook.getExperiments().stream().filter(e -> {
            UserPermission permission = PermissionUtil.findPermissionsByUserId(e.getAccessList(), userId);
            return permission != null;
        }).count() == 0;
    }

    private Notebook saveNotebookAndHandleError(Notebook notebook) {
        try {
            return notebookRepository.save(notebook);
        } catch (DuplicateKeyException e) {
            throw DuplicateFieldException.createWithNotebookName(notebook.getName(), e);
        }
    }
}