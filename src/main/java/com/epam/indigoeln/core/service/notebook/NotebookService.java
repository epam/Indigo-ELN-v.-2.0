package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.sequenceid.SequenceIdRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.exception.ChildReferenceException;
import com.epam.indigoeln.core.service.exception.DuplicateFieldException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
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
    private SequenceIdRepository sequenceIdRepository;

    @Autowired
    private CustomDtoMapper dtoMapper;

    public List<TreeNodeDTO> getAllNotebookTreeNodes(Long projectSequenceId) {
        return getAllNotebookTreeNodes(projectSequenceId, null);
    }

    public List<TreeNodeDTO> getAllNotebookTreeNodes(Long projectSequenceId, User user) {
        Collection<Notebook> notebooks = getAllNotebooks(projectSequenceId, user);
        return notebooks.stream().
                map(notebook -> new TreeNodeDTO(new NotebookDTO(notebook), hasExperiments(notebook, user))).
                collect(Collectors.toList());
    }

    /**
     * If user is null, then retrieve notebooks without checking for UserPermissions
     * Otherwise, use checking for UserPermissions
     */
    private Collection<Notebook> getAllNotebooks(Long  projectSequenceId, User user) {
        Project project = projectRepository.findOneBySequenceId(projectSequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectSequenceId.toString()));

        if (user == null) {
            return project.getNotebooks();
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in project access list)
        if (!PermissionUtil.hasPermissions(user.getId(), project.getAccessList(),
                UserPermission.READ_SUB_ENTITY)) {
            throw OperationDeniedException.createProjectSubEntitiesReadOperation(project.getId());
        }

        return getNotebooksWithAccess(project.getNotebooks(), user.getId());

    }

    /**
     * If user is null, then check only notebook's experiments list for empty
     * Otherwise, use checking for UserPermissions
     */
    private boolean hasExperiments(Notebook notebook, User user) {
        if (user == null) {
            return !notebook.getExperiments().isEmpty();
        }
        // Checking userPermission for "Read Sub-Entity" possibility,
        // and that notebook has experiments with UserPermission for specified User
        return PermissionUtil.hasPermissions(user.getId(), notebook.getAccessList(),
                UserPermission.READ_SUB_ENTITY) &&
                hasExperimentsWithAccess(notebook.getExperiments(), user.getId());
    }

    private boolean hasExperimentsWithAccess(List<Experiment> experiments, String userId) {
        // Because we have one at least Experiment with UserPermission for Read Entity
        return experiments.stream().anyMatch(e -> PermissionUtil.findPermissionsByUserId(e.getAccessList(), userId) != null);
    }

    public NotebookDTO getNotebookById(Long sequenceId, User user) {
        Notebook notebook = notebookRepository.findOneBySequenceId(sequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(sequenceId.toString()));

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in project access list and
        // "Read Entity" permission in notebook access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Project project = projectRepository.findByNotebookId(notebook.getId());
            if (project == null) {
                throw EntityNotFoundException.createWithProjectChildId(notebook.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    project.getAccessList(), UserPermission.READ_SUB_ENTITY,
                    notebook.getAccessList(), UserPermission.READ_ENTITY)) {
                throw OperationDeniedException.createNotebookReadOperation(notebook.getId());
            }
        }

        return new NotebookDTO(notebook);
    }

    public NotebookDTO createNotebook(NotebookDTO notebookDTO, Long projectSequenceId, User user) {
        Project project = projectRepository.findOneBySequenceId(projectSequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectSequenceId.toString()));

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in project access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(),
                    UserPermission.CREATE_SUB_ENTITY)) {
            throw OperationDeniedException.createProjectSubEntityCreateOperation(project.getId());
        }

        Notebook notebook = dtoMapper.convertFromDTO(notebookDTO);

        // reset notebook's id
        notebook.setId(null);
        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, notebook.getAccessList());
        // add OWNER's permissions for specified User to notebook
        PermissionUtil.addOwnerToAccessList(notebook.getAccessList(), user);

        //set notebook sequence #
        notebook.setSequenceId(sequenceIdRepository.getNextNotebookId());

        try {
            notebook = notebookRepository.save(notebook);
        } catch (DuplicateKeyException e) {
            throw DuplicateFieldException.createWithNotebookName(notebook.getName());
        }

        project.getNotebooks().add(notebook);
        projectRepository.save(project);

        return new NotebookDTO(notebook);
    }

    public NotebookDTO updateNotebook(NotebookDTO notebookDTO, User user) {
        Notebook notebookFromDB = notebookRepository.findOneBySequenceId(notebookDTO.getSequenceId()).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookDTO.getSequenceId().toString()));

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in project access list and
        // "Update Entity" permission in notebook access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Project project = projectRepository.findByNotebookId(notebookFromDB.getId());
            if (project == null) {
                throw EntityNotFoundException.createWithNotebookChildId(notebookFromDB.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    project.getAccessList(), UserPermission.CREATE_SUB_ENTITY,
                    notebookFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                throw OperationDeniedException.createNotebookUpdateOperation(notebookFromDB.getId());
            }
        }

        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, notebookDTO.getAccessList());

        notebookFromDB.setName(notebookDTO.getName());
        notebookFromDB.setAccessList(notebookDTO.getAccessList());// Stay old notebook's experiments for updated notebook
        return new NotebookDTO(notebookRepository.save(notebookFromDB));
    }

    public void deleteNotebook(Long sequenceId) {
        Notebook notebook = notebookRepository.findOneBySequenceId(sequenceId).orElseThrow(
                () -> EntityNotFoundException.createWithNotebookId(sequenceId.toString()));

        if(notebook.getExperiments() != null && !notebook.getExperiments().isEmpty()) {
            throw new ChildReferenceException(notebook.getId());
        }

        Project project = projectRepository.findByNotebookId(notebook.getId());
        if (project == null) {
            throw EntityNotFoundException.createWithProjectChildId(notebook.getId());
        }

        project.getNotebooks().remove(notebook);
        projectRepository.save(project);

        notebookRepository.delete(notebook);
    }

    private static List<Notebook> getNotebooksWithAccess(List<Notebook> notebooks, String userId) {
        return notebooks.stream().filter(notebook -> PermissionUtil.findPermissionsByUserId(
                notebook.getAccessList(), userId) != null).collect(Collectors.toList());
    }
}