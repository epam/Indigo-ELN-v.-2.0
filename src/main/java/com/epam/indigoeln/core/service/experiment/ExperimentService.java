package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.exception.ConcurrencyException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentTreeNodeDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import com.google.common.util.concurrent.Striped;
import com.mongodb.DBRef;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Service
public class ExperimentService {

    @Autowired
    private CustomDtoMapper dtoMapper;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private NotebookRepository notebookRepository;
    @Autowired
    private ExperimentRepository experimentRepository;
    @Autowired
    private ComponentRepository componentRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SequenceIdService sequenceIdService;
    @Autowired
    private SimpMessagingTemplate template;

    private Striped<Lock> locks = Striped.lazyWeakLock(2);

    private static List<Experiment> getExperimentsWithAccess(List<Experiment> experiments, String userId) {
        return experiments == null ? new ArrayList<>() :
                experiments.stream().filter(experiment -> PermissionUtil.findPermissionsByUserId(
                        experiment.getAccessList(), userId) != null).collect(Collectors.toList());
    }

    public List<TreeNodeDTO> getAllExperimentTreeNodes(String projectId, String notebookId) {
        return getAllExperimentTreeNodes(projectId, notebookId, null);
    }

    public List<TreeNodeDTO> getAllExperimentTreeNodes(String projectId, String notebookId, User user) {
        Collection<Experiment> experiments = getAllExperiments(projectId, notebookId, user);
        return experiments.stream().map(ExperimentTreeNodeDTO::new).sorted(TreeNodeDTO.NAME_COMPARATOR).collect(Collectors.toList());
    }

    public List<ExperimentDTO> getAllExperimentNotebookSummary(String projectId, String notebookId, User user) {
        Collection<Experiment> experiments = getAllExperiments(projectId, notebookId, PermissionUtil.isContentEditor(user) ? null : user);
        return experiments.stream().sorted((e1,e2) -> {
            int i = e1.getName().compareTo(e2.getName());
            if (i == 0){
                i = e1.getExperimentVersion() - e2.getExperimentVersion();
            }
            return i;
        }).map(ExperimentDTO::new).collect(Collectors.toList());
    }

    /**
     * If user is null, then retrieve experiments without checking for UserPermissions
     * Otherwise, use checking for UserPermissions
     */
    private Collection<Experiment> getAllExperiments(String projectId, String notebookId, User user) {
        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(SequenceIdUtil.buildFullId(projectId, notebookId))).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

        if (user == null) {
            return notebook.getExperiments();
        }

        // Check of EntityAccess (User must have "Read Entity" permission in notebook's access list)
        if (!PermissionUtil.hasPermissions(user.getId(), notebook.getAccessList(),
                UserPermission.READ_ENTITY)) {
            throw OperationDeniedException.createNotebookSubEntitiesReadOperation(notebook.getId());
        }

        return getExperimentsWithAccess(notebook.getExperiments(), user.getId());
    }

    public ExperimentDTO getExperiment(String projectId, String notebookId, String id, User user) {
        Experiment experiment = Optional
                .ofNullable(experimentRepository.findOne(SequenceIdUtil.buildFullId(projectId, notebookId, id)))
                .orElseThrow(() -> EntityNotFoundException.createWithExperimentId(id));

        // Check of EntityAccess (User must have "Read Entity" permission in notebook's access list and
        // "Read Entity" in experiment's access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(experiment.getId());
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(experiment.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    notebook.getAccessList(), UserPermission.READ_ENTITY,
                    experiment.getAccessList(), UserPermission.READ_ENTITY)) {
                throw OperationDeniedException.createExperimentReadOperation(experiment.getId());
            }
        }
        return new ExperimentDTO(experiment);
    }

    public Collection<ExperimentDTO> getExperimentsByAuthor(User user) {
        return experimentRepository.findByAuthor(user).stream().map(ExperimentDTO::new).collect(Collectors.toList());
    }

    public ExperimentDTO createExperiment(ExperimentDTO experimentDTO, String projectId, String notebookId, User user) {
        Project project = projectRepository.findOne(projectId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }
        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(SequenceIdUtil.buildFullId(projectId, notebookId))).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

        // check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw OperationDeniedException.createNotebookSubEntityCreateOperation(notebook.getId());
        }

        Experiment experiment = dtoMapper.convertFromDTO(experimentDTO);
        experiment.setStatus(ExperimentStatus.OPEN);

        if (experimentDTO.getTemplate() != null) {
            Template tmpl = new Template();
            tmpl.setTemplateContent(experimentDTO.getTemplate().getTemplateContent());
            experiment.setTemplate(tmpl);
        }
        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, experiment.getAccessList());
        // add OWNER's permissions for specified User to experiment
        PermissionUtil.addOwnerToAccessList(experiment.getAccessList(), user);
        // add VIEWER's permissions for Project Author to experiment, if Experiment creator is another User
        PermissionUtil.addProjectAuthorToAccessList(experiment.getAccessList(), project.getAuthor(), user);

        //increment sequence Id
        experiment.setId(sequenceIdService.getNextExperimentId(projectId, notebookId));

        experiment.setComponents(updateComponents(null, experiment.getComponents(), experiment.getId()));

        //generate name
        experiment.setName(SequenceIdUtil.generateExperimentName(experiment));

        //set latest version
        experiment.setExperimentVersion(1);
        experiment.setLastVersion(true);

        experiment = experimentRepository.save(experiment);

        // add all users as VIEWER to notebook & project
        experiment.getAccessList().forEach(up -> {
            PermissionUtil.addUserPermissions(notebook.getAccessList(), up.getUser(), UserPermission.VIEWER_PERMISSIONS);
            PermissionUtil.addUserPermissions(project.getAccessList(), up.getUser(), UserPermission.VIEWER_PERMISSIONS);
        });
        notebook.getExperiments().add(experiment);
        notebookRepository.save(notebook);
        projectRepository.save(project);

        return new ExperimentDTO(experiment);
    }

    public ExperimentDTO versionExperiment(String experimentName, String projectId, String notebookId, User user) {
        if (StringUtils.isEmpty(experimentName)) {
            throw new IllegalArgumentException("Experiment name cannot be null.");
        }
        Project project = projectRepository.findOne(projectId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }
        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(SequenceIdUtil.buildFullId(projectId, notebookId))).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

        // check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw OperationDeniedException.createNotebookSubEntityCreateOperation(notebook.getId());
        }

        notebook.getExperiments().stream()
                .filter(e -> experimentName.equals(e.getName()) && ExperimentStatus.OPEN.equals(e.getStatus()))
                .findAny().ifPresent((e) ->{throw OperationDeniedException.createVersionExperimentOperation(e.getId());});

        // Update previous version
        Experiment lastVersion = notebook.getExperiments().stream().filter(e -> e.isLastVersion() && experimentName.equals(e.getName()))
                .findFirst().orElseThrow(() -> EntityNotFoundException.createWithExperimentName(experimentName));
        lastVersion.setLastVersion(false);
        experimentRepository.save(lastVersion);

        int newExperimentVersion = lastVersion.getExperimentVersion() + 1;

        // Save new version
        Experiment newVersion = new Experiment();
        String id;
        if (lastVersion.getExperimentVersion() > 1) {
            id = lastVersion.getId().split("_")[0];
        } else {
            id = lastVersion.getId();
        }
        newVersion.setId(id + "_" + newExperimentVersion);
        newVersion.setName(experimentName);
        newVersion.setAccessList(lastVersion.getAccessList());
        PermissionUtil.addOwnerToAccessList(newVersion.getAccessList(),user);
        newVersion.setTemplate(lastVersion.getTemplate());
        newVersion.setStatus(ExperimentStatus.OPEN);
        final List<Component> components = lastVersion.getComponents();
        components.forEach(c -> c.setId(null));
        final List<Component> newComponents = updateComponents(Collections.emptyList(), components, newVersion.getId());
        newVersion.setComponents(newComponents);
        newVersion.setLastVersion(true);
        newVersion.setExperimentVersion(newExperimentVersion);

        final Experiment savedNewVersion = experimentRepository.save(newVersion);
        notebook.getExperiments().add(savedNewVersion);
        notebookRepository.save(notebook);

        return new ExperimentDTO(savedNewVersion);
    }

    public ExperimentDTO updateExperiment(String projectId, String notebookId, ExperimentDTO experimentDTO, User user) {
        Lock lock = locks.get(projectId);
        ExperimentDTO result;
        try {
            lock.lock();
            Experiment experimentFromDB = Optional.ofNullable(experimentRepository.findOne(SequenceIdUtil.buildFullId(projectId, notebookId, experimentDTO.getId()))).
                    orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentDTO.getId()));

            // Check of EntityAccess (User must have "Read Entity" permission in notebook's access list and
            // "Update Entity" in experiment's access list, or must have CONTENT_EDITOR authority)
            if (!PermissionUtil.isContentEditor(user)) {
                Notebook notebook = notebookRepository.findByExperimentId(experimentFromDB.getId());
                if (notebook == null) {
                    throw EntityNotFoundException.createWithNotebookChildId(experimentFromDB.getId());
                }

                if (!PermissionUtil.hasPermissions(user.getId(),
                        notebook.getAccessList(), UserPermission.READ_ENTITY,
                        experimentFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                    throw OperationDeniedException.createExperimentUpdateOperation(experimentFromDB.getId());
                }
            }

            Experiment experimentForSave = dtoMapper.convertFromDTO(experimentDTO);

            //Check experiment version before component's saving
            if (!experimentForSave.getVersion().equals(experimentFromDB.getVersion())){
                throw ConcurrencyException.createWithExperimentName(experimentFromDB.getName(), new IndigoRuntimeException());
            }

            if (experimentDTO.getTemplate() != null) {
                Template tmpl = new Template();
                tmpl.setTemplateContent(experimentDTO.getTemplate().getTemplateContent());
                experimentForSave.setTemplate(tmpl);
            }

            // check of user permissions's correctness in access control list
            PermissionUtil.checkCorrectnessOfAccessList(userRepository, experimentForSave.getAccessList());

            experimentFromDB.setTemplate(experimentForSave.getTemplate());
            experimentFromDB.setAccessList(experimentForSave.getAccessList());
            experimentFromDB.setComments(experimentForSave.getComments());
            experimentFromDB.setStatus(experimentForSave.getStatus());
            experimentFromDB.setDocumentId(experimentForSave.getDocumentId());
            experimentFromDB.setSubmittedBy(experimentForSave.getSubmittedBy());
            experimentFromDB.setVersion(experimentForSave.getVersion());

            experimentFromDB.setComponents(updateComponents(experimentFromDB.getComponents(), experimentForSave.getComponents(), experimentFromDB.getId()));

            Experiment savedExperiment;
            try {
                savedExperiment = experimentRepository.save(experimentFromDB);
            } catch (OptimisticLockingFailureException e) {
                throw ConcurrencyException.createWithExperimentName(experimentFromDB.getName(), e);
            }
            result = new ExperimentDTO(savedExperiment);

            Project project = Optional.ofNullable(projectRepository.findOne(projectId)).
                    orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));
            Notebook notebook = Optional.ofNullable(notebookRepository.findOne(SequenceIdUtil.buildFullId(projectId, notebookId))).
                    orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));
            // add all users as VIEWER to project
            experimentDTO.getAccessList().forEach(up -> {
                PermissionUtil.addUserPermissions(notebook.getAccessList(), up.getUser(), UserPermission.VIEWER_PERMISSIONS);
                PermissionUtil.addUserPermissions(project.getAccessList(), up.getUser(), UserPermission.VIEWER_PERMISSIONS);
            });
            notebookRepository.save(notebook);
            projectRepository.save(project);

            Map<String, Object> data = new HashMap<>();
            data.put("user", user.getId());
            Map<String, Object> entity = new HashMap<>();
            entity.put("projectId", projectId);
            entity.put("notebookId", notebookId);
            entity.put("experimentId", SequenceIdUtil.extractShortId(experimentFromDB));
            data.put("entity", entity);
            data.put("version", savedExperiment.getVersion());
            template.convertAndSend("/topic/entity_updated", data);

        } finally {
            lock.unlock();
        }
        return result;
    }

    private List<Component> updateComponents(List<Component> oldComponents, List<Component> newComponents, String experimentId) {

        List<Component> componentsFromDb = oldComponents != null ? oldComponents : Collections.emptyList();
        List<String> componentIdsForRemove = componentsFromDb.stream().filter(Objects::nonNull).map(Component::getId).collect(Collectors.toList());
        DBRef dbRef = new DBRef(Experiment.COLLECTION_NAME, experimentId);

        List<Component> componentsForSave = new ArrayList<>();
        for (Component component : newComponents) {
            if (component.getId() != null) {
                Optional<Component> existing = componentsFromDb.stream().filter(Objects::nonNull).filter(c -> c.getId().equals(component.getId())).findFirst();
                if (existing.isPresent()) {
                    Component componentForSave = existing.get();
                    componentForSave.setContent(component.getContent());
                    componentIdsForRemove.remove(componentForSave.getId());
                    componentForSave.setName(componentForSave.getName());
                    componentForSave.setExperiment(dbRef);
                    componentsForSave.add(componentForSave);
                } else {
                    throw new ValidationException("Cannot find component with id=" + component.getId());
                }
            } else {
                component.setExperiment(dbRef);
                componentsForSave.add(component);
            }
        }

        componentRepository.delete(
                componentsFromDb.stream()
                        .filter(Objects::nonNull)
                        .filter(c -> componentIdsForRemove.contains(c.getId()))
                        .collect(Collectors.toList()));

        return componentRepository.save(componentsForSave);
    }

    public void deleteExperiment(String id, String projectId, String notebookId) {
        Experiment experiment = Optional.ofNullable(experimentRepository.findOne(SequenceIdUtil.buildFullId(projectId, notebookId, id))).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(id));

        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(notebookId)).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookChildId(experiment.getId()));

        notebook.getExperiments().remove(experiment);
        notebookRepository.save(notebook);

        //delete experiment components
        Optional.ofNullable(experiment.getComponents()).ifPresent(components ->
                        componentRepository.deleteAllById(components.stream().map(Component::getId).collect(Collectors.toList()))
        );

        fileRepository.delete(experiment.getFileIds());
        experimentRepository.delete(experiment);
    }

    public Lock getLock(String projectId){
        return locks.get(projectId);
    }
}
